package io.github.mataku.compose.highlight.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.IncrementalHighlighter
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkdownIncrementalHighlightTest {

  private val emphasisColor = Color.Magenta

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = Color.White),
    punctuation = SpanStyle(color = Color.Gray),
    extras = mapOf(
      "text.title" to SpanStyle(color = Color.Red),
      "text.literal" to SpanStyle(color = Color.Yellow),
      "text.emphasis" to SpanStyle(color = emphasisColor),
      "text.strong" to SpanStyle(color = Color.Blue),
    ),
  )

  @Test
  fun first_call_styles_inline_constructs() {
    val code = "# Title\n\nsome *em* text\n"
    IncrementalHighlighter(Languages.Markdown).use { engine ->
      val actual = engine.update(code, theme)
      val start = code.indexOf("*em*")
      assertTrue(
        actual.spanStyles.any { it.start == start && it.end == start + 4 && it.item.color == emphasisColor },
        "expected emphasis at $start; spans=${actual.spanStyles}",
      )
    }
  }

  @Test
  fun typing_one_char_at_a_time_matches_full_highlight() {
    val target = "# Title *em*\n\nfirst **bold** and `code`\n\nsecond *é😀* line\n"
    IncrementalHighlighter(Languages.Markdown).use { engine ->
      for (i in 0..target.length) {
        val current = target.substring(0, i)
        assertMatchesFullHighlight(current, engine.update(current, theme))
      }
    }
  }

  @Test
  fun edits_that_join_and_split_paragraphs_match_full_highlight() {
    val edits = listOf(
      "one *a*\n\ntwo **b**\n\nthree `c`\n",
      "one *a*\ntwo **b**\n\nthree `c`\n",
      "one *a*\ntwo **b**\n\nthree `c` and *d*\n",
      "one *a*\n\ntwo **b**\n\nthree `c` and *d*\n",
      "two **b**\n\nthree `c` and *d*\n",
      "zero *z*\n\ntwo **b**\n\nthree `c` and *d*\n",
    )
    IncrementalHighlighter(Languages.Markdown).use { engine ->
      for (code in edits) {
        assertMatchesFullHighlight(code, engine.update(code, theme))
      }
    }
  }

  private fun assertMatchesFullHighlight(code: String, actual: AnnotatedString) {
    val expected = highlight(code, Languages.Markdown, theme)
    assertEquals(expected.text, actual.text)
    assertEquals(effectiveStyles(expected), effectiveStyles(actual), "effective styles mismatch for: $code")
  }

  private fun effectiveStyles(annotated: AnnotatedString): List<SpanStyle> = List(annotated.text.length) { index ->
    annotated.spanStyles
      .filter { index >= it.start && index < it.end }
      .fold(SpanStyle()) { merged, range -> merged.merge(range.item) }
  }
}
