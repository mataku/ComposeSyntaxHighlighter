package io.github.mataku.compose.highlight.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import io.github.mataku.compose.highlight.api.Language

/**
 * Composable wrapper around [highlight] that memoizes the produced [AnnotatedString], keyed on
 * `(code, language, theme)`.
 */
@Composable
fun rememberHighlightedString(
  code: String,
  language: Language,
  theme: SyntaxTheme,
): AnnotatedString = remember(code, language, theme) {
  highlight(code, language, theme)
}

internal fun plainHighlightedString(code: String, theme: SyntaxTheme): AnnotatedString = buildAnnotatedString {
  append(code)
  if (theme.baseStyle != SpanStyle() && code.isNotEmpty()) {
    addStyle(theme.baseStyle, 0, code.length)
  }
}
