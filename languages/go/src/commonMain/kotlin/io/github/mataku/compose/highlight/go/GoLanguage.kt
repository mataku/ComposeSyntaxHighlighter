package io.github.mataku.compose.highlight.go

import io.github.mataku.compose.highlight.core.Language
import io.github.mataku.compose.highlight.core.kTreeSitterLanguage
import io.github.mataku.compose.highlight.go.internal.TreeSitterGo
import io.github.treesitter.ktreesitter.Language as TsLanguage

val GoLanguage: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitterGo.language()), HIGHLIGHTS_QUERY)
}
