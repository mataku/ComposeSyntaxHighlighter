import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
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
      api(project(":core"))
      api(libs.compose.runtime)
      api(libs.compose.ui)
      api(libs.compose.foundation)
      api(libs.compose.material3)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

android {
  namespace = "io.github.mataku.compose.highlight.material3"
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
  coordinates(artifactId = "compose-syntax-highlight-material3")
  pom {
    name.set("Compose Syntax Highlight Material3")
    description.set("Material3 binding for Compose Syntax Highlight: provides SyntaxHighlightedText")
  }
}
