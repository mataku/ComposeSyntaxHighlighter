package io.github.mataku.compose.highlight.python

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.python.internal.TreeSitterPython
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterPython.language()), HIGHLIGHTS_QUERY)
}

val Languages.Python: Language get() = instance
