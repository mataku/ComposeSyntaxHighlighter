package io.github.mataku.compose.highlight.rust

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.core.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RustHighlightTest {

    private val keywordColor = Color.Red
    private val stringColor = Color.Green
    private val commentColor = Color.Blue
    private val numberColor = Color.Yellow
    private val baseColor = Color.White

    private val theme = SyntaxTheme(
        baseStyle = SpanStyle(color = baseColor),
        styles = mapOf(
            "keyword" to SpanStyle(color = keywordColor),
            "string" to SpanStyle(color = stringColor),
            "comment" to SpanStyle(color = commentColor),
            // Upstream Rust highlights.scm tags integer/float/boolean literals
            // as @constant.builtin, not @number, so we map that capture path here.
            "constant.builtin" to SpanStyle(color = numberColor),
        ),
    )

    @Test
    fun keyword_fn_is_styled() {
        val code = "fn foo() {}"
        val annotated = highlight(code, RustLanguage, theme)
        val keywordSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == 2 }
        assertEquals(keywordColor, keywordSpan?.item?.color, "expected keyword 0..2 styled with keywordColor; spans=${annotated.spanStyles}")
    }

    @Test
    fun string_literal_is_styled() {
        val code = """let s = "hi";"""
        val annotated = highlight(code, RustLanguage, theme)
        assertTrue(
            annotated.spanStyles.any { it.item.color == stringColor },
            "expected at least one string-styled span; spans=${annotated.spanStyles}",
        )
    }

    @Test
    fun line_comment_is_styled() {
        val code = "// hello\nlet x = 1;"
        val annotated = highlight(code, RustLanguage, theme)
        val commentEnd = code.indexOf('\n')
        assertTrue(
            annotated.spanStyles.any { it.start == 0 && it.end == commentEnd && it.item.color == commentColor },
            "expected comment 0..$commentEnd styled with commentColor; spans=${annotated.spanStyles}",
        )
    }

    @Test
    fun integer_literal_is_styled() {
        val code = "let n = 42;"
        val annotated = highlight(code, RustLanguage, theme)
        val start = code.indexOf("42")
        val end = start + 2
        assertTrue(
            annotated.spanStyles.any { it.start == start && it.end == end && it.item.color == numberColor },
            "expected number $start..$end styled with numberColor; spans=${annotated.spanStyles}",
        )
    }

    @Test
    fun base_style_covers_entire_code() {
        val code = "fn foo() {}"
        val annotated = highlight(code, RustLanguage, theme)
        val baseSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == code.length && it.item.color == baseColor }
        assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
    }

    @Test
    fun languages_extension_returns_canonical_language() {
        assertSame(RustLanguage, Languages.rust)
    }
}
