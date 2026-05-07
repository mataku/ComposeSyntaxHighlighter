package io.github.mataku.compose.highlight.swift

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SwiftHighlightTest {

  private val keywordColor = Color.Red
  private val stringColor = Color.Green
  private val commentColor = Color.Blue
  private val baseColor = Color.White

  private val theme = SyntaxTheme(
    baseStyle = SpanStyle(color = baseColor),
    keyword = SpanStyle(color = keywordColor),
    string = SpanStyle(color = stringColor),
    comment = SpanStyle(color = commentColor),
  )

  @Test
  fun keyword_func_is_styled() {
    val code = "func foo() {}"
    val annotated = highlight(code, SwiftLanguage, theme)
    val keywordSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == 4 }
    assertEquals(keywordColor, keywordSpan?.item?.color, "expected keyword 0..4 styled with keywordColor; spans=${annotated.spanStyles}")
  }

  @Test
  fun string_literal_is_styled() {
    val code = """let s = "hi""""
    val annotated = highlight(code, SwiftLanguage, theme)
    assertTrue(
      annotated.spanStyles.any { it.item.color == stringColor },
      "expected at least one string-styled span; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun line_comment_is_styled() {
    val code = "// hello\nlet x = 1"
    val annotated = highlight(code, SwiftLanguage, theme)
    val commentEnd = code.indexOf('\n')
    assertTrue(
      annotated.spanStyles.any { it.start == 0 && it.end == commentEnd && it.item.color == commentColor },
      "expected comment 0..$commentEnd styled with commentColor; spans=${annotated.spanStyles}",
    )
  }

  @Test
  fun base_style_covers_entire_code() {
    val code = "func foo() {}"
    val annotated = highlight(code, SwiftLanguage, theme)
    val baseSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == code.length && it.item.color == baseColor }
    assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
  }

  @Test
  fun languages_extension_returns_canonical_language() {
    assertSame(SwiftLanguage, Languages.swift)
  }
}
