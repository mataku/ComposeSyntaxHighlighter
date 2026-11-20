package io.github.mataku.compose.highlight.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Language
import io.github.treesitter.ktreesitter.Parser

/**
 * Returns an [AnnotatedString] of [code] with [theme] styles applied to tree-sitter capture
 * spans defined by [language]. Each capture name is resolved through [SyntaxTheme.resolve],
 * which falls back along dotted prefixes (e.g. `string.escape` -> `string`) before yielding
 * `null`.
 *
 * Constructs a fresh tree-sitter [Parser] on every call. Compose callers should prefer
 * [rememberHighlightedString] or [SyntaxHighlightedText], both of which memoize the result.
 */
fun highlight(
  code: String,
  language: Language,
  theme: SyntaxTheme,
): AnnotatedString = styledString(code, captureSpans(code, language), theme)

/**
 * Parses [code] and returns its theme-independent capture spans in application order.
 * Offsets are UTF-16 offsets into [code].
 */
internal fun captureSpans(code: String, language: Language): List<HighlightSpan> {
  val tree = Parser(language.parser).parse(code)
  val spans = ArrayList<HighlightSpan>()
  collectCaptures(code, tree, language) { start, end, name ->
    spans.add(HighlightSpan(start, end, name))
  }
  return spans
}

/**
 * Applies [SyntaxTheme.baseStyle] over the whole [text], then each of [spans] in order, so later spans merge over earlier ones.
 * Spans whose capture does not resolve are skipped.
 */
internal fun styledString(
  text: String,
  spans: List<HighlightSpan>,
  theme: SyntaxTheme,
): AnnotatedString {
  val ranges = ArrayList<AnnotatedString.Range<SpanStyle>>(spans.size + 1)
  if (theme.baseStyle != SpanStyle() && text.isNotEmpty()) {
    ranges.add(AnnotatedString.Range(theme.baseStyle, 0, text.length))
  }
  for (span in spans) {
    val style = theme.resolve(span.captureName) ?: continue
    ranges.add(AnnotatedString.Range(style, span.start, span.end))
  }
  return AnnotatedString(text = text, spanStyles = ranges, paragraphStyles = emptyList())
}
