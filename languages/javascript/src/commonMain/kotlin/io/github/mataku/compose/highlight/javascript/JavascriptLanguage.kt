package io.github.mataku.compose.highlight.javascript

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.javascript.internal.TreeSitterJavascript
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterJavascript.language()), HIGHLIGHTS_QUERY)
}

val Languages.Javascript: Language get() = instance
