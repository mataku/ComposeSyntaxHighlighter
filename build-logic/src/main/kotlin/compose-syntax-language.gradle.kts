import com.android.build.gradle.LibraryExtension
import io.github.mataku.compose.syntax.buildlogic.ComposeSyntaxLanguageExtension
import io.github.mataku.compose.syntax.buildlogic.writeAndroidCMakeLists
import io.github.mataku.compose.syntax.buildlogic.writeHostCMakeLists
import io.github.treesitter.ktreesitter.plugin.GrammarExtension
import io.github.treesitter.ktreesitter.plugin.GrammarFilesTask
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("io.github.tree-sitter.ktreesitter-plugin")
}

val composeSyntaxLanguage = extensions.create<ComposeSyntaxLanguageExtension>("composeSyntaxLanguage")

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val grammarDirProvider: Provider<File> = composeSyntaxLanguage.grammarSubmodulePath.map { projectDir.resolve(it) }
val packageNameProvider: Provider<String> = composeSyntaxLanguage.languageName.map {
    "io.github.mataku.compose.syntax.language.$it.internal"
}
val highlightsPackageDirProvider: Provider<String> = composeSyntaxLanguage.languageName.map {
    "io/github/mataku/compose/syntax/language/$it"
}

val highlightsQueryDir = layout.buildDirectory.dir("generated/highlights")

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

val generateGrammarFilesTask = tasks.named<GrammarFilesTask>("generateGrammarFiles")

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

val hostCMakeWorkDir = layout.buildDirectory.dir("host-cmake")

val configureHostCMake = tasks.register<Exec>("configureHostCMake") {
    val workDirProvider = hostCMakeWorkDir.map { it.asFile }
    val srcDir = projectDir.resolve("host-cmake")
    inputs.file(srcDir.resolve("CMakeLists.txt"))
    outputs.file(workDirProvider.map { File(it, "CMakeCache.txt") })
    doFirst { workDirProvider.get().mkdirs() }
    workingDir(workDirProvider)
    commandLine("cmake", srcDir.absolutePath)
    dependsOn(generateGrammarFilesTask)
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

val regenerateCMakeLists = tasks.register("regenerateCMakeLists") {
    val androidFile = projectDir.resolve("CMakeLists.txt")
    val hostFile = projectDir.resolve("host-cmake/CMakeLists.txt")
    val nameProvider = composeSyntaxLanguage.languageName
    val grammarPathProvider = composeSyntaxLanguage.grammarSubmodulePath
    val sourcesProvider = composeSyntaxLanguage.sources
    outputs.file(androidFile)
    outputs.file(hostFile)
    doLast {
        writeAndroidCMakeLists(
            androidFile,
            nameProvider.get(),
            grammarPathProvider.get(),
            sourcesProvider.get(),
        )
        writeHostCMakeLists(
            hostFile,
            nameProvider.get(),
            grammarPathProvider.get(),
            sourcesProvider.get(),
        )
    }
}

afterEvaluate {
    val name = composeSyntaxLanguage.languageName.get()
    val grammarPath = composeSyntaxLanguage.grammarSubmodulePath.get()
    val sourcesList = composeSyntaxLanguage.sources.get()
    writeAndroidCMakeLists(
        projectDir.resolve("CMakeLists.txt"),
        name,
        grammarPath,
        sourcesList,
    )
    writeHostCMakeLists(
        projectDir.resolve("host-cmake/CMakeLists.txt"),
        name,
        grammarPath,
        sourcesList,
    )
}

extensions.configure<KotlinMultiplatformExtension>("kotlin") {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("ktreesitter") {
                withAndroidTarget()
                withJvm()
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

    sourceSets {
        val generatedSrc = generateGrammarFilesTask.flatMap { it.generatedSrc }
        configureEach {
            kotlin.srcDir(generatedSrc.map { it.dir(name).dir("kotlin") })
        }
        commonMain {
            dependencies {
                api(project(":core"))
            }
        }
        val ktreesitterMain by getting {
            kotlin.srcDir(highlightsQueryDir)
            dependencies {
                api(versionCatalog.findLibrary("ktreesitter").get())
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(versionCatalog.findLibrary("kotlin-test").get())
                implementation(versionCatalog.findLibrary("compose-ui").get())
            }
        }
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateGrammarFilesTask, generateHighlightsQuery)
}

tasks.matching {
    it.name.startsWith("configureCMake") || it.name.startsWith("buildCMake")
}.configureEach {
    dependsOn(generateGrammarFilesTask)
}

tasks.named<Test>("jvmTest") {
    dependsOn(buildHostCMake)
    val libDirProvider = hostCMakeWorkDir.map { it.asFile.absolutePath }
    doFirst {
        systemProperty("java.library.path", libDirProvider.get())
    }
}

extensions.configure<LibraryExtension>("android") {
    compileSdk = 36
    ndkVersion = "26.3.11579264"

    defaultConfig {
        minSdk = 26
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
}

afterEvaluate {
    extensions.configure<LibraryExtension>("android") {
        namespace = "io.github.mataku.compose.syntax.language.${composeSyntaxLanguage.languageName.get()}"
    }
}
