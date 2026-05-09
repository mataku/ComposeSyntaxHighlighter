package io.github.mataku.compose.highlight.material3.textfield

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.core.LocalSyntaxTheme
import io.github.mataku.compose.highlight.core.SyntaxTheme

/**
 * Renders an editable, syntax-highlighted code surface backed by [IncrementalHighlighter].
 *
 * Internally layers a transparent [BasicTextField] beneath a [Text] that paints the highlighted
 * form, sharing the same [TextStyle] and [ScrollState] so glyphs in both children sit at
 * identical positions. The [BasicTextField] owns cursor, selection, and IME; the overlay
 * [Text] owns visible colour.
 *
 * For non-Material3 chrome or custom layouts, use [rememberSyntaxHighlightedString] directly
 * and compose your own overlay.
 *
 * @param state       Source of truth for the editable text.
 * @param language    Tree-sitter [Language] driving the highlighting.
 * @param theme       Maps capture names to [androidx.compose.ui.text.SpanStyle]s. Defaults to
 *                    the value of [LocalSyntaxTheme].
 * @param textStyle   Base text style. The overlay [Text] uses it as-is; the underlying
 *                    [BasicTextField] copies it with [Color.Transparent] so its glyphs
 *                    remain invisible while still measuring.
 * @param cursorBrush Caret brush. Defaults to the Material3 primary colour.
 * @param lineLimits  Defaults to [TextFieldLineLimits.MultiLine] — code-editor flow.
 * @param scrollState Vertical scroll. The same instance is shared by overlay and underlay.
 */
@Composable
fun SyntaxHighlightedTextField(
  state: TextFieldState,
  language: Language,
  modifier: Modifier = Modifier,
  theme: SyntaxTheme = LocalSyntaxTheme.current,
  textStyle: TextStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
  enabled: Boolean = true,
  readOnly: Boolean = false,
  cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.primary),
  lineLimits: TextFieldLineLimits = TextFieldLineLimits.MultiLine(),
  scrollState: ScrollState = rememberScrollState(),
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  onKeyboardAction: KeyboardActionHandler? = null,
  interactionSource: MutableInteractionSource? = null,
) {
  val highlighted by rememberSyntaxHighlightedString(
    state = state,
    language = language,
    theme = theme,
  )
  Box(modifier) {
    Text(
      text = highlighted,
      style = textStyle,
      modifier = Modifier
        .matchParentSize()
        .verticalScroll(scrollState),
    )
    BasicTextField(
      state = state,
      modifier = Modifier.matchParentSize(),
      enabled = enabled,
      readOnly = readOnly,
      textStyle = textStyle.copy(color = Color.Transparent),
      keyboardOptions = keyboardOptions,
      onKeyboardAction = onKeyboardAction,
      lineLimits = lineLimits,
      interactionSource = interactionSource,
      cursorBrush = cursorBrush,
      scrollState = scrollState,
    )
  }
}
