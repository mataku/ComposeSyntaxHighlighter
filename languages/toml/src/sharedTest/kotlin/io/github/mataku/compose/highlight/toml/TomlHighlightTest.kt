package io.github.mataku.compose.highlight.toml

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TomlHighlightTest {

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
  fun bare_key_is_styled_last_as_property() {
    val code = "name = 1"
    val annotated = highlight(code, Languages.Toml, theme.copy(type = SpanStyle(color = Color.Cyan)))
    val last = annotated.spanStyles.last { it.start <= 0 && it.end >= 4 }
    assertEquals(propertyColor, last.item.color, "spans=${annotated.spanStyles}")
  }

  @Test
  fun dotted_key_segments_are_styled_as_property() {
    val code = "server.port = 8080"
    val annotated = highlight(code, Languages.Toml, theme)
    listOf(0 to 6, 7 to 11).forEach { (start, end) ->
      assertTrue(
        annotated.spanStyles.any { it.start == start && it.end == end && it.item.color == propertyColor },
        "expected key segment $start..$end styled with propertyColor; spans=${annotated.spanStyles}",
      )
    }
  }

  @Test
  fun string_value_is_styled_last_over_its_range() {
    val code = "name = \"demo\""
    val annotated = highlight(code, Languages.Toml, theme)
    val last = annotated.spanStyles.last { it.start <= 7 && it.end >= code.length }
    assertEquals(stringColor, last.item.color, "spans=${annotated.spanStyles}")
  }

  @Test
  fun integer_is_styled() {
    val code = "port = 8080"
    val annotated = highlight(code, Languages.Toml, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 7 && it.end == 11 && it.item.color == numberColor },
      "expected integer 7..11 styled with numberColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun boolean_is_styled() {
    val code = "debug = true"
    val annotated = highlight(code, Languages.Toml, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 8 && it.end == 12 && it.item.color == booleanColor },
      "expected boolean 8..12 styled with booleanColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun comment_is_styled() {
    val code = "# note\nport = 1"
    val annotated = highlight(code, Languages.Toml, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == 6 && it.item.color == commentColor },
      "expected comment 0..6 styled with commentColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun base_style_covers_entire_code() {
    val code = "[package]\nname = \"x\""
    val annotated = highlight(code, Languages.Toml, theme)
    val baseSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == code.length && it.item.color == baseColor }
    assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
  }
}
