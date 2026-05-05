package io.github.mataku.compose.highlight.ruby

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.ruby.internal.TreeSitterRuby
import io.github.treesitter.ktreesitter.Language as TsLanguage

val RubyLanguage: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitterRuby.language()), HIGHLIGHTS_QUERY)
}

val Languages.ruby: Language get() = RubyLanguage
