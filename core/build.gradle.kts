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
      api(project(":core-api"))
      api(libs.ktreesitter)
      api(libs.compose.runtime)
      api(libs.compose.ui)
      api(libs.kotlinx.coroutines.core)
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
  coordinates(artifactId = "compose-syntax-highlight-core")
  pom {
    name.set("Compose Syntax Highlight Core")
    description.set("Compose Multiplatform syntax highlighter (core API)")
  }
}
