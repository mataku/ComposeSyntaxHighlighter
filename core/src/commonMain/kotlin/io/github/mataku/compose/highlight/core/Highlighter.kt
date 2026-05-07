package io.github.mataku.compose.highlight.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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
): AnnotatedString {
  val parser = Parser(language.parser)
  val tree = parser.parse(code)
  val query = language.query
  val byteToChar = Utf8ByteIndex(code)
  val emptySpan = SpanStyle()

  return buildAnnotatedString {
    append(code)
    if (theme.baseStyle != emptySpan) {
      addStyle(theme.baseStyle, 0, code.length)
    }
    query.captures(tree.rootNode).forEach { (_, match) ->
      match.captures.forEach { capture ->
        val style = theme.resolve(capture.name) ?: return@forEach
        val start = byteToChar.charIndexAt(capture.node.startByte.toInt())
        val end = byteToChar.charIndexAt(capture.node.endByte.toInt())
        if (start < end) addStyle(style, start, end)
      }
    }
  }
}
