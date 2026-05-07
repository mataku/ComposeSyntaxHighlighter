package io.github.mataku.compose.highlight.api

import androidx.compose.runtime.Immutable
import io.github.treesitter.ktreesitter.Query
import io.github.treesitter.ktreesitter.Language as TsLanguage

@Immutable
class Language @InternalSyntaxHighlightApi constructor(
  @property:InternalSyntaxHighlightApi val parser: TsLanguage,
  @property:InternalSyntaxHighlightApi val highlightsQuery: String,
  @property:InternalSyntaxHighlightApi val query: Query,
)

@InternalSyntaxHighlightApi
fun kTreeSitterLanguage(parser: TsLanguage, highlightsQuery: String): Language {
  val query = parser.query(highlightsQuery)
  return Language(parser, highlightsQuery, query)
}
