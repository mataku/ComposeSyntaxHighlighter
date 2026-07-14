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
  ignoredProjects += listOf("composeApp", "androidApp", "jvm", "android")
  nonPublicMarkers += "io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi"
  ignoredPackages += listOf(
    "go",
    "java",
    "javascript",
    "kotlin",
    "markdown",
    "markdownInline",
    "python",
    "ruby",
    "rust",
    "swift",
    "tsx",
    "typescript",
  ).map { "io.github.mataku.compose.highlight.$it.internal" }
  @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
  klib {
    enabled = true
  }
}

dependencies {
  dokka(project(":core"))
  dokka(project(":core-api"))
  dokka(project(":material3"))
  dokka(project(":material3-text-field"))
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
