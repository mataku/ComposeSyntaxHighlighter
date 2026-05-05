package io.github.mataku.compose.syntax.language.swift

import io.github.mataku.compose.syntax.core.Language
import io.github.mataku.compose.syntax.core.kTreeSitterLanguage
import io.github.mataku.compose.syntax.language.swift.internal.TreeSitterSwift
import io.github.treesitter.ktreesitter.Language as TsLanguage

val SwiftLanguage: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitterSwift.language()), HIGHLIGHTS_QUERY)
}
