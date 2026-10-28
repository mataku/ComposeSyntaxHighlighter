package io.github.mataku.compose.highlight.css

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.css.internal.TreeSitterCss
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterCss.language()), HIGHLIGHTS_QUERY)
}

val Languages.Css: Language get() = instance
