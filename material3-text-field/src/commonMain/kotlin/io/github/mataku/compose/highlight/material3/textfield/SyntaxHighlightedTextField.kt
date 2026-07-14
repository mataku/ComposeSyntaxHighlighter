package io.github.mataku.compose.highlight.material3.textfield

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.core.LocalSyntaxTheme
import io.github.mataku.compose.highlight.core.SyntaxTheme

/**
 * Renders an editable, syntax-highlighted code surface backed by [IncrementalHighlighter].
 *
 * A single [BasicTextField] owns the one and only text layout, cursor, selection, and IME.
 * Highlighting is painted onto that same layout by a [rememberSyntaxHighlightVisualTransformation],
 * so glyph metrics, caret position, and color are always measured from one layout — there is no
 * second layer to drift against. The highlight is computed asynchronously and may lag the caret
 * by a few frames; the caret position itself is always exact.
 *
 * For non-Material3 chrome or custom layouts, use [rememberSyntaxHighlightVisualTransformation]
 * directly on your own [BasicTextField].
 *
 * @param value        Source of truth for the editable text and selection.
 * @param onValueChange Invoked with the updated [TextFieldValue] on every edit.
 * @param language     Tree-sitter [Language] driving the highlighting.
 * @param theme        Maps capture names to [androidx.compose.ui.text.SpanStyle]s. Defaults to
 *                     the value of [LocalSyntaxTheme].
 * @param textStyle    Base text style; applied to the field and inherited by the highlight spans.
 * @param enabled      Whether the field accepts input. Defaults to `true`.
 * @param readOnly     Whether the field's contents can be modified. Defaults to `false`.
 * @param cursorBrush  Caret brush. Defaults to the Material3 primary colour.
 * @param singleLine   Defaults to `false` — code-editor flow.
 * @param maxLines     Defaults to unbounded.
 * @param minLines     Defaults to `1`.
 * @param keyboardActions Callbacks invoked for IME actions (e.g. Done, Next). Defaults to none.
 */
@Composable
fun SyntaxHighlightedTextField(
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  language: Language,
  modifier: Modifier = Modifier,
  theme: SyntaxTheme = LocalSyntaxTheme.current,
  textStyle: TextStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
  enabled: Boolean = true,
  readOnly: Boolean = false,
  cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.primary),
  singleLine: Boolean = false,
  maxLines: Int = Int.MAX_VALUE,
  minLines: Int = 1,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  interactionSource: MutableInteractionSource? = null,
) {
  val visualTransformation = rememberSyntaxHighlightVisualTransformation(
    text = value.text,
    language = language,
    theme = theme,
  )
  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    cursorBrush = cursorBrush,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    maxLines = maxLines,
    minLines = minLines,
    interactionSource = interactionSource,
  )
}
