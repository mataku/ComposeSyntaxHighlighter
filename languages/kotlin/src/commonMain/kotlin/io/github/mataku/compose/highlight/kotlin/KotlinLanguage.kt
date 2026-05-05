package io.github.mataku.compose.highlight.kotlin

import io.github.mataku.compose.highlight.core.Language
import io.github.mataku.compose.highlight.core.Languages
import io.github.mataku.compose.highlight.core.kTreeSitterLanguage
import io.github.mataku.compose.highlight.kotlin.internal.TreeSitterKotlin
import io.github.treesitter.ktreesitter.Language as TsLanguage

val KotlinLanguage: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitterKotlin.language()), HIGHLIGHTS_QUERY)
}

val Languages.kotlin: Language get() = KotlinLanguage
