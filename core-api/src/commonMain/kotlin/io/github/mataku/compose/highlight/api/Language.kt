package io.github.mataku.compose.highlight.api

import androidx.compose.runtime.Immutable
import io.github.treesitter.ktreesitter.Query
import io.github.treesitter.ktreesitter.Language as TsLanguage

@Immutable
class Language @InternalSyntaxHighlightApi constructor(
  @property:InternalSyntaxHighlightApi val parser: TsLanguage,
  @property:InternalSyntaxHighlightApi val highlightsQuery: String,
  @property:InternalSyntaxHighlightApi val query: Query,
  @property:InternalSyntaxHighlightApi val injections: List<Injection> = emptyList(),
)

@Immutable
class Injection @InternalSyntaxHighlightApi constructor(
  @property:InternalSyntaxHighlightApi val injectionsQuery: Query,
  @property:InternalSyntaxHighlightApi val injectionsQuerySource: String,
  @property:InternalSyntaxHighlightApi val target: Language,
)

@InternalSyntaxHighlightApi
fun kTreeSitterLanguage(parser: TsLanguage, highlightsQuery: String): Language {
  val query = parser.query(highlightsQuery)
  return Language(parser, highlightsQuery, query)
}

@InternalSyntaxHighlightApi
fun kTreeSitterLanguage(
  parser: TsLanguage,
  highlightsQuery: String,
  injections: List<Injection>,
): Language {
  val query = parser.query(highlightsQuery)
  return Language(parser, highlightsQuery, query, injections)
}
