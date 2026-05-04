package io.github.mataku.compose.syntax.language.kotlin

import io.github.mataku.compose.syntax.core.Language
import io.github.mataku.compose.syntax.language.kotlin.internal.TreeSitterKotlin
import io.github.treesitter.ktreesitter.Language as TsLanguage

object KotlinLanguage : Language {
    override val parser: TsLanguage by lazy { TsLanguage(TreeSitterKotlin.language()) }
    override val highlightsQuery: String = HIGHLIGHTS_QUERY
}
