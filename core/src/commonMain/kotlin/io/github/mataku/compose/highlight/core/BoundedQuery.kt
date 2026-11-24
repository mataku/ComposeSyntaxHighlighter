package io.github.mataku.compose.highlight.core

import io.github.treesitter.ktreesitter.Node
import io.github.treesitter.ktreesitter.Query
import io.github.treesitter.ktreesitter.QueryCursor

/**
 * Runs this query on [node] within the bounds every highlight uses.
 *
 * [QUERY_MATCH_LIMIT] caps how many matches the cursor holds at once; some queries otherwise hold a number of matches that grows with how deeply the code is nested.
 * [QUERY_MAX_START_DEPTH] keeps the cursor from starting matches, and from descending, deeper than that many levels of the syntax tree.
 * Query time can grow with the square of the nesting depth for some grammars, and the query cursor is designed for trees up to 65,535 levels deep.
 * Ordinary code stays well within both bounds; parts of an input nested deeper than [QUERY_MAX_START_DEPTH] are left unhighlighted.
 */
internal fun Query.boundedCursor(node: Node): QueryCursor = this(node).apply {
  matchLimit = QUERY_MATCH_LIMIT
  maxStartDepth = QUERY_MAX_START_DEPTH
}

internal const val QUERY_MATCH_LIMIT: UInt = 256u

internal const val QUERY_MAX_START_DEPTH: UInt = 2_000u
