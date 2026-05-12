package io.github.mataku.compose.highlight.buildlogic

import org.gradle.api.Named
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class GrammarSpec @Inject constructor(private val grammarName: String) : Named {
  override fun getName(): String = grammarName

  abstract val submodulePath: Property<String>
  abstract val parserClassName: Property<String>
  abstract val sources: ListProperty<String>
  abstract val queries: ListProperty<String>

  /**
   * C symbol exported by `parser.c`, used when the upstream grammar's C identifier differs
   * from [name]. Leave unset to inherit `tree_sitter_<name_snake_case>`.
   */
  abstract val cSymbol: Property<String>
}
