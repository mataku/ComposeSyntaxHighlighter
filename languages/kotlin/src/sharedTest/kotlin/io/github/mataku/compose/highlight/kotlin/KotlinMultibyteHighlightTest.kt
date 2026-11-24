package io.github.mataku.compose.highlight.kotlin

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.IncrementalHighlighter
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.core.highlightLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinMultibyteHighlightTest {

  private val keywordColor = Color.Red
  private val stringColor = Color.Green
  private val builtinColor = Color.Magenta

  private val theme = SyntaxTheme(
    keyword = SpanStyle(color = keywordColor),
    string = SpanStyle(color = stringColor),
    extras = mapOf("variable.builtin" to SpanStyle(color = builtinColor)),
  )

  @Test
  fun keyword_after_supplementary_character_keeps_its_position() {
    val code = "val a = \"😀😀😀\"\nval b = 1"
    assertSpan(highlight(code, Languages.Kotlin, theme), code.lastIndexOf("val"), 3, keywordColor)
  }

  @Test
  fun keyword_after_nul_keeps_its_position() {
    val code = "val a = \"\u0000\u0000\"\nval b = 1"
    assertSpan(highlight(code, Languages.Kotlin, theme), code.lastIndexOf("val"), 3, keywordColor)
  }

  @Test
  fun text_predicate_after_bmp_multibyte_characters_matches() {
    val code = "val s = \"日本語\"\nlistOf(1).map { it }"
    assertSpan(highlight(code, Languages.Kotlin, theme), code.lastIndexOf("it"), 2, builtinColor)
  }

  @Test
  fun trailing_code_after_multibyte_characters_is_parsed() {
    val code = "val s = \"日本語日本語日本語\"\nval t = \"x\""
    assertSpan(highlight(code, Languages.Kotlin, theme), code.lastIndexOf("\"x\""), 3, stringColor)
  }

  @Test
  fun multibyte_string_literal_is_styled_over_original_characters() {
    val code = "val s = \"日本語😀\""
    val annotated = highlight(code, Languages.Kotlin, theme)
    assertEquals(code, annotated.text)
    assertSpan(annotated, code.indexOf('"'), code.length - code.indexOf('"'), stringColor)
  }

  @Test
  fun code_after_japanese_comments_is_styled_at_its_position() {
    val code = "// 日本語のコメント\nval a = 1 // 末尾のコメント😀\n/* 複数行\n   コメント */\nfun f() = listOf(1).map { it }"
    val annotated = highlight(code, Languages.Kotlin, theme)
    assertSpan(annotated, code.indexOf("val"), 3, keywordColor)
    assertSpan(annotated, code.indexOf("fun"), 3, keywordColor)
    assertSpan(annotated, code.lastIndexOf("it"), 2, builtinColor)
  }

  @Test
  fun identifier_with_non_ascii_letters_does_not_shift_following_code() {
    val code = "val 名前 = \"x\"\nval b = 1"
    val annotated = highlight(code, Languages.Kotlin, theme)
    assertSpan(annotated, code.indexOf("\"x\""), 3, stringColor)
    assertSpan(annotated, code.lastIndexOf("val"), 3, keywordColor)
  }

  @Test
  fun highlight_lines_handles_multibyte_characters() {
    val code = "val a = \"😀\"\nval b = 1"
    val line = highlightLines(code, Languages.Kotlin, listOf(2..2)).single()
    assertTrue(
      line.spans.any { it.start == 0 && it.end == 3 && it.captureName == "keyword" },
      "expected keyword on line 2 at 0..3; spans=${line.spans}",
    )
  }

  @Test
  fun incremental_update_after_inserting_supplementary_character_keeps_positions() {
    val before = "val a = \"\"\nlistOf(1).map { it }"
    val after = "val a = \"😀日\"\nlistOf(1).map { it }"
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      engine.update(before, theme)
      val annotated = engine.update(after, theme)
      assertSpan(annotated, after.lastIndexOf("it"), 2, builtinColor)
      assertSpan(annotated, after.indexOf('"'), "\"😀日\"".length, stringColor)
    }
  }

  @Test
  fun keyword_after_lone_surrogate_keeps_its_position() {
    val code = "// \uD800 \uDC00\nval b = 1"
    assertSpan(highlight(code, Languages.Kotlin, theme), code.lastIndexOf("val"), 3, keywordColor)
  }

  @Test
  fun incremental_edit_after_lone_surrogate_matches_full_highlight() {
    val before = "// \uD800\nval b = 1"
    val after = "// \uD800\nval b = \"x\""
    IncrementalHighlighter(Languages.Kotlin).use { engine ->
      engine.update(before, theme)
      val actual = engine.update(after, theme)
      val expected = highlight(after, Languages.Kotlin, theme)
      assertEquals(expected.spanStyles.toSet(), actual.spanStyles.toSet())
      assertSpan(actual, after.lastIndexOf("val"), 3, keywordColor)
    }
  }

  private fun assertSpan(annotated: AnnotatedString, start: Int, length: Int, color: Color) {
    assertTrue(
      annotated.spanStyles.any { it.start == start && it.end == start + length && it.item.color == color },
      "expected $color at $start..${start + length}; spans=${annotated.spanStyles}",
    )
  }
}
