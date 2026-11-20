import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  id("compose-syntax-highlight-spotless")
}

kotlin {
  jvm {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  sourceSets {
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(projects.core)
      implementation(projects.languages.kotlin)
      implementation(projects.languages.swift)
      implementation(projects.languages.ruby)
      implementation(projects.languages.rust)
      implementation(projects.languages.python)
      implementation(projects.languages.go)
      implementation(projects.languages.java)
    }

    // TextLayoutBenchmark measures Compose text layout, which runs on Skiko on the JVM; the awt-runtime ships per OS/arch via classifier and nothing else puts it on this module's classpath.
    val jvmTest by getting {
      dependencies {
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

tasks.named<Test>("jvmTest") {
  minHeapSize = "2g"
  maxHeapSize = "2g"
  jvmArgs("-XX:+AlwaysPreTouch")
}
