plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeCompiler)
  id("compose-syntax-highlight-spotless")
}

android {
  namespace = "com.mataku.composesyntaxhighlighter"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  defaultConfig {
    applicationId = "com.mataku.composesyntaxhighlighter"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
    create("benchmark") {
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += listOf("release")
      isDebuggable = false
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

dependencies {
  implementation(projects.samples.composeApp)
  implementation(libs.androidx.activity.compose)
  debugImplementation(libs.compose.uiTooling)
}
