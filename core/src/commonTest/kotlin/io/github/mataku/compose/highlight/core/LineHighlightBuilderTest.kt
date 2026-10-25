package io.github.mataku.compose.highlight.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LineHighlightBuilderTest {
  private fun lines(code: String, ranges: List<IntRange>? = null, vararg spans: Triple<Int, Int, String>): List<HighlightedLine> {
    val b = LineHighlightBuilder(code, ranges)
    spans.forEach { (s, e, n) -> b.add(s, e, n) }
    return b.build()
  }

  @Test
  fun line_count_follows_git() {
    assertEquals(0, LineHighlightBuilder("", null).lineCount)
    assertEquals(1, LineHighlightBuilder("a", null).lineCount)
    assertEquals(1, LineHighlightBuilder("a\n", null).lineCount)
    assertEquals(2, LineHighlightBuilder("a\nb\n", null).lineCount)
    assertEquals(1, LineHighlightBuilder("a\rb c", null).lineCount)
    assertEquals(2, LineHighlightBuilder("a\r\r\nb", null).lineCount)
  }

  @Test
  fun text_strips_terminator_and_one_trailing_cr() {
    assertEquals(listOf("a", "b\r", "c"), lines("a\r\nb\r\r\nc").map { it.text })
    assertEquals(listOf(1, 2, 3), lines("a\r\nb\r\r\nc").map { it.lineNumber })
  }

  @Test
  fun unterminated_final_line_strips_one_trailing_cr() {
    assertEquals(listOf("a"), lines("a\r").map { it.text })
    assertEquals(listOf("a", "b"), lines("a\r\nb\r").map { it.text })
    assertEquals(listOf("a\r"), lines("a\r\r").map { it.text })
    assertEquals(1, LineHighlightBuilder("a\r", null).lineCount)
  }

  @Test
  fun span_on_unterminated_final_line_is_clipped_before_trailing_cr() {
    val result = lines("a\r", null, Triple(0, 2, "comment"))
    assertEquals(listOf(HighlightSpan(0, 1, "comment")), result[0].spans)
  }

  @Test
  fun null_selects_all_and_empty_selects_nothing() {
    assertEquals(3, lines("a\nb\nc").size)
    assertTrue(LineHighlightBuilder("a\nb", emptyList()).isEmptySelection)
    assertTrue(LineHighlightBuilder("a\nb", listOf(0..-1, 5..9)).isEmptySelection)
    assertTrue(LineHighlightBuilder("", null).isEmptySelection)
    assertEquals(emptyList(), lines("a\nb", listOf(0..-1)))
  }

  @Test
  fun ranges_are_clipped_merged_and_sorted() {
    val result = lines("l1\nl2\nl3\nl4", listOf(3..9, 0..1, 2..3))
    assertEquals(listOf(1, 2, 3, 4), result.map { it.lineNumber })
    assertEquals(listOf(2, 3), lines("l1\nl2\nl3", listOf(2..2, 2..3, 3..3)).map { it.lineNumber })
  }

  @Test
  fun span_is_split_and_clipped_per_line() {
    val code = "/* a\n b */\nval"
    val result = lines(code, null, Triple(0, 10, "comment"), Triple(11, 14, "keyword"))
    assertEquals(listOf(HighlightSpan(0, 4, "comment")), result[0].spans)
    assertEquals(listOf(HighlightSpan(0, 5, "comment")), result[1].spans)
    assertEquals(listOf(HighlightSpan(0, 3, "keyword")), result[2].spans)
  }

  @Test
  fun span_reaching_cr_is_clipped_to_text() {
    val code = "ab\r\ncd"
    val result = lines(code, null, Triple(0, 3, "comment"))
    assertEquals(listOf(HighlightSpan(0, 2, "comment")), result[0].spans)
    assertEquals(emptyList(), result[1].spans)
  }

  @Test
  fun span_on_unselected_line_is_dropped() {
    val result = lines("aa\nbb\ncc", listOf(2..2), Triple(0, 8, "comment"), Triple(0, 2, "keyword"))
    assertEquals(listOf(HighlightedLine(2, "bb", listOf(HighlightSpan(0, 2, "comment")))), result)
  }

  @Test
  fun application_order_is_preserved_within_a_line() {
    val result = lines("x = \"a\\tb\"", null, Triple(4, 10, "string"), Triple(6, 8, "string.escape"), Triple(0, 1, "variable"))
    assertEquals(listOf("string", "string.escape", "variable"), result[0].spans.map { it.captureName })
  }

  @Test
  fun blank_line_has_empty_text_and_no_spans() {
    assertEquals(HighlightedLine(2, "", emptyList()), lines("a\n\nb")[1])
  }
}
