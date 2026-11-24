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
    @OptIn(org.gradle.api.Incubating::class)
    optimization.consumerKeepRules.apply {
      publish = true
      file("consumer-rules.pro")
    }
  }

  jvm {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
      freeCompilerArgs.add("-Xjdk-release=17")
    }
  }
  iosArm64()
  iosSimulatorArm64()
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
      implementation(libs.compose.uiTest)
      implementation(libs.kotlinx.coroutines.test)
      implementation(projects.languages.kotlin)
      implementation(projects.languages.python)
    }
    // Skiko awt-runtime is a runtime-only dependency for jvmTest: runComposeUiTest renders Compose via Skiko on the JVM,
    // and the awt-runtime ships per OS/arch via classifier. Without it, runComposeUiTest fails with "Skiko library not found".
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlin.test)
        val skikoClassifier = run {
          val osName = System.getProperty("os.name").lowercase()
          val osArch = System.getProperty("os.arch")
          when {
            osName.contains("mac") && osArch == "aarch64" -> "macos-arm64"
            osName.contains("mac") -> "macos-x64"
            osName.contains("linux") && osArch == "aarch64" -> "linux-arm64"
            osName.contains("linux") -> "linux-x64"
            osName.contains("windows") -> "windows-x64"
            else -> error("Unsupported OS/arch for Skiko runtime: $osName $osArch")
          }
        }
        implementation("org.jetbrains.skiko:skiko-awt-runtime-$skikoClassifier:${libs.versions.skiko.get()}")
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
