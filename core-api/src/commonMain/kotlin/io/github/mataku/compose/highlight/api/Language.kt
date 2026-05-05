package io.github.mataku.compose.highlight.api

import io.github.treesitter.ktreesitter.Language as TsLanguage

class Language @InternalSyntaxHighlightApi constructor(
    @property:InternalSyntaxHighlightApi val parser: TsLanguage,
    @property:InternalSyntaxHighlightApi val highlightsQuery: String,
)

@InternalSyntaxHighlightApi
fun kTreeSitterLanguage(parser: TsLanguage, highlightsQuery: String): Language =
    Language(parser, highlightsQuery)
