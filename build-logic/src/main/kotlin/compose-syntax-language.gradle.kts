import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import io.github.mataku.compose.syntax.buildlogic.ComposeSyntaxLanguageExtension
import io.github.mataku.compose.syntax.buildlogic.WasmToolchainAvailableSpec
import io.github.mataku.compose.syntax.buildlogic.writeAndroidCMakeLists
import io.github.mataku.compose.syntax.buildlogic.writeHostCMakeLists
import io.github.treesitter.ktreesitter.plugin.GrammarExtension
import io.github.treesitter.ktreesitter.plugin.GrammarFilesTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("io.github.tree-sitter.ktreesitter-plugin")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.vanniktech.maven.publish")
}

val composeSyntaxLanguage = extensions.create<ComposeSyntaxLanguageExtension>("composeSyntaxLanguage")

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun catalogVersionInt(alias: String): Int =
    versionCatalog.findVersion(alias).get().requiredVersion.toInt()

val highlightsQueryDir = layout.buildDirectory.dir("generated/highlights")
val hostCMakeWorkDir = layout.buildDirectory.dir("host-cmake")
val wasmStubsDir = layout.buildDirectory.dir("generated/wasm-stubs")
val noticeOutDir = layout.buildDirectory.dir("notice")

val grammarDirProvider: Provider<File> = composeSyntaxLanguage.grammarSubmodulePath.map { projectDir.resolve(it) }
val packageNameProvider: Provider<String> = composeSyntaxLanguage.languageName.map {
    "io.github.mataku.compose.syntax.language.$it.internal"
}
val highlightsPackageDirProvider: Provider<String> = composeSyntaxLanguage.languageName.map {
    "io/github/mataku/compose/syntax/language/$it"
}

extensions.configure<GrammarExtension>("grammar") {
    baseDir.set(grammarDirProvider)
    grammarName.set(composeSyntaxLanguage.languageName)
    className.set(composeSyntaxLanguage.parserClassName)
    packageName.set(packageNameProvider)
    files.set(
        composeSyntaxLanguage.sources.zip(grammarDirProvider) { sources, grammarDir ->
            sources.map { grammarDir.resolve(it) }.toTypedArray()
        }
    )
}

val generateWasmTreeSitterStub = tasks.register("generateWasmTreeSitterStub") {
    val pkgProvider = packageNameProvider
    val classNameProvider = composeSyntaxLanguage.parserClassName
    val outDirProvider = wasmStubsDir.map { it.asFile }
    inputs.property("packageName", pkgProvider)
    inputs.property("className", classNameProvider)
    outputs.dir(outDirProvider)
    doLast {
        val pkg = pkgProvider.get()
        val cls = classNameProvider.get()
        val pkgDir = pkg.replace('.', '/')
        val out = File(outDirProvider.get(), "$pkgDir/$cls.kt")
        out.parentFile.mkdirs()
        out.writeText(
            """
            |// Automatically generated wasmJs stub. The wasmJs target loads grammars
            |// via web-tree-sitter at runtime, so this actual is never invoked.
            |package $pkg
            |
            |actual object $cls {
            |    actual fun language(): Any = error("$cls.language() is not used on wasmJs; load grammars via webTreeSitterLanguage instead")
            |}
            |
            """.trimMargin()
        )
    }
}

val generateNotice = tasks.register("generateNotice") {
    val tplFile = rootProject.file("NOTICE.tpl")
    val outDirProvider = noticeOutDir.map { it.asFile }
    val nameProvider = composeSyntaxLanguage.languageName
    val licenseSpdxProvider = composeSyntaxLanguage.licenseSpdx
    val licenseSourceProvider = composeSyntaxLanguage.licenseSource
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
    val srcProvider = composeSyntaxLanguage.queries.zip(grammarDirProvider) { queries, grammarDir ->
        require(queries.isNotEmpty()) { "composeSyntaxLanguage.queries must contain at least one entry" }
        grammarDir.resolve(queries.first())
    }
    val packageProvider = composeSyntaxLanguage.languageName
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
            |package io.github.mataku.compose.syntax.language.${packageProvider.get()}
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
    inputs.file(grammarJsProvider)
    outputs.file(parserCProvider)
    workingDir(grammarDirFileProvider)
    val abi = versionCatalog.findVersion("treesitterAbi").get().requiredVersion
    commandLine("tree-sitter", "generate", "--abi=$abi")
    doFirst {
        val check = ProcessBuilder("which", "tree-sitter")
            .redirectErrorStream(true)
            .start()
        check.waitFor()
        if (check.exitValue() != 0) {
            error(
                "tree-sitter CLI is not on PATH. Install with: " +
                    "npm i -g tree-sitter-cli  (Node 20+ required). " +
                    "See CONTRIBUTING.md."
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

val wasmGrammarOutFileProvider: Provider<File> = composeSyntaxLanguage.languageName.map {
    projectDir.resolve("src/wasmJsMain/composeResources/files/grammars/tree-sitter-$it.wasm")
}

val buildGrammarWasm = tasks.register<Exec>("buildGrammarWasm") {
    inputs.dir(grammarDirProvider.map { it.resolve("src") })
    inputs.file(grammarDirProvider.map { it.resolve("grammar.js") })
    outputs.file(wasmGrammarOutFileProvider)
    workingDir(grammarDirProvider)
    val outFileProvider = wasmGrammarOutFileProvider
    doFirst {
        val out = outFileProvider.get()
        out.parentFile.mkdirs()
        commandLine("tree-sitter", "build", "--wasm", "-o", out.absolutePath)
    }
    commandLine("tree-sitter", "--version")
    dependsOn(generateParserSource)
    onlyIf(WasmToolchainAvailableSpec)
}

extensions.configure<KotlinMultiplatformExtension>("kotlin") {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("ktreesitter") {
                withAndroidTarget()
                withJvm()
            }
            group("web") {
                withWasmJs()
            }
        }
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        publishLibraryVariants("release")
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        compilerOptions {
            optIn.add("kotlin.js.ExperimentalWasmJsInterop")
        }
    }

    sourceSets {
        commonMain {
            resources.srcDir(noticeOutDir)
            dependencies {
                api(project(":core"))
                implementation(versionCatalog.findLibrary("compose-runtime").get())
                implementation(versionCatalog.findLibrary("compose-components-resources").get())
            }
        }
        val ktreesitterMain by getting {
            kotlin.srcDir(highlightsQueryDir)
            dependencies {
                api(versionCatalog.findLibrary("ktreesitter").get())
            }
        }
        val wasmJsMain by getting {
            kotlin.srcDir(highlightsQueryDir)
            kotlin.srcDir(wasmStubsDir)
        }
        val jvmTest by getting {
            dependencies {
                implementation(versionCatalog.findLibrary("kotlin-test").get())
                implementation(versionCatalog.findLibrary("compose-ui").get())
            }
        }
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
        val name = composeSyntaxLanguage.languageName.orNull
            ?: error("composeSyntaxLanguage.languageName must be set in the consumer build script")
        dsl.namespace = "io.github.mataku.compose.syntax.language.$name"
    }
}

val composeExtension = extensions.getByType(org.jetbrains.compose.ComposeExtension::class.java)
val composeResourcesExtension = (composeExtension as ExtensionAware).extensions.getByType(ResourcesExtension::class.java)
composeResourcesExtension.apply {
    publicResClass = false
    generateResClass = always
}

afterEvaluate {
    composeResourcesExtension.packageOfResClass =
        "io.github.mataku.compose.syntax.language.${composeSyntaxLanguage.languageName.get()}.resources"

    val languageName = composeSyntaxLanguage.languageName.orNull
        ?: error("composeSyntaxLanguage.languageName must be set in the consumer build script")
    val grammarSubmodulePath = composeSyntaxLanguage.grammarSubmodulePath.orNull
        ?: error("composeSyntaxLanguage.grammarSubmodulePath must be set")
    @Suppress("UNUSED_VARIABLE")
    val parserClassName = composeSyntaxLanguage.parserClassName.orNull
        ?: error("composeSyntaxLanguage.parserClassName must be set")
    val sources = composeSyntaxLanguage.sources.orNull?.takeIf { it.isNotEmpty() }
        ?: error("composeSyntaxLanguage.sources must contain at least one C source path relative to the grammar submodule")
    @Suppress("UNUSED_VARIABLE")
    val queries = composeSyntaxLanguage.queries.orNull?.takeIf { it.isNotEmpty() }
        ?: error("composeSyntaxLanguage.queries must contain at least the highlights.scm path")

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
        dependsOn(generateGrammarFilesTask, generateHighlightsQuery, generateWasmTreeSitterStub)
    }

    tasks.matching {
        it.name.endsWith("SourcesJar", ignoreCase = true)
    }.configureEach {
        dependsOn(generateGrammarFilesTask, generateHighlightsQuery, generateWasmTreeSitterStub)
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
        it.name == "wasmJsBrowserProductionWebpack" ||
            it.name == "wasmJsBrowserDevelopmentWebpack" ||
            it.name == "wasmJsProcessResources" ||
            it.name == "copyNonXmlValueResourcesForWasmJsMain" ||
            it.name == "convertXmlValueResourcesForWasmJsMain"
    }.configureEach {
        dependsOn(buildGrammarWasm)
    }

    tasks.matching {
        val n = it.name
        n.endsWith("ProcessResources") ||
            n.startsWith("copyNonXmlValueResources") ||
            n.startsWith("convertXmlValueResources") ||
            n == "mergeReleaseJavaResource" ||
            n == "mergeDebugJavaResource"
    }.configureEach {
        dependsOn(generateNotice)
    }

    val mavenPublishing = extensions.getByType(com.vanniktech.maven.publish.MavenPublishBaseExtension::class.java)
    mavenPublishing.coordinates(artifactId = "compose-syntax-language-$languageName")
    mavenPublishing.pom {
        name.set("Compose Syntax Language: $languageName")
        description.set("tree-sitter $languageName grammar for the Compose Syntax Highlighter")
    }
}
