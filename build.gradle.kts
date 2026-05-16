plugins {
  // this is necessary to avoid the plugins to be loaded multiple times
  // in each subproject's classloader
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.composeMultiplatform) apply false
  alias(libs.plugins.composeCompiler) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.vanniktechPublish) apply false
  alias(libs.plugins.dokka)
  alias(libs.plugins.binaryCompatibilityValidator)
  id("compose-syntax-highlight-spotless")
}

apiValidation {
  ignoredProjects += listOf("composeApp", "androidApp", "benchmark", "benchmark-android")
  nonPublicMarkers += "io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi"
}

dependencies {
  dokka(project(":core"))
  dokka(project(":core-api"))
  dokka(project(":material3"))
  dokka(project(":material3-text-field"))
  // :languages:* are temporarily excluded during AGP 9 Stage 1 / Stage 2.
  // Re-add when modules are restored. See
  // docs/plans/2026-05-16-agp-9-phase1-plan.md Task 2.5.
  dokka(project(":languages:kotlin"))
  dokka(project(":languages:swift"))
  dokka(project(":languages:ruby"))
  dokka(project(":languages:rust"))
  dokka(project(":languages:python"))
  dokka(project(":languages:go"))
  dokka(project(":languages:java"))
  dokka(project(":languages:markdown"))
  dokka(project(":languages:javascript"))
  dokka(project(":languages:typescript"))
}
