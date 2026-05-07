package io.github.mataku.compose.highlight.rust

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.rust.internal.TreeSitterRust
import io.github.treesitter.ktreesitter.Language as TsLanguage

val RustLanguage: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterRust.language()), HIGHLIGHTS_QUERY)
}

val Languages.rust: Language get() = RustLanguage
