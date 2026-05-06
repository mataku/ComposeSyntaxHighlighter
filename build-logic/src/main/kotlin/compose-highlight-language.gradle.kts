import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import io.github.mataku.compose.highlight.buildlogic.ComposeHighlightLanguageExtension
import io.github.mataku.compose.highlight.buildlogic.writeAndroidCMakeLists
import io.github.mataku.compose.highlight.buildlogic.writeHostCMakeLists
import io.github.treesitter.ktreesitter.plugin.GrammarExtension
import io.github.treesitter.ktreesitter.plugin.GrammarFilesTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("io.github.tree-sitter.ktreesitter-plugin")
    id("com.vanniktech.maven.publish")
    id("compose-highlight-dokka")
}

val composeHighlightLanguage = extensions.create<ComposeHighlightLanguageExtension>("composeHighlightLanguage")

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun catalogVersionInt(alias: String): Int =
    versionCatalog.findVersion(alias).get().requiredVersion.toInt()

val highlightsQueryDir = layout.buildDirectory.dir("generated/highlights")
val hostCMakeWorkDir = layout.buildDirectory.dir("host-cmake")
val noticeOutDir = layout.buildDirectory.dir("notice")

val grammarDirProvider: Provider<File> = composeHighlightLanguage.grammarSubmodulePath.map { projectDir.resolve(it) }
val packageNameProvider: Provider<String> = composeHighlightLanguage.languageName.map {
    "io.github.mataku.compose.highlight.$it.internal"
}
val highlightsPackageDirProvider: Provider<String> = composeHighlightLanguage.languageName.map {
    "io/github/mataku/compose/highlight/$it"
}

extensions.configure<GrammarExtension>("grammar") {
    baseDir.set(grammarDirProvider)
    grammarName.set(composeHighlightLanguage.languageName)
    className.set(composeHighlightLanguage.parserClassName)
    packageName.set(packageNameProvider)
    files.set(
        composeHighlightLanguage.sources.zip(grammarDirProvider) { sources, grammarDir ->
            sources.map { grammarDir.resolve(it) }.toTypedArray()
        }
    )
}

val generateNotice = tasks.register("generateNotice") {
    val tplFile = rootProject.file("NOTICE.tpl")
    val outDirProvider = noticeOutDir.map { it.asFile }
    val nameProvider = composeHighlightLanguage.languageName
    val licenseSpdxProvider = composeHighlightLanguage.licenseSpdx
    val licenseSourceProvider = composeHighlightLanguage.licenseSource
    inputs.file(tplFile)
    inputs.property("languageName", nameProvider)
    inputs.property("licenseSpdx", licenseSpdxProvider)
    inputs.property("licenseSource", licenseSourceProvider)
    outputs.dir(outDirProvider)
    doLast {
        val baseTpl = tplFile.readText()
        val entry = buildString {
            appendLine()
            appendLine("Bundled grammar:")
            appendLine("  Component: tree-sitter-${nameProvider.get()}")
            appendLine("  License: ${licenseSpdxProvider.get()}")
            appendLine("  Source: ${licenseSourceProvider.get()}")
        }
        val outFile = File(outDirProvider.get(), "META-INF/NOTICE")
        outFile.parentFile.mkdirs()
        outFile.writeText(baseTpl + entry)
    }
}

val generateHighlightsQuery = tasks.register("generateHighlightsQuery") {
    val srcProvider = composeHighlightLanguage.queries.zip(grammarDirProvider) { queries, grammarDir ->
        require(queries.isNotEmpty()) { "composeHighlightLanguage.queries must contain at least one entry" }
        grammarDir.resolve(queries.first())
    }
    val packageProvider = composeHighlightLanguage.languageName
    val pkgDirProvider = highlightsPackageDirProvider
    val outDirProvider = highlightsQueryDir.map { it.asFile }
    inputs.file(srcProvider)
    outputs.dir(outDirProvider)
    doLast {
        val src = srcProvider.get()
        val outDir = outDirProvider.get()
        val text = src.readText()
        val out = File(outDir, "${pkgDirProvider.get()}/HighlightsQuery.kt")
        out.parentFile.mkdirs()
        out.writeText(
            """
            |package io.github.mataku.compose.highlight.${packageProvider.get()}
            |
            |internal const val HIGHLIGHTS_QUERY: String = ${'"'}${'"'}${'"'}
            |$text${'"'}${'"'}${'"'}
            |
            """.trimMargin()
        )
    }
}

val generateParserSource = tasks.register<Exec>("generateParserSource") {
    val grammarDirFileProvider = grammarDirProvider
    val grammarJsProvider = grammarDirFileProvider.map { it.resolve("grammar.js") }
    val parserCProvider = grammarDirFileProvider.map { it.resolve("src/parser.c") }
    val localTreeSitterBin = rootProject.file("node_modules/.bin/tree-sitter")
    inputs.file(grammarJsProvider)
    outputs.file(parserCProvider)
    workingDir(grammarDirFileProvider)
    val abi = versionCatalog.findVersion("treesitterAbi").get().requiredVersion
    val resolvedCommand = if (localTreeSitterBin.exists()) localTreeSitterBin.absolutePath else "tree-sitter"
    commandLine(resolvedCommand, "generate", "--abi=$abi")
    doFirst {
        val available = if (localTreeSitterBin.exists()) {
            true
        } else {
            val check = ProcessBuilder("which", "tree-sitter")
                .redirectErrorStream(true)
                .start()
            check.waitFor()
            check.exitValue() == 0
        }
        if (!available) {
            error(
                "tree-sitter CLI was not found. The convention plugin prefers the project-local " +
                    "install at <repo>/node_modules/.bin/tree-sitter and falls back to any " +
                    "tree-sitter on PATH. To populate the project-local install, run your " +
                    "package manager from the repository root (e.g. " +
                    "`pnpm install --frozen-lockfile`; see package.json for the pinned " +
                    "version). If lifecycle scripts are disabled in your environment " +
                    "(e.g. ~/.npmrc sets ignore-scripts=true), trigger the CLI binary " +
                    "download explicitly afterward (e.g. `pnpm rebuild tree-sitter-cli`)."
            )
        }
    }
    onlyIf {
        val parserC = parserCProvider.get()
        val grammarJs = grammarJsProvider.get()
        if (!parserC.exists()) return@onlyIf true
        if (!grammarJs.exists()) return@onlyIf false
        grammarJs.lastModified() > parserC.lastModified()
    }
}

val configureHostCMake = tasks.register<Exec>("configureHostCMake") {
    val workDirProvider = hostCMakeWorkDir.map { it.asFile }
    val srcDir = projectDir.resolve("host-cmake")
    inputs.file(srcDir.resolve("CMakeLists.txt"))
    outputs.file(workDirProvider.map { File(it, "CMakeCache.txt") })
    doFirst { workDirProvider.get().mkdirs() }
    workingDir(workDirProvider)
    commandLine("cmake", srcDir.absolutePath)
    dependsOn("generateGrammarFiles")
    dependsOn(generateParserSource)
}

val buildHostCMake = tasks.register<Exec>("buildHostCMake") {
    val workDirProvider = hostCMakeWorkDir.map { it.asFile }
    workingDir(workDirProvider)
    commandLine("cmake", "--build", ".")
    inputs.dir(projectDir.resolve("host-cmake"))
    inputs.dir(grammarDirProvider.map { it.resolve("src") })
    outputs.dir(workDirProvider)
    dependsOn(configureHostCMake)
}

extensions.configure<KotlinMultiplatformExtension>("kotlin") {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        publishLibraryVariants("release")
    }

    jvm()

    sourceSets {
        commonMain {
            resources.srcDir(noticeOutDir)
            kotlin.srcDir(highlightsQueryDir)
            dependencies {
                api(project(":core-api"))
                api(versionCatalog.findLibrary("ktreesitter").get())
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(versionCatalog.findLibrary("kotlin-test").get())
                implementation(versionCatalog.findLibrary("compose-ui").get())
                implementation(project(":core"))
            }
        }
    }

    sourceSets.all {
        languageSettings.optIn("io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi")
    }
}

extensions.configure<LibraryExtension>("android") {
    compileSdk = catalogVersionInt("android-compileSdk")
    ndkVersion = "26.3.11579264"

    defaultConfig {
        minSdk = catalogVersionInt("android-minSdk")
        ndk {
            abiFilters += setOf("x86_64", "arm64-v8a", "armeabi-v7a")
        }
    }
    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
            buildStagingDirectory = file(".cmake")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets.named("main") {
        resources.srcDirs(noticeOutDir)
    }
    packaging {
        resources {
            excludes -= setOf("/META-INF/NOTICE", "/META-INF/NOTICE.txt", "/META-INF/NOTICE.md")
            pickFirsts += "/META-INF/NOTICE"
        }
    }
}

extensions.configure<LibraryAndroidComponentsExtension>("androidComponents") {
    finalizeDsl { dsl ->
        val name = composeHighlightLanguage.languageName.orNull
            ?: error("composeHighlightLanguage.languageName must be set in the consumer build script")
        dsl.namespace = "io.github.mataku.compose.highlight.$name"
    }
}

afterEvaluate {
    val languageName = composeHighlightLanguage.languageName.orNull
        ?: error("composeHighlightLanguage.languageName must be set in the consumer build script")
    val grammarSubmodulePath = composeHighlightLanguage.grammarSubmodulePath.orNull
        ?: error("composeHighlightLanguage.grammarSubmodulePath must be set")
    composeHighlightLanguage.parserClassName.orNull
        ?: error("composeHighlightLanguage.parserClassName must be set")
    val sources = composeHighlightLanguage.sources.orNull?.takeIf { it.isNotEmpty() }
        ?: error("composeHighlightLanguage.sources must contain at least one C source path relative to the grammar submodule")
    composeHighlightLanguage.queries.orNull?.takeIf { it.isNotEmpty() }
        ?: error("composeHighlightLanguage.queries must contain at least the highlights.scm path")

    writeAndroidCMakeLists(
        projectDir.resolve("CMakeLists.txt"),
        languageName,
        grammarSubmodulePath,
        sources,
    )
    writeHostCMakeLists(
        projectDir.resolve("host-cmake/CMakeLists.txt"),
        languageName,
        grammarSubmodulePath,
        sources,
    )

    val generateGrammarFilesTask = tasks.named<GrammarFilesTask>("generateGrammarFiles")
    generateGrammarFilesTask.configure {
        dependsOn(generateParserSource)
    }
    val generatedSrc = generateGrammarFilesTask.get().generatedSrc.get()
    extensions.configure<KotlinMultiplatformExtension>("kotlin") {
        sourceSets.configureEach {
            kotlin.srcDir(generatedSrc.dir(this.name).dir("kotlin"))
        }
    }

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        dependsOn(generateGrammarFilesTask, generateHighlightsQuery)
    }

    tasks.matching {
        it.name.endsWith("SourcesJar", ignoreCase = true)
    }.configureEach {
        dependsOn(generateGrammarFilesTask, generateHighlightsQuery)
    }

    tasks.matching {
        it.name.startsWith("configureCMake") || it.name.startsWith("buildCMake")
    }.configureEach {
        dependsOn(generateGrammarFilesTask)
        dependsOn(generateParserSource)
    }

    tasks.named<Test>("jvmTest") {
        dependsOn(buildHostCMake)
        val libDirProvider = hostCMakeWorkDir.map { it.asFile.absolutePath }
        doFirst {
            systemProperty("java.library.path", libDirProvider.get())
        }
    }

    tasks.matching {
        val n = it.name
        n.endsWith("ProcessResources") ||
            n.startsWith("copyNonXmlValueResources") ||
            n.startsWith("convertXmlValueResources") ||
            n == "mergeReleaseJavaResource" ||
            n == "mergeDebugJavaResource" ||
            n == "processReleaseJavaRes" ||
            n == "processDebugJavaRes"
    }.configureEach {
        dependsOn(generateNotice)
    }

    val mavenPublishing = extensions.getByType(com.vanniktech.maven.publish.MavenPublishBaseExtension::class.java)
    mavenPublishing.coordinates(artifactId = "compose-highlight-$languageName")
    mavenPublishing.pom {
        name.set("Compose Highlight $languageName")
        description.set("$languageName syntax highlighting for Compose Multiplatform powered by tree-sitter")
    }
}
