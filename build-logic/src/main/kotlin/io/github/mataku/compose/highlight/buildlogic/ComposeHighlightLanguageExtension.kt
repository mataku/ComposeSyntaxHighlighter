package io.github.mataku.compose.highlight.buildlogic

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class ComposeHighlightLanguageExtension {
  abstract val languageName: Property<String>
  abstract val grammarSubmodulePath: Property<String>
  abstract val parserClassName: Property<String>
  abstract val sources: ListProperty<String>
  abstract val queries: ListProperty<String>
  abstract val licenseSpdx: Property<String>
  abstract val licenseSource: Property<String>
}
