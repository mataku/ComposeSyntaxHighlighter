package io.github.mataku.compose.highlight.material3.textfield

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.kotlin.Kotlin
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SyntaxHighlightedTextFieldTest {

  @Test
  fun rendersAndAcceptsEdits() = runComposeUiTest {
    val state = TextFieldState(initialText = "val a = 1")
    setContent {
      MaterialTheme {
        SyntaxHighlightedTextField(
          state = state,
          language = Languages.Kotlin,
          theme = SyntaxTheme.DarkDefault,
        )
      }
    }
    waitForIdle()
    state.edit { replace(0, length, "fun f() {}") }
    waitForIdle()
    assertEquals("fun f() {}", state.text.toString())
  }
}
