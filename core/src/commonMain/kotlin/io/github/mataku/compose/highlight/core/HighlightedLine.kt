package io.github.mataku.compose.highlight.core

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

/**
 * A theme-independent highlight range within a single [HighlightedLine].
 *
 * [start] and [end] are UTF-16 offsets into [HighlightedLine.text].
 * [end] is exclusive, and `start < end` holds for every span the library produces.
 * [captureName] is the tree-sitter capture name that is resolved against a [SyntaxTheme] only when the line is rendered.
 */
@Immutable
class HighlightSpan(
  val start: Int,
  val end: Int,
  val captureName: String,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is HighlightSpan) return false
    if (start != other.start) return false
    if (end != other.end) return false
    if (captureName != other.captureName) return false
    return true
  }

  override fun hashCode(): Int {
    var result = start
    result = 31 * result + end
    result = 31 * result + captureName.hashCode()
    return result
  }

  override fun toString(): String = "HighlightSpan(start=$start, end=$end, captureName=$captureName)"
}

/**
 * One line of source text together with its theme-independent highlight [spans].
 *
 * Span offsets are UTF-16 offsets into [text].
 * [spans] is in application order: spans may overlap, and a later span merges over an earlier one with Compose `addStyle` semantics, so the order must not be changed.
 * Rendering through [toAnnotatedString] never reads [SyntaxTheme.background].
 */
@Immutable
class HighlightedLine(
  val lineNumber: Int,
  val text: String,
  val spans: List<HighlightSpan>,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is HighlightedLine) return false
    if (lineNumber != other.lineNumber) return false
    if (text != other.text) return false
    if (spans != other.spans) return false
    return true
  }

  override fun hashCode(): Int {
    var result = lineNumber
    result = 31 * result + text.hashCode()
    result = 31 * result + spans.hashCode()
    return result
  }

  override fun toString(): String = "HighlightedLine(lineNumber=$lineNumber, text=$text, spans=$spans)"
}

/**
 * Resolves this line's spans against [theme] and returns the styled [AnnotatedString].
 *
 * [SyntaxTheme.baseStyle] is applied first over the whole text, then each span in [HighlightedLine.spans] order, so later spans merge over earlier ones (Compose `addStyle` semantics).
 * Offsets are UTF-16.
 * Spans whose capture does not resolve are skipped.
 * [SyntaxTheme.background] is never read.
 */
fun HighlightedLine.toAnnotatedString(theme: SyntaxTheme): AnnotatedString {
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
