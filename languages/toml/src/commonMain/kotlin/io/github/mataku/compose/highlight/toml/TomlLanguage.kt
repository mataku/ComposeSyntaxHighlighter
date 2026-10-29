package io.github.mataku.compose.highlight.toml

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.toml.internal.TreeSitterToml
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterToml.language()), HIGHLIGHTS_QUERY)
}

val Languages.Toml: Language get() = instance
