package io.github.mataku.compose.highlight.material3.textfield

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.IncrementalHighlighter
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.kotlin.Kotlin
import io.github.mataku.compose.highlight.python.Python
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SyntaxHighlightVisualTransformationTest {

  private fun highlighted(text: String): AnnotatedString = AnnotatedString(
    text = text,
    spanStyles = listOf(
      AnnotatedString.Range(SpanStyle(color = Color.Red), 0, 3),
    ),
  )

  @Test
  fun identityMappingRoundTrips() {
    val transformation = ClampingSyntaxVisualTransformation(highlighted("val a"))
    val result = transformation.filter(AnnotatedString("val a"))
    assertEquals(2, result.offsetMapping.originalToTransformed(2))
    assertEquals(2, result.offsetMapping.transformedToOriginal(2))
    assertEquals("val a", result.text.text)
  }

  @Test
  fun shorterCurrentTextClampsSpansWithoutCrashing() {
    // cached spans were computed for "val a" (0..3); current text is now "va"
    val transformation = ClampingSyntaxVisualTransformation(highlighted("val a"))
    val result = transformation.filter(AnnotatedString("va"))
    assertEquals("va", result.text.text)
    assertTrue(result.text.spanStyles.all { it.end <= 2 }, "no span may exceed current length")
    assertTrue(result.text.spanStyles.all { it.start < it.end }, "no empty span")
  }

  @Test
  fun spanFullyPastShorterCurrentTextIsDropped() {
    // cached span is 5..8, entirely beyond the current text's length of 2
    val stale = AnnotatedString(
      text = "val a = 1",
      spanStyles = listOf(
        AnnotatedString.Range(SpanStyle(color = Color.Red), 5, 8),
      ),
    )
    val transformation = ClampingSyntaxVisualTransformation(stale)
    val result = transformation.filter(AnnotatedString("va"))
    assertEquals("va", result.text.text)
    assertTrue(result.text.spanStyles.isEmpty(), "span fully past current text must be dropped")
  }

  @Test
  fun longerCurrentTextLeavesTailUnstyled() {
    val transformation = ClampingSyntaxVisualTransformation(highlighted("val"))
    val result = transformation.filter(AnnotatedString("val a = 1"))
    assertEquals("val a = 1", result.text.text)
    assertTrue(result.text.spanStyles.all { it.end <= 9 })
  }

  @Test
  fun textChangePropagatesToSpans() = runComposeUiTest {
    var text by mutableStateOf("val a = 1")
    var captured: AnnotatedString? = null
    setContent {
      val highlighted by rememberSyntaxSpans(text, Languages.Kotlin, SyntaxTheme.DarkDefault)
      captured = highlighted
    }
    waitForIdle()
    text = "val b = 2"
    waitUntil(timeoutMillis = 2_000) { captured?.text == "val b = 2" }
    assertEquals("val b = 2", captured?.text)
    assertTrue((captured?.spanStyles?.size ?: 0) > 0, "expected styled spans")
  }

  @Test
  fun themeChangeDoesNotRebuildEngine() = runComposeUiTest {
    var constructionCount = 0
    val factory: (Language) -> IncrementalHighlighter = { language ->
      constructionCount++
      IncrementalHighlighter(language)
    }
    var theme by mutableStateOf(SyntaxTheme.DarkDefault)
    var captured: AnnotatedString? = null
    setContent {
      val highlighted by rememberSyntaxSpans("val a = 1", Languages.Kotlin, theme, factory)
      captured = highlighted
    }
    waitForIdle()
    waitUntil(timeoutMillis = 2_000) { captured?.text == "val a = 1" }
    val afterFirst = constructionCount
    theme = SyntaxTheme.LightDefault
    waitForIdle()
    waitUntil(timeoutMillis = 2_000) { captured?.spanStyles?.isNotEmpty() == true }
    assertEquals(afterFirst, constructionCount, "theme change must not rebuild engine")
  }

  @Test
  fun languageSwapRebuildsEngine() = runComposeUiTest {
    var constructionCount = 0
    val factory: (Language) -> IncrementalHighlighter = { language ->
      constructionCount++
      IncrementalHighlighter(language)
    }
    var language by mutableStateOf<Language>(Languages.Kotlin)
    var captured: AnnotatedString? = null
    setContent {
      val highlighted by rememberSyntaxSpans("x = 1", language, SyntaxTheme.DarkDefault, factory)
      captured = highlighted
    }
    waitUntil(timeoutMillis = 2_000) { captured?.text == "x = 1" }
    assertEquals(1, constructionCount)
    language = Languages.Python
    waitUntil(timeoutMillis = 2_000) { constructionCount == 2 }
    assertEquals(2, constructionCount, "language swap must rebuild engine")
  }
}
