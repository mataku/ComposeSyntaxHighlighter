import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.vanniktechPublish)
  id("compose-syntax-highlight-kdoc")
  id("compose-syntax-highlight-spotless")
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
      api(libs.ktreesitter)
    }
  }
}

android {
  namespace = "io.github.mataku.compose.highlight.api"
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
  coordinates(artifactId = "compose-syntax-highlight-api")
  pom {
    name.set("Compose Syntax Highlight API")
    description.set("Stable cross-module SPI for Compose Syntax Highlight language modules")
  }
}
