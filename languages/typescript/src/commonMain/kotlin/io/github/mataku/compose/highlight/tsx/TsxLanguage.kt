package io.github.mataku.compose.highlight.tsx

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.tsx.internal.TreeSitterTsx
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterTsx.language()), HIGHLIGHTS_QUERY)
}

val Languages.Tsx: Language get() = instance
