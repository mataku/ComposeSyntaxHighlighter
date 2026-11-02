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

  private fun assertSpan(annotated: AnnotatedString, start: Int, length: Int, color: Color) {
    assertTrue(
      annotated.spanStyles.any { it.start == start && it.end == start + length && it.item.color == color },
      "expected $color at $start..${start + length}; spans=${annotated.spanStyles}",
    )
  }
}
