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
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.compose.uiTest)
      implementation(projects.languages.kotlin)
    }
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

android {
  namespace = "io.github.mataku.compose.highlight.material3.textfield"
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
  coordinates(artifactId = "compose-syntax-highlight-material3-text-field")
  pom {
    name.set("Compose Syntax Highlight Material3 Text Field")
    description.set(
      "Material3 BasicTextField wrapper with syntax-highlighted overlay backed by IncrementalHighlighter",
    )
  }
}

tasks.named<Test>("jvmTest") {
  val kotlinLangProject = project(":languages:kotlin")
  dependsOn(kotlinLangProject.tasks.named("buildHostCMake"))
  val libPath = kotlinLangProject.layout.buildDirectory
    .dir("host-cmake")
    .get()
    .asFile.absolutePath
  doFirst {
    systemProperty("java.library.path", libPath)
  }
}
