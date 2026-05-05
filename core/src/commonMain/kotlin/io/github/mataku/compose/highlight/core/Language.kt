package io.github.mataku.compose.highlight.core

import io.github.treesitter.ktreesitter.Language as TsLanguage

class Language internal constructor(
    internal val parser: TsLanguage,
    internal val highlightsQuery: String,
)

fun kTreeSitterLanguage(parser: TsLanguage, highlightsQuery: String): Language =
    Language(parser, highlightsQuery)
