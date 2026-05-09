import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  id("compose-syntax-highlight-spotless")
}

kotlin {
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

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

    val androidInstrumentedTest by getting {
      dependsOn(commonTest.get())
      dependencies {
        implementation(libs.androidx.benchmark.junit4)
        implementation(libs.androidx.test.runner)
        implementation(libs.androidx.testExt.junit)
        implementation(libs.compose.ui)
      }
    }

    val jvmTest by getting
  }

  sourceSets.all {
    languageSettings.optIn("io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi")
  }
}

tasks.named<Test>("jvmTest") {
  val languageProjects =
    listOf(
      project(":languages:kotlin"),
      project(":languages:swift"),
      project(":languages:ruby"),
      project(":languages:rust"),
      project(":languages:python"),
      project(":languages:go"),
      project(":languages:java"),
    )
  languageProjects.forEach { dependsOn(it.tasks.named("buildHostCMake")) }
  val libPaths =
    languageProjects.joinToString(":") {
      it.layout.buildDirectory
        .dir("host-cmake")
        .get()
        .asFile.absolutePath
    }
  doFirst {
    systemProperty("java.library.path", libPaths)
  }
}

android {
  namespace = "io.github.mataku.compose.highlight.benchmark"
  compileSdk =
    libs.versions.android.compileSdk
      .get()
      .toInt()

  defaultConfig {
    minSdk =
      libs.versions.android.minSdk
        .get()
        .toInt()
    testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
  }

  testBuildType = "benchmark"

  buildTypes {
    create("benchmark") {
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += listOf("release")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  sourceSets["androidTest"].resources.srcDirs("src/commonTest/resources")
}
