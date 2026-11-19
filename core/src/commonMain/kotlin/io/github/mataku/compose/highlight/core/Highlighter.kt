package io.github.mataku.compose.highlight.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import io.github.mataku.compose.highlight.api.Language
import io.github.treesitter.ktreesitter.Parser
import io.github.treesitter.ktreesitter.Tree

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
  return applyStyles(code, tree, language, theme)
}

/**
 * Builds the styled [AnnotatedString] for [code] given a pre-parsed [tree]. Internal entry
 * point shared by [highlight] (one-shot) and the Composable wrappers (which cache the tree
 * across theme-only changes).
 */
internal fun applyStyles(
  code: String,
  tree: Tree,
  language: Language,
  theme: SyntaxTheme,
): AnnotatedString {
  val emptySpan = SpanStyle()

  return buildAnnotatedString {
    append(code)
    if (theme.baseStyle != emptySpan && code.isNotEmpty()) {
      addStyle(theme.baseStyle, 0, code.length)
    }
    collectCaptures(code, tree, language) { start, end, name ->
      theme.resolve(name)?.let { addStyle(it, start, end) }
    }
  }
}
