import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktechPublish)
    id("compose-highlight-kdoc")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        publishLibraryVariants("release")
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(project(":core-api"))
            api(libs.ktreesitter)
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }

    sourceSets.all {
        languageSettings.optIn("io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi")
    }
}

android {
    namespace = "io.github.mataku.compose.highlight.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets.named("main") {
        resources.srcDirs("src/commonMain/resources")
    }
    packaging {
        resources {
            excludes -= setOf("/META-INF/NOTICE", "/META-INF/NOTICE.txt", "/META-INF/NOTICE.md")
            pickFirsts += "/META-INF/NOTICE"
        }
    }
}

mavenPublishing {
    coordinates(artifactId = "compose-highlight-core")
    pom {
        name.set("Compose Highlight Core")
        description.set("Compose Multiplatform syntax highlighter (core API)")
    }
}
