plugins {
  `kotlin-dsl`
  alias(libs.plugins.spotless)
}

repositories {
  google()
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(libs.plugins.kotlinMultiplatform.toGradle())
  implementation(libs.plugins.androidLibrary.toGradle())
  implementation(libs.plugins.ktreesitter.toGradle())
  implementation(libs.plugins.composeMultiplatform.toGradle())
  implementation(libs.plugins.composeCompiler.toGradle())
  implementation(libs.plugins.vanniktechPublish.toGradle())
  implementation(libs.plugins.dokka.toGradle())
  implementation(libs.plugins.spotless.toGradle())
  testImplementation(embeddedKotlin("test"))
  testImplementation(embeddedKotlin("test-junit"))
}

fun org.gradle.plugin.use.PluginDependency.toGradle(): String = "$pluginId:$pluginId.gradle.plugin:${version.requiredVersion}"

fun Provider<org.gradle.plugin.use.PluginDependency>.toGradle(): String = get().toGradle()

val ktlintOverrides = mapOf(
  "ktlint_standard_filename" to "disabled",
  "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
)

spotless {
  kotlin {
    target("src/**/*.kt")
    targetExclude("**/build/**", "**/generated/**")
    ktlint().editorConfigOverride(ktlintOverrides)
  }
  kotlinGradle {
    target("*.gradle.kts", "src/**/*.gradle.kts")
    ktlint().editorConfigOverride(ktlintOverrides)
  }
}
