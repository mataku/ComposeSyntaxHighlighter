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
  // :composeApp and :benchmark are temporarily excluded from settings during AGP 9
  // Stage 1 / Stage 2; restore these ignored entries when the modules are re-included.
  // See docs/plans/2026-05-16-agp-9-phase1-plan.md Task 2.5.
  // ignoredProjects += listOf("composeApp", "benchmark")
  nonPublicMarkers += "io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi"
}

dependencies {
  dokka(project(":core"))
  dokka(project(":core-api"))
  // dokka(project(":material3"))  // re-enabled by Task 8
  // dokka(project(":material3-text-field"))  // re-enabled by Task 7
  // :languages:* are temporarily excluded during AGP 9 Stage 1 / Stage 2.
  // Re-add when modules are restored. See
  // docs/plans/2026-05-16-agp-9-phase1-plan.md Task 2.5.
  // dokka(project(":languages:kotlin"))
  // dokka(project(":languages:swift"))
  // dokka(project(":languages:ruby"))
  // dokka(project(":languages:rust"))
  // dokka(project(":languages:python"))
  // dokka(project(":languages:go"))
  // dokka(project(":languages:java"))
}
