package io.github.mataku.compose.highlight.buildlogic

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class ComposeSyntaxHighlightLanguageExtension {
  abstract val languageName: Property<String>
  abstract val grammars: NamedDomainObjectContainer<GrammarSpec>

  // Legacy top-level properties — kept until Task 4 migrates all modules to `grammars { ... }`.
  // Remove with their callers in Task 4.
  abstract val grammarSubmodulePath: Property<String>
  abstract val parserClassName: Property<String>
  abstract val sources: ListProperty<String>
  abstract val queries: ListProperty<String>

  abstract val licenseSpdx: Property<String>
  abstract val licenseSource: Property<String>

  /**
   * C symbol exported by the grammar's `parser.c`, used to override the default
   * `tree_sitter_${languageName}` JNI binding. Set this when the upstream grammar's C
   * identifier differs from [languageName] — e.g. when a camelCase Gradle module name
   * (`markdownInline`) maps to a snake_case upstream symbol (`tree_sitter_markdown_inline`).
   * Leave unset to inherit the ktreesitter plugin default.
   */
  abstract val cSymbol: Property<String>
}
