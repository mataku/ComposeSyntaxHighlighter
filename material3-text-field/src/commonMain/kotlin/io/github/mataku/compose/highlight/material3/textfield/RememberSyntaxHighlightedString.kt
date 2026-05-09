package io.github.mataku.compose.highlight.material3.textfield

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.AnnotatedString
import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.core.IncrementalHighlighter
import io.github.mataku.compose.highlight.core.LocalSyntaxTheme
import io.github.mataku.compose.highlight.core.SyntaxTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext

/**
 * Returns a [State] holding the syntax-highlighted form of `state.text`. The returned state
 * recomputes incrementally as the user edits via the underlying [IncrementalHighlighter].
 *
 * The engine is owned by this hook: one instance per `(state, language)` lifetime, closed on
 * recomposition leave or key change. `theme` changes do not rebuild the engine — the engine's
 * theme-only short-circuit handles them.
 *
 * Thread-safety: the engine is driven from a single coroutine on [Dispatchers.Default];
 * callers must not invoke `update`/`close` on a shared engine instance themselves.
 */
@Composable
fun rememberSyntaxHighlightedString(
  state: TextFieldState,
  language: Language,
  theme: SyntaxTheme = LocalSyntaxTheme.current,
): State<AnnotatedString> = rememberSyntaxHighlightedString(
  state = state,
  language = language,
  theme = theme,
  engineFactory = ::IncrementalHighlighter,
)

@Composable
internal fun rememberSyntaxHighlightedString(
  state: TextFieldState,
  language: Language,
  theme: SyntaxTheme,
  engineFactory: (Language) -> IncrementalHighlighter,
): State<AnnotatedString> = produceState(
  initialValue = AnnotatedString(state.text.toString()),
  key1 = state,
  key2 = language,
) {
  val engine = engineFactory(language)
  try {
    withContext(Dispatchers.Default) {
      combine(
        snapshotFlow { state.text.toString() },
        snapshotFlow { theme },
      ) { text, currentTheme -> text to currentTheme }
        .conflate()
        .collect { (text, currentTheme) ->
          value = engine.update(text, currentTheme)
        }
    }
  } finally {
    engine.close()
  }
}
