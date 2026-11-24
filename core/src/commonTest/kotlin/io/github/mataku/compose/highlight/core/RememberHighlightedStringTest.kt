package io.github.mataku.compose.highlight.core

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.kotlin.Kotlin
import io.github.mataku.compose.highlight.python.Python
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class RememberHighlightedStringTest {

  private val kotlinCode = "fun main() { val a = 1 }"
  private val otherKotlinCode = "class Foo { fun bar() = \"baz\" }"
  private val pythonCode = "def main():\n    return 1\n"

  // A dedicated scheduler keeps the dispatcher independent of the clock that runComposeUiTest drives.
  private fun newDispatcher(): TestDispatcher = StandardTestDispatcher(TestCoroutineScheduler())

  @Test
  fun syncMatchesHighlight() = runComposeUiTest {
    var result: AnnotatedString? = null
    setContent {
      result = rememberHighlightedString(kotlinCode, Languages.Kotlin, SyntaxTheme.DarkDefault)
    }
    waitForIdle()
    assertEquals(highlight(kotlinCode, Languages.Kotlin, SyntaxTheme.DarkDefault), result)
  }

  @Test
  fun syncUpdatesWhenCodeChanges() = runComposeUiTest {
    var code by mutableStateOf(kotlinCode)
    var result: AnnotatedString? = null
    setContent {
      result = rememberHighlightedString(code, Languages.Kotlin, SyntaxTheme.DarkDefault)
    }
    waitForIdle()
    code = otherKotlinCode
    waitForIdle()
    assertEquals(highlight(otherKotlinCode, Languages.Kotlin, SyntaxTheme.DarkDefault), result)
  }

  @Test
  fun syncUpdatesWhenLanguageChanges() = runComposeUiTest {
    // The same source parsed by a different grammar yields different capture spans.
    var language: Language by mutableStateOf(Languages.Kotlin)
    var result: AnnotatedString? = null
    setContent {
      result = rememberHighlightedString(pythonCode, language, SyntaxTheme.DarkDefault)
    }
    waitForIdle()
    val before = result
    language = Languages.Python
    waitForIdle()
    val expected = highlight(pythonCode, Languages.Python, SyntaxTheme.DarkDefault)
    assertNotEquals(expected, before)
    assertEquals(expected, result)
  }

  @Test
  fun syncUpdatesWhenThemeChanges() = runComposeUiTest {
    var theme by mutableStateOf(SyntaxTheme.DarkDefault)
    var result: AnnotatedString? = null
    setContent {
      result = rememberHighlightedString(kotlinCode, Languages.Kotlin, theme)
    }
    waitForIdle()
    val before = result
    theme = SyntaxTheme.LightDefault
    waitForIdle()
    val expected = highlight(kotlinCode, Languages.Kotlin, SyntaxTheme.LightDefault)
    assertNotEquals(expected, before)
    assertEquals(expected, result)
  }

  @Test
  fun asyncStartsPlainThenHighlights() = runComposeUiTest {
    val dispatcher = newDispatcher()
    lateinit var state: State<AnnotatedString>
    setContent {
      state = rememberHighlightedStringAsync(kotlinCode, Languages.Kotlin, SyntaxTheme.DarkDefault, dispatcher)
    }
    waitForIdle()
    assertEquals(plainHighlightedString(kotlinCode, SyntaxTheme.DarkDefault), state.value)

    dispatcher.scheduler.advanceUntilIdle()
    waitForIdle()
    assertEquals(highlight(kotlinCode, Languages.Kotlin, SyntaxTheme.DarkDefault), state.value)
  }

  @Test
  fun asyncResetsToPlainOnCodeChangeThenHighlights() = runComposeUiTest {
    val dispatcher = newDispatcher()
    var code by mutableStateOf(kotlinCode)
    lateinit var state: State<AnnotatedString>
    setContent {
      state = rememberHighlightedStringAsync(code, Languages.Kotlin, SyntaxTheme.DarkDefault, dispatcher)
    }
    waitForIdle()
    dispatcher.scheduler.advanceUntilIdle()
    waitForIdle()

    code = otherKotlinCode
    waitForIdle()
    assertEquals(plainHighlightedString(otherKotlinCode, SyntaxTheme.DarkDefault), state.value)

    dispatcher.scheduler.advanceUntilIdle()
    waitForIdle()
    assertEquals(highlight(otherKotlinCode, Languages.Kotlin, SyntaxTheme.DarkDefault), state.value)
  }

  @Test
  fun asyncKeepsLatestCodeWhenChangedTwiceBeforeCompletion() = runComposeUiTest {
    val dispatcher = newDispatcher()
    var code by mutableStateOf(kotlinCode)
    lateinit var state: State<AnnotatedString>
    setContent {
      state = rememberHighlightedStringAsync(code, Languages.Kotlin, SyntaxTheme.DarkDefault, dispatcher)
    }
    waitForIdle()
    code = otherKotlinCode
    waitForIdle()
    code = pythonCode
    waitForIdle()
    assertEquals(plainHighlightedString(pythonCode, SyntaxTheme.DarkDefault), state.value)

    dispatcher.scheduler.advanceUntilIdle()
    waitForIdle()
    assertEquals(highlight(pythonCode, Languages.Kotlin, SyntaxTheme.DarkDefault), state.value)
  }

  @Test
  fun asyncKeepsHighlightOnUnrelatedRecomposition() = runComposeUiTest {
    val dispatcher = newDispatcher()
    var tick by mutableIntStateOf(0)
    var recompositions = 0
    lateinit var state: State<AnnotatedString>
    setContent {
      // Reading tick here puts this scope in charge of recomposing when it changes.
      recompositions = tick
      state = rememberHighlightedStringAsync(kotlinCode, Languages.Kotlin, SyntaxTheme.DarkDefault, dispatcher)
    }
    waitForIdle()
    dispatcher.scheduler.advanceUntilIdle()
    waitForIdle()
    val highlighted = highlight(kotlinCode, Languages.Kotlin, SyntaxTheme.DarkDefault)
    assertEquals(highlighted, state.value)

    tick++
    waitForIdle()
    assertEquals(1, recompositions)
    assertEquals(highlighted, state.value)
  }

  @Test
  fun asyncRestartsWhenContextChanges() = runComposeUiTest {
    val first = newDispatcher()
    val second = newDispatcher()
    var dispatcher by mutableStateOf(first)
    lateinit var state: State<AnnotatedString>
    setContent {
      state = rememberHighlightedStringAsync(kotlinCode, Languages.Kotlin, SyntaxTheme.DarkDefault, dispatcher)
    }
    waitForIdle()
    first.scheduler.advanceUntilIdle()
    waitForIdle()

    dispatcher = second
    waitForIdle()
    val plain = plainHighlightedString(kotlinCode, SyntaxTheme.DarkDefault)
    assertEquals(plain, state.value)

    first.scheduler.advanceUntilIdle()
    waitForIdle()
    assertEquals(plain, state.value)

    second.scheduler.advanceUntilIdle()
    waitForIdle()
    assertEquals(highlight(kotlinCode, Languages.Kotlin, SyntaxTheme.DarkDefault), state.value)
  }
}
