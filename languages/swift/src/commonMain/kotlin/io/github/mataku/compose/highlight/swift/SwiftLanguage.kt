package io.github.mataku.compose.highlight.swift

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.swift.internal.TreeSitterSwift
import io.github.treesitter.ktreesitter.Language as TsLanguage

val SwiftLanguage: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitterSwift.language()), HIGHLIGHTS_QUERY)
}

val Languages.swift: Language get() = SwiftLanguage
