package io.github.mataku.compose.highlight.html

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.html.internal.TreeSitterHtml
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterHtml.language()), HIGHLIGHTS_QUERY)
}

val Languages.Html: Language get() = instance
