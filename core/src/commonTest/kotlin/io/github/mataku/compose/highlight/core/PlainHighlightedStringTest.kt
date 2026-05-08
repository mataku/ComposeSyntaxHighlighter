package io.github.mataku.compose.highlight.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class PlainHighlightedStringTest {

  @Test
  fun text_matches_input_code() {
    val result = plainHighlightedString("fun foo() {}", SyntaxTheme())
    assertEquals("fun foo() {}", result.text)
  }

  @Test
  fun empty_code_produces_empty_annotated_string() {
    val result = plainHighlightedString("", SyntaxTheme())
    assertEquals("", result.text)
    assertEquals(0, result.spanStyles.size)
  }

  @Test
  fun default_base_style_adds_no_span() {
    val result = plainHighlightedString("hello", SyntaxTheme())
    assertEquals(0, result.spanStyles.size)
  }

  @Test
  fun non_default_base_style_is_applied_to_full_range() {
    val baseStyle = SpanStyle(color = Color(0xFFE0E0E0))
    val result = plainHighlightedString("hello", SyntaxTheme(baseStyle = baseStyle))
    assertEquals(1, result.spanStyles.size)
    val span = result.spanStyles.single()
    assertEquals(baseStyle, span.item)
    assertEquals(0, span.start)
    assertEquals(5, span.end)
  }

  @Test
  fun non_default_base_style_on_empty_code_produces_no_span() {
    val baseStyle = SpanStyle(color = Color(0xFFE0E0E0))
    val result = plainHighlightedString("", SyntaxTheme(baseStyle = baseStyle))
    assertEquals(0, result.spanStyles.size)
  }
}
