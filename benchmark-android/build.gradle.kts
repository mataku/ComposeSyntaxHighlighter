import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  alias(libs.plugins.androidLibrary)
  id("compose-syntax-highlight-spotless")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
  compilerOptions {
    optIn.add("io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi")
  }
}

android {
  namespace = "io.github.mataku.compose.highlight.benchmark"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
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

  // Share helpers (BenchmarkSamples, BenchmarkConfig) with :benchmark's commonTest tree.
  // Use the new com.android.build.api.dsl types directly to avoid the legacy Kotlin DSL
  // accessor that downcasts to com.android.build.gradle.api.AndroidLibrarySourceSet,
  // which fails at runtime in AGP 9 (the runtime instance only implements the new dsl
  // package's AndroidLibrarySourceSet).
  val androidTestSourceSet =
    (sourceSets as org.gradle.api.NamedDomainObjectContainer<com.android.build.api.dsl.AndroidSourceSet>)
      .getByName("androidTest")
  @Suppress("DEPRECATION")
  androidTestSourceSet.kotlin.srcDirs("../benchmark/src/commonTest/kotlin")
  @Suppress("DEPRECATION")
  androidTestSourceSet.resources.srcDirs("../benchmark/src/commonTest/resources")
  // AGP's source-set filter excludes `**/*.kt` / `**/*.java` from resources by default
  // (treated as sources). Clear that filter so BenchmarkSamples can load Kotlin.kt and
  // Java.java at runtime from the test resources directory.
  (androidTestSourceSet.resources as org.gradle.api.tasks.util.PatternFilterable)
    .setExcludes(emptySet())
}

dependencies {
  androidTestImplementation(projects.core)
  androidTestImplementation(projects.languages.kotlin)
  androidTestImplementation(projects.languages.swift)
  androidTestImplementation(projects.languages.ruby)
  androidTestImplementation(projects.languages.rust)
  androidTestImplementation(projects.languages.python)
  androidTestImplementation(projects.languages.go)
  androidTestImplementation(projects.languages.java)
  androidTestImplementation(libs.androidx.benchmark.junit4)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.testExt.junit)
  androidTestImplementation(libs.compose.ui)
}
