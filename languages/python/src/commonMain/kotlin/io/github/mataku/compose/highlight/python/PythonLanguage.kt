package io.github.mataku.compose.highlight.python

import io.github.mataku.compose.highlight.core.Language
import io.github.mataku.compose.highlight.core.kTreeSitterLanguage
import io.github.mataku.compose.highlight.python.internal.TreeSitterPython
import io.github.treesitter.ktreesitter.Language as TsLanguage

val PythonLanguage: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitterPython.language()), HIGHLIGHTS_QUERY)
}
