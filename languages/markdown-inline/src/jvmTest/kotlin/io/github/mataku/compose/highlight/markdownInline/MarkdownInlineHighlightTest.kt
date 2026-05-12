package io.github.mataku.compose.highlight.markdownInline

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertTrue

class MarkdownInlineHighlightTest {

  private val emphasisColor = Color.Red
  private val strongColor = Color.Magenta
  private val literalColor = Color.Yellow
  private val escapeColor = Color.Cyan
  private val baseColor = Color.White
  private val punctuationColor = Color.Gray

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = baseColor),
    stringEscape = SpanStyle(color = escapeColor),
    punctuation = SpanStyle(color = punctuationColor),
    extras = mapOf(
      "text.emphasis" to SpanStyle(color = emphasisColor),
      "text.strong" to SpanStyle(color = strongColor),
      "text.literal" to SpanStyle(color = literalColor),
    ),
  )

  @Test
  fun emphasis_is_styled() {
    val code = "*x*"
    val annotated = highlight(code, Languages.MarkdownInline, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == code.length && it.item.color == emphasisColor },
      "expected emphasis 0..${code.length} styled with emphasisColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun strong_is_styled() {
    val code = "**x**"
    val annotated = highlight(code, Languages.MarkdownInline, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == code.length && it.item.color == strongColor },
      "expected strong 0..${code.length} styled with strongColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun code_span_is_styled() {
    val code = "`code`"
    val annotated = highlight(code, Languages.MarkdownInline, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == code.length && it.item.color == literalColor },
      "expected code_span 0..${code.length} styled with literalColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun backslash_escape_is_styled() {
    val code = "\\*"
    val annotated = highlight(code, Languages.MarkdownInline, theme)
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == code.length && it.item.color == escapeColor },
      "expected backslash_escape 0..${code.length} styled with escapeColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun base_style_covers_entire_code() {
    val code = "*x*"
    val annotated = highlight(code, Languages.MarkdownInline, theme)
    val baseSpan = annotated.spanStyles.firstOrNull {
      it.start == 0 && it.end == code.length && it.item.color == baseColor
    }
    assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
  }
}
