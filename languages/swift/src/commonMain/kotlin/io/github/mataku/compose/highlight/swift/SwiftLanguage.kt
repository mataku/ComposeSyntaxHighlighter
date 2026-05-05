package io.github.mataku.compose.highlight.swift

import io.github.mataku.compose.highlight.core.Language
import io.github.mataku.compose.highlight.core.Languages
import io.github.mataku.compose.highlight.core.kTreeSitterLanguage
import io.github.mataku.compose.highlight.swift.internal.TreeSitterSwift
import io.github.treesitter.ktreesitter.Language as TsLanguage

val SwiftLanguage: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitterSwift.language()), HIGHLIGHTS_QUERY)
}

val Languages.swift: Language get() = SwiftLanguage
