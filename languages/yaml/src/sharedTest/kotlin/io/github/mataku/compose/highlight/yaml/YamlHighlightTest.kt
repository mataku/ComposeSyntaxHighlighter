package io.github.mataku.compose.highlight.yaml

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertTrue

class YamlHighlightTest {

  private val propertyColor = Color.Red
  private val stringColor = Color.Green
  private val numberColor = Color.Yellow
  private val booleanColor = Color.Magenta
  private val commentColor = Color.Blue
  private val baseColor = Color.White

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = baseColor),
    property = SpanStyle(color = propertyColor),
    string = SpanStyle(color = stringColor),
    number = SpanStyle(color = numberColor),
    boolean = SpanStyle(color = booleanColor),
    comment = SpanStyle(color = commentColor),
  )

  @Test
  fun mapping_key_is_styled_as_property() {
    val code = "name: value"
    val annotated = highlight(code, Languages.Yaml, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == 4 && it.item.color == propertyColor },
      "expected key 0..4 styled with propertyColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun scalar_value_is_styled_as_string() {
    val code = "name: value"
    val annotated = highlight(code, Languages.Yaml, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 6 && it.end == 11 && it.item.color == stringColor },
      "expected value 6..11 styled with stringColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun integer_is_styled() {
    val code = "port: 8080"
    val annotated = highlight(code, Languages.Yaml, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 6 && it.end == 10 && it.item.color == numberColor },
      "expected number 6..10 styled with numberColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun boolean_is_styled() {
    val code = "debug: true"
    val annotated = highlight(code, Languages.Yaml, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 7 && it.end == 11 && it.item.color == booleanColor },
      "expected boolean 7..11 styled with booleanColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun comment_is_styled() {
    val code = "# note\nkey: v"
    val annotated = highlight(code, Languages.Yaml, theme)
    val commentEnd = code.indexOf('\n')
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == commentEnd && it.item.color == commentColor },
      "expected comment 0..$commentEnd styled with commentColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun base_style_covers_entire_code() {
    val code = "name: value"
    val annotated = highlight(code, Languages.Yaml, theme)
    val baseSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == code.length && it.item.color == baseColor }
    assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
  }
}
