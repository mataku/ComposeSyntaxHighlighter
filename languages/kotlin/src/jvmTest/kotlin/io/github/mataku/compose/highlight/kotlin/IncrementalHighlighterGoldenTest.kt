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

  @Test
  fun edit_at_end_matches_full_highlight() {
    val codeA = "class Foo { val n = 42 }"
    val codeB = "class Foo { val n = 42 }\nval m = 7"
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      engine.update(codeA, theme)
      val actual = engine.update(codeB, theme)
      val expected = highlight(codeB, Languages.Kotlin, theme)
      assertEqualAnnotated(expected, actual)
    }
  }

  @Test
  fun edit_in_middle_matches_full_highlight() {
    val codeA = "fun a() { val x = 1 }\nfun b() { val y = 2 }"
    val codeB = "fun a() { val x = 100 }\nfun b() { val y = 2 }"
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      engine.update(codeA, theme)
      val actual = engine.update(codeB, theme)
      val expected = highlight(codeB, Languages.Kotlin, theme)
      assertEqualAnnotated(expected, actual)
    }
  }

  @Test
  fun deletion_at_start_matches_full_highlight() {
    val codeA = "// comment\nclass Foo"
    val codeB = "class Foo"
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      engine.update(codeA, theme)
      val actual = engine.update(codeB, theme)
      val expected = highlight(codeB, Languages.Kotlin, theme)
      assertEqualAnnotated(expected, actual)
    }
  }

  @Test
  fun five_successive_edits_each_match_full_highlight() {
    val edits = listOf(
      "class A",
      "class A {",
      "class A { val n",
      "class A { val n = 1",
      "class A { val n = 1 }",
    )
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      for (code in edits) {
        val actual = engine.update(code, theme)
        val expected = highlight(code, Languages.Kotlin, theme)
        assertEqualAnnotated(expected, actual)
      }
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
