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

    val jvmTest by getting
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
