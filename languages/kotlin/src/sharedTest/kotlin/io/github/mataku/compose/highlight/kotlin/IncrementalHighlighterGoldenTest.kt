package io.github.mataku.compose.highlight.kotlin

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
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

  @Test
  fun multibyte_edits_each_match_full_highlight() {
    val edits = listOf(
      "val s = \"あ\" // こ",
      "val s = \"あい\" // こ",
      "val s = \"あい😀\" // こん",
      "val 名前 = \"い😀\" // こん",
      "val 名前 = 1 // 😀",
    )
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      for (code in edits) {
        val actual = engine.update(code, theme)
        val expected = highlight(code, Languages.Kotlin, theme)
        assertEqualAnnotated(expected, actual)
        assertEquals(effectiveStyles(expected), effectiveStyles(actual), "effective styles mismatch for: $code")
      }
    }
  }

  @Test
  fun edits_keep_the_span_application_order_of_full_highlight() {
    // Overlapping captures resolve to different styles under a full theme, so a reordered span list changes what is drawn.
    val fullTheme = SyntaxTheme.DarkDefault
    val target = "@Suppress(\"x\")\nfun greet(name: String): Int { println(\"hi \$name \${name.length}\"); return 1 }"
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      for (i in 0..target.length) {
        val current = target.substring(0, i)
        val actual = engine.update(current, fullTheme)
        val expected = highlight(current, Languages.Kotlin, fullTheme)
        assertEquals(effectiveStyles(expected), effectiveStyles(actual), "effective styles mismatch for: $current")
      }
      val edited = target.replace("greet", "welcome").replace("return 1", "return 100")
      val actual = engine.update(edited, fullTheme)
      val expected = highlight(edited, Languages.Kotlin, fullTheme)
      assertEquals(effectiveStyles(expected), effectiveStyles(actual), "effective styles mismatch for: $edited")
    }
  }

  @Test
  fun typing_one_char_at_a_time_each_call_matches_full_highlight() {
    val target = "fun greet() { println(\"hi\") }"
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      for (i in 0..target.length) {
        val current = target.substring(0, i)
        val actual = engine.update(current, theme)
        val expected = highlight(current, Languages.Kotlin, theme)
        assertEqualAnnotated(expected, actual)
      }
    }
  }

  @Test
  fun edit_inside_multiline_string_re_emits_string_capture() {
    val codeA = """
      val msg = ""${'"'}
        hello
        world
      ""${'"'}
    """.trimIndent()
    val codeB = codeA.replace("hello", "HELLO")
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      engine.update(codeA, theme)
      val actual = engine.update(codeB, theme)
      val expected = highlight(codeB, Languages.Kotlin, theme)
      assertEqualAnnotated(expected, actual)
    }
  }

  @Test
  fun edit_inside_block_comment_re_emits_comment_capture() {
    val codeA = """
      /*
       * old line
       */
      class Foo
    """.trimIndent()
    val codeB = codeA.replace("old line", "new line")
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      engine.update(codeA, theme)
      val actual = engine.update(codeB, theme)
      val expected = highlight(codeB, Languages.Kotlin, theme)
      assertEqualAnnotated(expected, actual)
    }
  }
}

// The style each character is drawn with once the spans covering it are merged in list order.
private fun effectiveStyles(annotated: AnnotatedString): List<SpanStyle> = List(annotated.text.length) { index ->
  annotated.spanStyles
    .filter { index >= it.start && index < it.end }
    .fold(SpanStyle()) { merged, range -> merged.merge(range.item) }
}

private fun assertEqualAnnotated(
  expected: AnnotatedString,
  actual: AnnotatedString,
) {
  assertEquals(expected.text, actual.text, "text mismatch")
  assertEquals(
    expected.spanStyles.map { Triple(it.start, it.end, it.item) }.toSet(),
    actual.spanStyles.map { Triple(it.start, it.end, it.item) }.toSet(),
    "spanStyles mismatch (compared as set)",
  )
}
