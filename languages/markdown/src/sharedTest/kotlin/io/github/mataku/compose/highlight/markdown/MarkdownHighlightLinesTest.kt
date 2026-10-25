package io.github.mataku.compose.highlight.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.core.highlightLines
import io.github.mataku.compose.highlight.core.toAnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkdownHighlightLinesTest {

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = Color.White),
    punctuation = SpanStyle(color = Color.Gray),
    stringEscape = SpanStyle(color = Color.Green),
    extras = mapOf(
      "text.title" to SpanStyle(color = Color.Red),
      "text.literal" to SpanStyle(color = Color.Yellow),
      "text.uri" to SpanStyle(color = Color.Cyan),
      "text.emphasis" to SpanStyle(color = Color.Magenta),
      "text.strong" to SpanStyle(color = Color.Blue),
    ),
  )

  private val code = "# Title *em*\n\nplain\n"

  private fun effectiveStyles(a: AnnotatedString): List<SpanStyle> = List(a.text.length) { i ->
    a.spanStyles.filter { i >= it.start && i < it.end }.fold(SpanStyle()) { acc, r -> acc.merge(r.item) }
  }

  @Test
  fun injected_inline_captures_follow_host_captures() {
    val line = highlightLines(code, Languages.Markdown, listOf(1..1)).single()
    assertEquals("# Title *em*", line.text)
    val names = line.spans.map { it.captureName }
    assertTrue("text.title" in names && "text.emphasis" in names, "spans=${line.spans}")
    assertTrue(names.indexOf("text.title") < names.indexOf("text.emphasis"), "spans=${line.spans}")
    val emphasis = line.spans.first { it.captureName == "text.emphasis" }
    assertEquals(line.text.indexOf("*em*"), emphasis.start)
    assertEquals(emphasis.start + "*em*".length, emphasis.end)
  }

  @Test
  fun every_line_matches_highlight_subsequence() {
    val whole = highlight(code, Languages.Markdown, theme)
    val lines = highlightLines(code, Languages.Markdown)
    assertEquals(3, lines.size)
    var start = 0
    lines.forEachIndexed { i, line ->
      assertEquals(i + 1, line.lineNumber)
      val end = start + line.text.length
      assertEquals(code.substring(start, end), line.text)
      assertEquals(
        effectiveStyles(whole.subSequence(start, end)),
        effectiveStyles(line.toAnnotatedString(theme)),
        "line ${i + 1}",
      )
      start = end + 1
    }
  }
}
