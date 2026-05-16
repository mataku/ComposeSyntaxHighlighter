import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  id("com.android.kotlin.multiplatform.library")
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.vanniktechPublish)
  id("compose-syntax-highlight-kdoc")
  id("compose-syntax-highlight-spotless")
}

kotlin {
  android {
    namespace = "io.github.mataku.compose.highlight.core"
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
  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api("${libs.coreApi.get().module}") {
        version { strictly(libs.versions.coreApiCompatibleRange.get()) }
      }
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

mavenPublishing {
  coordinates(artifactId = "compose-syntax-highlight-core")
  pom {
    name.set("Compose Syntax Highlight Core")
    description.set("Compose Multiplatform syntax highlighter (core API)")
  }
}
