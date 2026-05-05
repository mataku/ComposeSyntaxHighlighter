package io.github.mataku.compose.highlight.ruby

import io.github.mataku.compose.highlight.core.Language
import io.github.mataku.compose.highlight.core.kTreeSitterLanguage
import io.github.mataku.compose.highlight.ruby.internal.TreeSitterRuby
import io.github.treesitter.ktreesitter.Language as TsLanguage

val RubyLanguage: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitterRuby.language()), HIGHLIGHTS_QUERY)
}
