package io.github.mataku.compose.syntax.core

import io.github.treesitter.ktreesitter.Language as TsLanguage

interface Language {
    val parser: TsLanguage
    val highlightsQuery: String
}
