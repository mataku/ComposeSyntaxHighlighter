package io.github.mataku.compose.highlight.material3.textfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.core.IncrementalHighlighter
import io.github.mataku.compose.highlight.core.LocalSyntaxTheme
import io.github.mataku.compose.highlight.core.SyntaxTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext

/**
 * Overlays the cached highlight [highlighted] onto the field's current text.
 *
 * The cached spans were computed for a possibly-stale snapshot of the text; the field's
 * live text is authoritative. Each cached span range is clamped to the live text length,
 * empty ranges are dropped, and the character count is preserved so [OffsetMapping.Identity]
 * keeps caret and selection geometry exact even while the highlight lags.
 */
internal class ClampingSyntaxVisualTransformation(
  private val highlighted: AnnotatedString,
) : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val length = text.text.length
    val spans = highlighted.spanStyles.mapNotNull { range ->
      val start = range.start.coerceIn(0, length)
      val end = range.end.coerceIn(start, length)
      if (start >= end) null else AnnotatedString.Range(range.item, start, end)
    }
    return TransformedText(
      AnnotatedString(text = text.text, spanStyles = spans),
      OffsetMapping.Identity,
    )
  }
}

/**
 * Returns a [State] holding the syntax-highlighted form of [text], recomputed incrementally
 * as [text] changes via the underlying [IncrementalHighlighter].
 *
 * The engine is owned per [language] lifetime (not per keystroke): [produceState] is keyed on
 * [language] so the engine survives edits and reuses tree-sitter incremental parse. Text and
 * theme changes are fed through [rememberUpdatedState] + [snapshotFlow]; a [language] change
 * restarts the producer and rebuilds the engine. The engine is driven from a single coroutine
 * on [Dispatchers.Default] and closed on leave / key change.
 */
@Composable
internal fun rememberSyntaxSpans(
  text: String,
  language: Language,
  theme: SyntaxTheme,
  engineFactory: (Language) -> IncrementalHighlighter = ::IncrementalHighlighter,
): State<AnnotatedString> {
  val textState = rememberUpdatedState(text)
  val themeState = rememberUpdatedState(theme)
  return produceState(
    initialValue = AnnotatedString(text),
    key1 = language,
  ) {
    val engine = engineFactory(language)
    try {
      withContext(Dispatchers.Default) {
        combine(
          snapshotFlow { textState.value },
          snapshotFlow { themeState.value },
        ) { currentText, currentTheme -> currentText to currentTheme }
          .conflate()
          .collect { (currentText, currentTheme) ->
            value = engine.update(currentText, currentTheme)
          }
      }
    } finally {
      engine.close()
    }
  }
}

/**
 * Returns a [VisualTransformation] that paints syntax highlighting onto a legacy
 * `BasicTextField` whose text equals [text]. Pass it straight to `BasicTextField`'s
 * `visualTransformation` parameter; the clamp against the live text is internal, so a
 * stale highlight can never produce an out-of-range offset.
 */
@Composable
fun rememberSyntaxHighlightVisualTransformation(
  text: String,
  language: Language,
  theme: SyntaxTheme = LocalSyntaxTheme.current,
): VisualTransformation {
  val highlighted by rememberSyntaxSpans(text = text, language = language, theme = theme)
  return remember(highlighted) { ClampingSyntaxVisualTransformation(highlighted) }
}
