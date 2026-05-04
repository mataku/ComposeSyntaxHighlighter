package io.github.mataku.compose.syntax.language.kotlin

import io.github.mataku.compose.syntax.core.Language
import io.github.mataku.compose.syntax.core.kTreeSitterLanguage
import io.github.mataku.compose.syntax.language.kotlin.internal.TreeSitterKotlin
import io.github.treesitter.ktreesitter.Language as TsLanguage

val KotlinLanguage: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitterKotlin.language()), HIGHLIGHTS_QUERY)
}
