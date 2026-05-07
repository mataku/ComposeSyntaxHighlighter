package io.github.mataku.compose.highlight.swift

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.swift.internal.TreeSitterSwift
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterSwift.language()), HIGHLIGHTS_QUERY)
}

val Languages.Swift: Language get() = instance
