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
  id("compose-syntax-highlight-spotless")
}

dependencies {
  dokka(project(":core"))
  dokka(project(":core-api"))
  dokka(project(":languages:kotlin"))
  dokka(project(":languages:swift"))
  dokka(project(":languages:ruby"))
  dokka(project(":languages:rust"))
  dokka(project(":languages:python"))
  dokka(project(":languages:go"))
  dokka(project(":languages:java"))
}
