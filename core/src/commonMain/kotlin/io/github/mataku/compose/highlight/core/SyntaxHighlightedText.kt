package io.github.mataku.compose.highlight.core

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import io.github.mataku.compose.highlight.api.Language

/**
 * Renders [code] as syntax-highlighted Compose [Text] using the parser and highlight query
 * carried by [language].
 *
 * @param theme Maps tree-sitter capture names to text styles. Defaults to the value provided by
 *   [LocalSyntaxTheme] (which itself defaults to [SyntaxTheme.DarkDefault]).
 * @param style Base [TextStyle] for the rendered text. Defaults to the ambient
 *   [androidx.compose.material3.LocalTextStyle] forced to [FontFamily.Monospace].
 */
@Composable
fun SyntaxHighlightedText(
  code: String,
  language: Language,
  modifier: Modifier = Modifier,
  theme: SyntaxTheme = LocalSyntaxTheme.current,
  style: TextStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
) {
  Text(
    text = rememberHighlightedString(code, language, theme),
    modifier = modifier,
    style = style,
  )
}
