package io.github.mataku.compose.highlight.material3.textfield

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.input.TextFieldValue
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.kotlin.Kotlin
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SyntaxHighlightedTextFieldTest {

  @Test
  fun rendersAndAcceptsEdits() = runComposeUiTest {
    var value = TextFieldValue("val a = 1")
    setContent {
      MaterialTheme {
        SyntaxHighlightedTextField(
          value = value,
          onValueChange = { value = it },
          language = Languages.Kotlin,
          theme = SyntaxTheme.DarkDefault,
        )
      }
    }
    waitForIdle()
    value = TextFieldValue("fun f() {}")
    waitForIdle()
    assertEquals("fun f() {}", value.text)
  }
}
