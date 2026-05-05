package io.github.mataku.compose.highlight.rust

import io.github.mataku.compose.highlight.core.Language
import io.github.mataku.compose.highlight.core.kTreeSitterLanguage
import io.github.mataku.compose.highlight.rust.internal.TreeSitterRust
import io.github.treesitter.ktreesitter.Language as TsLanguage

val RustLanguage: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitterRust.language()), HIGHLIGHTS_QUERY)
}
