package io.github.mataku.compose.highlight.css

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertTrue

class CssHighlightTest {

  private val keywordColor = Color.Red
  private val tagColor = Color.Magenta
  private val propertyColor = Color.Cyan
  private val numberColor = Color.Yellow
  private val commentColor = Color.Blue
  private val baseColor = Color.White

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = baseColor),
    keyword = SpanStyle(color = keywordColor),
    tag = SpanStyle(color = tagColor),
    property = SpanStyle(color = propertyColor),
    number = SpanStyle(color = numberColor),
    comment = SpanStyle(color = commentColor),
  )

  @Test
  fun element_selector_is_styled_as_tag() {
    val code = "div { color: red; }"
    val annotated = highlight(code, Languages.Css, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == 3 && it.item.color == tagColor },
      "expected tag 0..3 styled with tagColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun property_name_is_styled() {
    val code = "div { color: red; }"
    val annotated = highlight(code, Languages.Css, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 6 && it.end == 11 && it.item.color == propertyColor },
      "expected property 6..11 styled with propertyColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun at_rule_is_styled_as_keyword() {
    val code = "@media screen {}"
    val annotated = highlight(code, Languages.Css, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == 6 && it.item.color == keywordColor },
      "expected keyword 0..6 styled with keywordColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun integer_value_is_styled() {
    val code = "a { z-index: 10; }"
    val annotated = highlight(code, Languages.Css, theme)
    val start = code.indexOf("10")
    assertTrue(
      annotated.spanStyles.any { it.start == start && it.end == start + 2 && it.item.color == numberColor },
      "expected number $start..${start + 2} styled with numberColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun block_comment_is_styled() {
    val code = "/* note */\na {}"
    val annotated = highlight(code, Languages.Css, theme)
    val commentEnd = code.indexOf('\n')
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == commentEnd && it.item.color == commentColor },
      "expected comment 0..$commentEnd styled with commentColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun base_style_covers_entire_code() {
    val code = "a {}"
    val annotated = highlight(code, Languages.Css, theme)
    val baseSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == code.length && it.item.color == baseColor }
    assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
  }
}
