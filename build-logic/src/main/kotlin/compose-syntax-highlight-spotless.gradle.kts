plugins {
  id("com.diffplug.spotless")
}

private val ktlintOverrides = mapOf(
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
    target("*.gradle.kts")
    ktlint().editorConfigOverride(ktlintOverrides)
  }
}
