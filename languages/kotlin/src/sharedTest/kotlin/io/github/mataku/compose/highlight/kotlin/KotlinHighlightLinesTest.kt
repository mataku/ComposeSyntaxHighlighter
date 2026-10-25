package io.github.mataku.compose.highlight.kotlin

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.HighlightSpan
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.core.highlightLines
import io.github.mataku.compose.highlight.core.toAnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinHighlightLinesTest {

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = Color.White),
    keyword = SpanStyle(color = Color.Red),
    string = SpanStyle(color = Color.Green, fontStyle = FontStyle.Italic),
    stringEscape = SpanStyle(color = Color.Blue),
    comment = SpanStyle(color = Color.Gray),
  )

  private val code = listOf(
    "/* first",
    "   end */",
    "val s = \"\"\"",
    "raw",
    "\"\"\"",
    "val t = '\\t'",
    "val u = 1",
  ).joinToString("\n")

  private fun effectiveStyles(a: AnnotatedString): List<SpanStyle> = List(a.text.length) { i ->
    a.spanStyles.filter { i >= it.start && i < it.end }.fold(SpanStyle()) { acc, r -> acc.merge(r.item) }
  }

  @Test
  fun every_line_matches_highlight_subsequence() {
    val whole = highlight(code, Languages.Kotlin, theme)
    val lines = highlightLines(code, Languages.Kotlin)
    assertEquals(7, lines.size)
    var start = 0
    lines.forEachIndexed { i, line ->
      assertEquals(i + 1, line.lineNumber)
      val end = start + line.text.length
      assertEquals(code.substring(start, end), line.text)
      assertTrue(line.spans.all { it.start >= 0 && it.start < it.end && it.end <= line.text.length }, "line ${i + 1} spans=${line.spans}")
      assertEquals(
        effectiveStyles(whole.subSequence(start, end)),
        effectiveStyles(line.toAnnotatedString(theme)),
        "line ${i + 1}",
      )
      start = end + 1
    }
  }

  @Test
  fun multiline_comment_is_clipped_per_line() {
    val lines = highlightLines(code, Languages.Kotlin, listOf(1..2))
    assertEquals(listOf(1, 2), lines.map { it.lineNumber })
    assertEquals(listOf(HighlightSpan(0, lines[0].text.length, "comment")), lines[0].spans)
    assertEquals(listOf(HighlightSpan(0, "   end */".length, "comment")), lines[1].spans)
  }

  @Test
  fun string_escape_follows_character_in_span_order() {
    val line = highlightLines(code, Languages.Kotlin, listOf(6..6)).single()
    val names = line.spans.map { it.captureName }
    val character = line.spans.first { it.captureName == "character" }
    val escape = line.spans.first { it.captureName == "string.escape" }
    assertTrue(names.indexOf("character") < names.indexOf("string.escape"), "spans=${line.spans}")
    assertEquals(line.text.indexOf('\''), character.start)
    assertEquals(line.text.length, character.end)
    assertEquals(line.text.indexOf("\\t"), escape.start)
    assertEquals(escape.start + 2, escape.end)
  }

  @Test
  fun out_of_range_and_empty_selections_do_not_throw() {
    assertEquals(emptyList(), highlightLines(code, Languages.Kotlin, emptyList()))
    assertEquals(emptyList(), highlightLines(code, Languages.Kotlin, listOf(0..-1, 100..200)))
    assertEquals(listOf(6, 7), highlightLines(code, Languages.Kotlin, listOf(6..99)).map { it.lineNumber })
  }

  @Test
  fun crlf_input_yields_cr_free_text_and_same_numbering() {
    val crlf = code.replace("\n", "\r\n")
    val lines = highlightLines(crlf, Languages.Kotlin)
    assertEquals(highlightLines(code, Languages.Kotlin).map { it.text }, lines.map { it.text })
    assertEquals(highlightLines(code, Languages.Kotlin).map { it.spans }, lines.map { it.spans })
  }
}
