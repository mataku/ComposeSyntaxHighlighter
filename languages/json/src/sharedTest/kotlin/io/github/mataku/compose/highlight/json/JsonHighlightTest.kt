package io.github.mataku.compose.highlight.json

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertTrue

class JsonHighlightTest {

  private val stringColor = Color.Green
  private val numberColor = Color.Yellow
  private val constantColor = Color.Magenta
  private val commentColor = Color.Blue
  private val baseColor = Color.White

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = baseColor),
    string = SpanStyle(color = stringColor),
    number = SpanStyle(color = numberColor),
    constant = SpanStyle(color = constantColor),
    comment = SpanStyle(color = commentColor),
  )

  @Test
  fun object_key_is_styled_as_string() {
    val code = "{\"name\": 1}"
    val annotated = highlight(code, Languages.Json, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 1 && it.end == 7 && it.item.color == stringColor },
      "expected key 1..7 styled with stringColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun string_value_is_styled() {
    val code = "{\"a\": \"hi\"}"
    val annotated = highlight(code, Languages.Json, theme)
    val start = code.indexOf("\"hi\"")
    assertTrue(
      annotated.spanStyles.any { it.start == start && it.end == start + 4 && it.item.color == stringColor },
      "expected string $start..${start + 4} styled with stringColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun number_is_styled() {
    val code = "[42]"
    val annotated = highlight(code, Languages.Json, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 1 && it.end == 3 && it.item.color == numberColor },
      "expected number 1..3 styled with numberColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun literal_constants_are_styled() {
    val code = "[true, false, null]"
    val annotated = highlight(code, Languages.Json, theme)
    listOf("true", "false", "null").forEach { literal ->
      val start = code.indexOf(literal)
      val end = start + literal.length
      assertTrue(
        annotated.spanStyles.any { it.start == start && it.end == end && it.item.color == constantColor },
        "expected $literal $start..$end styled with constantColor; spans=${annotated.spanStyles}",
      )
    }
  }

  @Test
  fun comment_is_styled() {
    val code = "// note\n{}"
    val annotated = highlight(code, Languages.Json, theme)
    val commentEnd = code.indexOf('\n')
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == commentEnd && it.item.color == commentColor },
      "expected comment 0..$commentEnd styled with commentColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun base_style_covers_entire_code() {
    val code = "{\"a\": 1}"
    val annotated = highlight(code, Languages.Json, theme)
    val baseSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == code.length && it.item.color == baseColor }
    assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
  }
}
