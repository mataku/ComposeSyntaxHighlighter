package io.github.mataku.compose.syntax.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SyntaxThemeTest {
    private val keywordStyle = SpanStyle(color = Color.Red)
    private val keywordReturnStyle = SpanStyle(color = Color.Magenta)

    @Test
    fun exact_match_wins() {
        val theme = SyntaxTheme(styles = mapOf("keyword" to keywordStyle))
        assertEquals(keywordStyle, theme.resolve("keyword"))
    }

    @Test
    fun specific_capture_overrides_parent() {
        val theme = SyntaxTheme(
            styles = mapOf(
                "keyword" to keywordStyle,
                "keyword.return" to keywordReturnStyle,
            ),
        )
        assertEquals(keywordReturnStyle, theme.resolve("keyword.return"))
    }

    @Test
    fun missing_specific_falls_back_to_parent() {
        val theme = SyntaxTheme(styles = mapOf("keyword" to keywordStyle))
        assertEquals(keywordStyle, theme.resolve("keyword.function"))
        assertEquals(keywordStyle, theme.resolve("keyword.function.builtin"))
    }

    @Test
    fun no_match_returns_null() {
        val theme = SyntaxTheme(styles = mapOf("keyword" to keywordStyle))
        assertNull(theme.resolve("string"))
        assertNull(theme.resolve("string.escape"))
    }
}
