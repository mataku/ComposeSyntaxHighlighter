package io.github.mataku.compose.highlight.typescript

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.typescript.internal.TreeSitterTypescript
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterTypescript.language()), HIGHLIGHTS_QUERY)
}

val Languages.Typescript: Language get() = instance
