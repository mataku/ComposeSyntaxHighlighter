package io.github.mataku.compose.highlight.buildlogic

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

abstract class ComposeSyntaxHighlightLanguageExtension {
  abstract val languageName: Property<String>
  abstract val grammars: NamedDomainObjectContainer<GrammarSpec>
  abstract val licenseSpdx: Property<String>
  abstract val licenseSource: Property<String>
  abstract val licenseCopyright: Property<String>
}
