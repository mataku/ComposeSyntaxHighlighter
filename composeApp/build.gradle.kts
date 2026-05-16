import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  id("com.android.kotlin.multiplatform.library")
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  id("compose-syntax-highlight-spotless")
}

kotlin {
  android {
    namespace = "com.mataku.composesyntaxhighlighter.shared"
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

  iosArm64 {
    binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  applyDefaultHierarchyTemplate()

  sourceSets {
    androidMain.dependencies {
      implementation(libs.compose.uiToolingPreview)
    }
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(projects.core)
      implementation(projects.material3)
      implementation(projects.material3TextField)
      implementation(projects.languages.kotlin)
      implementation(projects.languages.swift)
      implementation(projects.languages.ruby)
      implementation(projects.languages.rust)
      implementation(projects.languages.python)
      implementation(projects.languages.go)
      implementation(projects.languages.java)
      implementation(projects.languages.markdown)
      implementation(projects.languages.javascript)
      implementation(projects.languages.typescript)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}
