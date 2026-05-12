package io.github.mataku.compose.highlight.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertTrue

class MarkdownHighlightTest {

  private val baseColor = Color.White
  private val titleColor = Color.Red
  private val literalColor = Color.Yellow
  private val uriColor = Color.Cyan
  private val emphasisColor = Color.Magenta
  private val strongColor = Color.Blue
  private val escapeColor = Color.Green
  private val punctuationColor = Color.Gray

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = baseColor),
    punctuation = SpanStyle(color = punctuationColor),
    stringEscape = SpanStyle(color = escapeColor),
    extras = mapOf(
      "text.title" to SpanStyle(color = titleColor),
      "text.literal" to SpanStyle(color = literalColor),
      "text.uri" to SpanStyle(color = uriColor),
      "text.emphasis" to SpanStyle(color = emphasisColor),
      "text.strong" to SpanStyle(color = strongColor),
    ),
  )

  @Test
  fun atx_heading_inline_is_styled() {
    val code = "# Hello\n"
    val annotated = highlight(code, Languages.Markdown, theme)
    val titleStart = code.indexOf("Hello")
    val titleEnd = titleStart + "Hello".length
    assertTrue(
      annotated.spanStyles.any { it.start == titleStart && it.end == titleEnd && it.item.color == titleColor },
      "expected heading inline $titleStart..$titleEnd styled with titleColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun fenced_code_delimiters_are_styled() {
    val code = "```\nbody\n```\n"
    val annotated = highlight(code, Languages.Markdown, theme)
    val openStart = 0
    val openEnd = 3
    val closeStart = code.indexOf("```", startIndex = 4)
    val closeEnd = closeStart + 3
    assertTrue(
      annotated.spanStyles.any { it.start == openStart && it.end == openEnd && it.item.color == punctuationColor },
      "expected opening fence $openStart..$openEnd styled with punctuationColor; spans=${annotated.spanStyles}",
    )
    assertTrue(
      annotated.spanStyles.any { it.start == closeStart && it.end == closeEnd && it.item.color == punctuationColor },
      "expected closing fence $closeStart..$closeEnd styled with punctuationColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun paragraph_emphasis_is_styled_via_injection() {
    val code = "a *b* c\n"
    val annotated = highlight(code, Languages.Markdown, theme)
    val emphasisStart = code.indexOf("*b*")
    val emphasisEnd = emphasisStart + "*b*".length
    assertTrue(
      annotated.spanStyles.any { it.start == emphasisStart && it.end == emphasisEnd && it.item.color == emphasisColor },
      "expected emphasis $emphasisStart..$emphasisEnd styled with emphasisColor via injection; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun paragraph_strong_is_styled_via_injection() {
    val code = "a **b** c\n"
    val annotated = highlight(code, Languages.Markdown, theme)
    val strongStart = code.indexOf("**b**")
    val strongEnd = strongStart + "**b**".length
    assertTrue(
      annotated.spanStyles.any { it.start == strongStart && it.end == strongEnd && it.item.color == strongColor },
      "expected strong $strongStart..$strongEnd styled with strongColor via injection; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun paragraph_inline_code_is_styled_via_injection() {
    val code = "a `b` c\n"
    val annotated = highlight(code, Languages.Markdown, theme)
    val codeStart = code.indexOf("`b`")
    val codeEnd = codeStart + "`b`".length
    assertTrue(
      annotated.spanStyles.any { it.start == codeStart && it.end == codeEnd && it.item.color == literalColor },
      "expected inline code $codeStart..$codeEnd styled with literalColor via injection; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun link_destination_is_styled() {
    val code = "[label]: https://example.com\n"
    val annotated = highlight(code, Languages.Markdown, theme)
    val uriStart = code.indexOf("https://example.com")
    val uriEnd = uriStart + "https://example.com".length
    assertTrue(
      annotated.spanStyles.any { it.start == uriStart && it.end == uriEnd && it.item.color == uriColor },
      "expected link destination $uriStart..$uriEnd styled with uriColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun multibyte_paragraph_offsets_are_correct() {
    val code = "日本語 *x* 末尾\n"
    val annotated = highlight(code, Languages.Markdown, theme)
    val emphasisStart = code.indexOf("*x*")
    val emphasisEnd = emphasisStart + "*x*".length
    assertTrue(
      annotated.spanStyles.any { it.start == emphasisStart && it.end == emphasisEnd && it.item.color == emphasisColor },
      "expected emphasis $emphasisStart..$emphasisEnd in multibyte paragraph styled with emphasisColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun base_style_covers_entire_code() {
    val code = "# Title\n"
    val annotated = highlight(code, Languages.Markdown, theme)
    val baseSpan = annotated.spanStyles.firstOrNull {
      it.start == 0 && it.end == code.length && it.item.color == baseColor
    }
    assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
  }
}
