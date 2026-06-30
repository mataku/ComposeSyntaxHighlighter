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
    namespace = "io.github.mataku.compose.highlight.material3.textfield"
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
      api(project(":core"))
      api(libs.compose.runtime)
      api(libs.compose.ui)
      api(libs.compose.foundation)
      api(libs.compose.material3)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest {
      dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.compose.uiTest)
        implementation(projects.languages.kotlin)
        implementation(projects.languages.python)
      }
    }
    // Skiko awt-runtime is a runtime-only dependency for jvmTest: nothing in commonTest references org.jetbrains.skiko.* directly.
    // runComposeUiTest renders Compose via Skiko on the JVM, and the awt-runtime ships per OS/arch via classifier.
    // Without it, runComposeUiTest fails at startup with "Skiko library not found".
    // Android uses the platform view system, so this dep is JVM-only.
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
}

mavenPublishing {
  coordinates(artifactId = "compose-syntax-highlight-material3-text-field")
  pom {
    name.set("Compose Syntax Highlight Material3 Text Field")
    description.set(
      "Material3 BasicTextField wrapper with syntax-highlighted overlay backed by IncrementalHighlighter",
    )
  }
}

tasks.named<Test>("jvmTest") {
  val languageProjects = listOf(
    project(":languages:kotlin"),
    project(":languages:python"),
  )
  languageProjects.forEach { dependsOn(it.tasks.named("buildHostCMake")) }
  val libPaths = languageProjects.joinToString(":") {
    it.layout.buildDirectory.dir("host-cmake").get().asFile.absolutePath
  }
  doFirst {
    systemProperty("java.library.path", libPaths)
  }
}
