package io.github.mataku.compose.highlight.material3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.core.LocalSyntaxTheme
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.rememberHighlightedString

/**
 * Renders [code] as syntax-highlighted Material3 [Text] using the parser and highlight query
 * carried by [language].
 *
 * When [SyntaxTheme.background] is non-null, it is painted behind the text. Layering, from outside
 * to inside, is `modifier` → background → [contentPadding] → text — so [modifier]'s padding sits
 * outside the background, while [contentPadding] insets the text within the background. Disable
 * the background by passing `theme.copy(background = null)`.
 *
 * @param contentPadding Padding applied between the background and the text. Defaults to no inset;
 *   pass e.g. `PaddingValues(16.dp)` for a typical code-block look.
 * @param theme Maps tree-sitter capture names to text styles. Defaults to the value provided by
 *   [LocalSyntaxTheme] (which itself defaults to [SyntaxTheme.DarkDefault]).
 * @param style Base [TextStyle] for the rendered text. Defaults to the ambient
 *   [androidx.compose.material3.LocalTextStyle] forced to [FontFamily.Monospace].
 * @param selectable When true (the default), the rendered text is wrapped in a
 *   [SelectionContainer] so users can highlight the code with the platform's native
 *   text-selection UI (which also surfaces a Copy action). Pass `false` to render without a
 *   selection scope.
 */
@Composable
fun SyntaxHighlightedText(
  code: String,
  language: Language,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  theme: SyntaxTheme = LocalSyntaxTheme.current,
  style: TextStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
  selectable: Boolean = true,
) {
  val backgroundModifier = theme.background?.let { Modifier.background(it) } ?: Modifier
  val text: @Composable () -> Unit = {
    Text(
      text = rememberHighlightedString(code, language, theme),
      modifier = modifier.then(backgroundModifier).padding(contentPadding),
      style = style,
    )
  }
  if (selectable) {
    SelectionContainer { text() }
  } else {
    text()
  }
}
