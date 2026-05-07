package io.github.mataku.compose.highlight.go

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.go.internal.TreeSitterGo
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterGo.language()), HIGHLIGHTS_QUERY)
}

val Languages.Go: Language get() = instance
