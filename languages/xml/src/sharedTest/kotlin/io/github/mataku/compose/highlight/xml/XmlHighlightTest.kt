package io.github.mataku.compose.highlight.xml

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertTrue

class XmlHighlightTest {

  private val tagColor = Color.Red
  private val propertyColor = Color.Cyan
  private val stringColor = Color.Green
  private val keywordColor = Color.Magenta
  private val constantColor = Color.Yellow
  private val commentColor = Color.Blue
  private val baseColor = Color.White

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = baseColor),
    tag = SpanStyle(color = tagColor),
    property = SpanStyle(color = propertyColor),
    string = SpanStyle(color = stringColor),
    keyword = SpanStyle(color = keywordColor),
    constant = SpanStyle(color = constantColor),
    comment = SpanStyle(color = commentColor),
  )

  @Test
  fun start_end_and_empty_tag_names_are_styled() {
    val code = "<root><leaf/></root>"
    val annotated = highlight(code, Languages.Xml, theme)
    listOf(1 to 5, 7 to 11, 15 to 19).forEach { (start, end) ->
      assertTrue(
        annotated.spanStyles.any { it.start == start && it.end == end && it.item.color == tagColor },
        "expected tag $start..$end styled with tagColor; spans=${annotated.spanStyles}",
      )
    }
  }

  @Test
  fun attribute_name_is_styled_as_property() {
    val code = "<item id=\"1\"/>"
    val annotated = highlight(code, Languages.Xml, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 6 && it.end == 8 && it.item.color == propertyColor },
      "expected attribute 6..8 styled with propertyColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun attribute_value_is_styled() {
    val code = "<item id=\"abc\"/>"
    val annotated = highlight(code, Languages.Xml, theme)
    val start = code.indexOf('"')
    assertTrue(
      annotated.spanStyles.any { it.start <= start + 1 && it.end >= start + 4 && it.item.color == stringColor },
      "expected attribute value around $start styled with stringColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun xml_declaration_keyword_is_styled() {
    val code = "<?xml version=\"1.0\"?>\n<a/>"
    val annotated = highlight(code, Languages.Xml, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 2 && it.end == 5 && it.item.color == keywordColor },
      "expected xml keyword 2..5 styled with keywordColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun entity_reference_is_styled_as_constant() {
    val code = "<a>x &amp; y</a>"
    val annotated = highlight(code, Languages.Xml, theme)
    val start = code.indexOf('&')
    assertTrue(
      annotated.spanStyles.any { it.start == start && it.end == start + 5 && it.item.color == constantColor },
      "expected entity $start..${start + 5} styled with constantColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun comment_is_styled() {
    val code = "<!-- note -->\n<a/>"
    val annotated = highlight(code, Languages.Xml, theme)
    val commentEnd = code.indexOf('\n')
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == commentEnd && it.item.color == commentColor },
      "expected comment 0..$commentEnd styled with commentColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun base_style_covers_entire_code() {
    val code = "<a>hi</a>"
    val annotated = highlight(code, Languages.Xml, theme)
    val baseSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == code.length && it.item.color == baseColor }
    assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
  }
}
