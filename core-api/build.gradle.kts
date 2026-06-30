import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  id("com.android.kotlin.multiplatform.library")
  alias(libs.plugins.vanniktechPublish)
  id("compose-syntax-highlight-kdoc")
  id("compose-syntax-highlight-spotless")
}

kotlin {
  android {
    namespace = "io.github.mataku.compose.highlight.api"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    androidResources { enable = true }
    packaging {
      resources {
        excludes -= setOf("/META-INF/NOTICE", "/META-INF/NOTICE.txt", "/META-INF/NOTICE.md")
        pickFirsts += "/META-INF/NOTICE"
      }
    }
  }

  jvm()
  iosArm64()
  iosSimulatorArm64()
  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(libs.ktreesitter)
      compileOnly(libs.compose.runtime)
    }
  }
}

mavenPublishing {
  coordinates(artifactId = "compose-syntax-highlight-api")
  pom {
    name.set("Compose Syntax Highlight API")
    description.set("Stable cross-module SPI for Compose Syntax Highlight language modules")
  }
}
