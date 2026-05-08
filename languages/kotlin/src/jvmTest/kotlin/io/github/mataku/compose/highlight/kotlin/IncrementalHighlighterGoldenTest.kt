package io.github.mataku.compose.highlight.kotlin

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.IncrementalHighlighter
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IncrementalHighlighterGoldenTest {

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = Color.White),
    keyword = SpanStyle(color = Color.Red),
    string = SpanStyle(color = Color.Green),
    comment = SpanStyle(color = Color.Blue),
    number = SpanStyle(color = Color.Yellow),
  )

  @Test
  fun first_call_matches_highlight() {
    val code = "class Foo { val n = 42 }"
    val expected = highlight(code, Languages.Kotlin, theme)
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      val actual = engine.update(code, theme)
      assertEqualAnnotated(expected, actual)
    }
  }

  @Test
  fun update_after_close_throws() {
    val engine = IncrementalHighlighter(Languages.Kotlin)
    engine.close()
    assertFailsWith<IllegalStateException> {
      engine.update("class A", theme)
    }
  }

  @Test
  fun close_is_idempotent() {
    val engine = IncrementalHighlighter(Languages.Kotlin)
    engine.close()
    engine.close()
  }

  @Test
  fun theme_only_update_matches_full_highlight_with_new_theme() {
    val code = "class Foo { val n = 42 }"
    val themeA = theme
    val themeB = SyntaxTheme(
      baseStyle = SpanStyle(color = Color.Black),
      keyword = SpanStyle(color = Color.Cyan),
      string = SpanStyle(color = Color.Magenta),
      comment = SpanStyle(color = Color.Gray),
      number = SpanStyle(color = Color(0xFF888888)),
    )
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      engine.update(code, themeA)
      val actual = engine.update(code, themeB)
      val expected = highlight(code, Languages.Kotlin, themeB)
      assertEqualAnnotated(expected, actual)
    }
  }
}

private fun assertEqualAnnotated(
  expected: androidx.compose.ui.text.AnnotatedString,
  actual: androidx.compose.ui.text.AnnotatedString,
) {
  assertEquals(expected.text, actual.text, "text mismatch")
  assertEquals(
    expected.spanStyles.map { Triple(it.start, it.end, it.item) }.toSet(),
    actual.spanStyles.map { Triple(it.start, it.end, it.item) }.toSet(),
    "spanStyles mismatch (compared as set)",
  )
}
