import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ktreesitter)
}

val grammarDir = projectDir.resolve("tree-sitter-kotlin")

grammar {
    baseDir = grammarDir
    grammarName = "kotlin"
    className = "TreeSitterKotlin"
    packageName = "io.github.mataku.compose.syntax.language.kotlin.internal"
    files = arrayOf(
        grammarDir.resolve("src/scanner.c"),
        grammarDir.resolve("src/parser.c"),
    )
}

val generateTask = tasks.generateGrammarFiles.get()

val highlightsQueryDir = layout.buildDirectory.dir("generated/highlights")

val generateHighlightsQuery = tasks.register("generateHighlightsQuery") {
    val src = grammarDir.resolve("queries/highlights.scm")
    val outDir = highlightsQueryDir.get().asFile
    inputs.file(src)
    outputs.dir(outDir)
    doLast {
        val text = src.readText()
        val out = File(outDir, "io/github/mataku/compose/syntax/language/kotlin/HighlightsQuery.kt")
        out.parentFile.mkdirs()
        out.writeText(
            """
            |package io.github.mataku.compose.syntax.language.kotlin
            |
            |internal const val HIGHLIGHTS_QUERY: String = ${'"'}${'"'}${'"'}
            |$text${'"'}${'"'}${'"'}
            |
            """.trimMargin()
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        publishLibraryVariants("release")
    }

    sourceSets {
        val generatedSrc = generateTask.generatedSrc.get()
        configureEach {
            kotlin.srcDir(generatedSrc.dir(name).dir("kotlin"))
        }
        commonMain {
            kotlin.srcDir(highlightsQueryDir)
            dependencies {
                api(libs.ktreesitter)
                api(projects.core)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateTask, generateHighlightsQuery)
}

tasks.matching {
    it.name.startsWith("configureCMake") || it.name.startsWith("buildCMake")
}.configureEach {
    dependsOn(generateTask)
}

android {
    namespace = "io.github.mataku.compose.syntax.language.kotlin"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    ndkVersion = "26.3.11579264"

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        ndk {
            //noinspection ChromeOsAbiSupport
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
