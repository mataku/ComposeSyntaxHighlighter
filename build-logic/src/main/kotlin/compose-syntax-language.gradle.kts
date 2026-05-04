import com.android.build.api.variant.LibraryAndroidComponentsExtension
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

fun catalogVersionInt(alias: String): Int =
    versionCatalog.findVersion(alias).get().requiredVersion.toInt()

val highlightsQueryDir = layout.buildDirectory.dir("generated/highlights")
val hostCMakeWorkDir = layout.buildDirectory.dir("host-cmake")

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

val configureHostCMake = tasks.register<Exec>("configureHostCMake") {
    val workDirProvider = hostCMakeWorkDir.map { it.asFile }
    val srcDir = projectDir.resolve("host-cmake")
    inputs.file(srcDir.resolve("CMakeLists.txt"))
    outputs.file(workDirProvider.map { File(it, "CMakeCache.txt") })
    doFirst { workDirProvider.get().mkdirs() }
    workingDir(workDirProvider)
    commandLine("cmake", srcDir.absolutePath)
    dependsOn("generateGrammarFiles")
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
}

extensions.configure<LibraryAndroidComponentsExtension>("androidComponents") {
    finalizeDsl { dsl ->
        val name = composeSyntaxLanguage.languageName.orNull
            ?: error("composeSyntaxLanguage.languageName must be set in the consumer build script")
        dsl.namespace = "io.github.mataku.compose.syntax.language.$name"
    }
}

afterEvaluate {
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
}
