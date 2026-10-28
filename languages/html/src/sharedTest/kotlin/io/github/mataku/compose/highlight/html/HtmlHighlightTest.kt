package io.github.mataku.compose.highlight.html

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlHighlightTest {

  private val tagColor = Color.Red
  private val attributeColor = Color.Cyan
  private val stringColor = Color.Green
  private val commentColor = Color.Blue
  private val baseColor = Color.White

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = baseColor),
    tag = SpanStyle(color = tagColor),
    attribute = SpanStyle(color = attributeColor),
    string = SpanStyle(color = stringColor),
    comment = SpanStyle(color = commentColor),
  )

  @Test
  fun start_and_end_tag_names_are_styled() {
    val code = "<div></div>"
    val annotated = highlight(code, Languages.Html, theme)
    listOf(1 to 4, 7 to 10).forEach { (start, end) ->
      assertTrue(
        annotated.spanStyles.any { it.start == start && it.end == end && it.item.color == tagColor },
        "expected tag $start..$end styled with tagColor; spans=${annotated.spanStyles}",
      )
    }
  }

  @Test
  fun attribute_name_is_styled() {
    val code = "<a href=\"/\">x</a>"
    val annotated = highlight(code, Languages.Html, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 3 && it.end == 7 && it.item.color == attributeColor },
      "expected attribute 3..7 styled with attributeColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun attribute_value_is_styled() {
    val code = "<a href=\"/home\">x</a>"
    val annotated = highlight(code, Languages.Html, theme)
    val start = code.indexOf("/home")
    assertTrue(
      annotated.spanStyles.any { it.start == start && it.end == start + 5 && it.item.color == stringColor },
      "expected attribute value $start..${start + 5} styled with stringColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun comment_is_styled() {
    val code = "<!-- note -->\n<p></p>"
    val annotated = highlight(code, Languages.Html, theme)
    val commentEnd = code.indexOf('\n')
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == commentEnd && it.item.color == commentColor },
      "expected comment 0..$commentEnd styled with commentColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun base_style_covers_entire_code() {
    val code = "<p>hi</p>"
    val annotated = highlight(code, Languages.Html, theme)
    val baseSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == code.length && it.item.color == baseColor }
    assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
  }
}
