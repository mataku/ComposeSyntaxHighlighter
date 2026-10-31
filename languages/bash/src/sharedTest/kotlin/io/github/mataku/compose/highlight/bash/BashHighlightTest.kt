package io.github.mataku.compose.highlight.bash

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BashHighlightTest {

  private val keywordColor = Color.Red
  private val functionColor = Color.Magenta
  private val stringColor = Color.Green
  private val commentColor = Color.Blue
  private val baseColor = Color.White

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = baseColor),
    keyword = SpanStyle(color = keywordColor),
    function = SpanStyle(color = functionColor),
    string = SpanStyle(color = stringColor),
    comment = SpanStyle(color = commentColor),
  )

  @Test
  fun keyword_if_is_styled() {
    val code = "if true; then echo hi; fi"
    val annotated = highlight(code, Languages.Bash, theme)
    val keywordSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == 2 }
    assertEquals(keywordColor, keywordSpan?.item?.color, "expected keyword 0..2 styled with keywordColor; spans=${annotated.spanStyles}")
  }

  @Test
  fun command_name_is_styled_as_function() {
    val code = "echo hi"
    val annotated = highlight(code, Languages.Bash, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == 4 && it.item.color == functionColor },
      "expected command 0..4 styled with functionColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun string_literal_is_styled() {
    val code = "echo \"hi\""
    val annotated = highlight(code, Languages.Bash, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 5 && it.end == 9 && it.item.color == stringColor },
      "expected string 5..9 styled with stringColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun line_comment_is_styled() {
    val code = "# note\necho hi"
    val annotated = highlight(code, Languages.Bash, theme)
    val commentEnd = code.indexOf('\n')
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == commentEnd && it.item.color == commentColor },
      "expected comment 0..$commentEnd styled with commentColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun base_style_covers_entire_code() {
    val code = "echo hi"
    val annotated = highlight(code, Languages.Bash, theme)
    val baseSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == code.length && it.item.color == baseColor }
    assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
  }
}
