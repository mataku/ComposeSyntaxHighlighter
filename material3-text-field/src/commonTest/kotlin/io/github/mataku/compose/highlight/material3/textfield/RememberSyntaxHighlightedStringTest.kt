package io.github.mataku.compose.highlight.material3.textfield

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.IncrementalHighlighter
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.kotlin.Kotlin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RememberSyntaxHighlightedStringTest {

  @Test
  fun textEditPropagatesToHighlightedState() = runComposeUiTest {
    val state = TextFieldState(initialText = "val a = 1")
    var capturedHighlighted: AnnotatedString? = null
    setContent {
      val highlighted by rememberSyntaxHighlightedString(
        state = state,
        language = Languages.Kotlin,
        theme = SyntaxTheme.DarkDefault,
      )
      capturedHighlighted = highlighted
    }
    waitForIdle()
    state.edit { replace(0, length, "val b = 2") }
    waitUntil(timeoutMillis = 2_000) {
      capturedHighlighted?.text == "val b = 2"
    }
    assertEquals("val b = 2", capturedHighlighted?.text)
    assertTrue((capturedHighlighted?.spanStyles?.size ?: 0) > 0, "expected styled spans")
  }

  @Test
  fun themeChangeDoesNotRebuildEngine() = runComposeUiTest {
    val state = TextFieldState(initialText = "val a = 1")
    var constructionCount = 0
    val factory: (Language) -> IncrementalHighlighter = { language ->
      constructionCount++
      IncrementalHighlighter(language)
    }

    var theme by mutableStateOf(SyntaxTheme.DarkDefault)
    var capturedHighlighted: AnnotatedString? = null
    setContent {
      val highlighted by rememberSyntaxHighlightedString(
        state = state,
        language = Languages.Kotlin,
        theme = theme,
        engineFactory = factory,
      )
      capturedHighlighted = highlighted
    }
    waitForIdle()
    waitUntil(timeoutMillis = 2_000) { capturedHighlighted?.text == "val a = 1" }
    val constructionsAfterFirst = constructionCount

    theme = SyntaxTheme.LightDefault
    waitForIdle()
    waitUntil(timeoutMillis = 2_000) {
      capturedHighlighted?.spanStyles?.isNotEmpty() == true
    }

    assertEquals(constructionsAfterFirst, constructionCount, "theme change must not rebuild engine")
  }
}
