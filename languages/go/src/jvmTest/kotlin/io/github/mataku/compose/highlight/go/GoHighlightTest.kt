package io.github.mataku.compose.highlight.go

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GoHighlightTest {

  private val keywordColor = Color.Red
  private val stringColor = Color.Green
  private val commentColor = Color.Blue
  private val numberColor = Color.Yellow
  private val baseColor = Color.White

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = baseColor),
    keyword = SpanStyle(color = keywordColor),
    string = SpanStyle(color = stringColor),
    comment = SpanStyle(color = commentColor),
    number = SpanStyle(color = numberColor),
  )

  @Test
  fun keyword_func_is_styled() {
    val code = "func main() {}"
    val annotated = highlight(code, GoLanguage, theme)
    val keywordSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == 4 }
    assertEquals(keywordColor, keywordSpan?.item?.color, "expected keyword 0..4 styled with keywordColor; spans=${annotated.spanStyles}")
  }

  @Test
  fun string_literal_is_styled() {
    val code = "\"hello\""
    val annotated = highlight(code, GoLanguage, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == 7 && it.item.color == stringColor },
      "expected string capture 0..7 styled with stringColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun line_comment_is_styled() {
    val code = "// comment\npackage main"
    val annotated = highlight(code, GoLanguage, theme)
    val commentEnd = code.indexOf('\n')
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == commentEnd && it.item.color == commentColor },
      "expected comment 0..$commentEnd styled with commentColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun integer_literal_is_styled() {
    val code = "x := 42"
    val annotated = highlight(code, GoLanguage, theme)
    val start = code.indexOf("42")
    val end = start + 2
    assertTrue(
      annotated.spanStyles.any { it.start == start && it.end == end && it.item.color == numberColor },
      "expected number $start..$end styled with numberColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun base_style_covers_entire_code() {
    val code = "func main() {}"
    val annotated = highlight(code, GoLanguage, theme)
    val baseSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == code.length && it.item.color == baseColor }
    assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
  }

  @Test
  fun languages_extension_returns_canonical_language() {
    assertSame(GoLanguage, Languages.go)
  }
}
