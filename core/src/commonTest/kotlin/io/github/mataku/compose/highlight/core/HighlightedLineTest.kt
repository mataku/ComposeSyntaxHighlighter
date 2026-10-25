package io.github.mataku.compose.highlight.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HighlightedLineTest {
  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = Color.White),
    string = SpanStyle(color = Color.Green, fontStyle = FontStyle.Italic),
    stringEscape = SpanStyle(color = Color.Red),
  )

  @Test
  fun to_annotated_string_applies_base_style_then_spans_in_order() {
    val line = HighlightedLine(3, "x = \"a\\tb\"", listOf(HighlightSpan(4, 10, "string"), HighlightSpan(6, 8, "string.escape")))
    val result = line.toAnnotatedString(theme)
    assertEquals("x = \"a\\tb\"", result.text)
    assertEquals(
      listOf(
        AnnotatedString.Range(theme.baseStyle, 0, 10),
        AnnotatedString.Range(theme.string!!, 4, 10),
        AnnotatedString.Range(theme.stringEscape!!, 6, 8),
      ),
      result.spanStyles,
    )
  }

  @Test
  fun unresolved_capture_is_skipped() {
    val line = HighlightedLine(1, "foo", listOf(HighlightSpan(0, 3, "keyword")))
    assertEquals(listOf(AnnotatedString.Range(theme.baseStyle, 0, 3)), line.toAnnotatedString(theme).spanStyles)
  }

  @Test
  fun empty_text_has_no_ranges() {
    val line = HighlightedLine(2, "", emptyList())
    assertEquals(emptyList<AnnotatedString.Range<SpanStyle>>(), line.toAnnotatedString(theme).spanStyles)
  }

  @Test
  fun empty_base_style_adds_no_range() {
    val line = HighlightedLine(1, "ab", emptyList())
    assertEquals(emptyList<AnnotatedString.Range<SpanStyle>>(), line.toAnnotatedString(SyntaxTheme()).spanStyles)
  }

  @Test
  fun value_equality() {
    val a = HighlightedLine(1, "ab", listOf(HighlightSpan(0, 1, "keyword")))
    val b = HighlightedLine(1, "ab", listOf(HighlightSpan(0, 1, "keyword")))
    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
    assertNotEquals(a, HighlightedLine(2, "ab", listOf(HighlightSpan(0, 1, "keyword"))))
    assertNotEquals(HighlightSpan(0, 1, "keyword"), HighlightSpan(0, 1, "string"))
    assertEquals("HighlightSpan(start=0, end=1, captureName=keyword)", HighlightSpan(0, 1, "keyword").toString())
  }
}
