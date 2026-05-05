package io.github.mataku.compose.highlight.core

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

    @Test
    fun dark_default_provides_keyword_string_comment_styles() {
        val theme = SyntaxTheme.DarkDefault
        assertEquals(true, theme.styles.containsKey("keyword"))
        assertEquals(true, theme.styles.containsKey("string"))
        assertEquals(true, theme.styles.containsKey("comment"))
    }

    @Test
    fun light_default_resolves_keyword_function_via_parent_fallback() {
        val theme = SyntaxTheme.LightDefault
        assertEquals(theme.styles["keyword"], theme.resolve("keyword.function"))
    }

    @Test
    fun background_defaults_to_null_for_user_constructed_theme() {
        assertNull(SyntaxTheme().background)
        assertNull(SyntaxTheme(styles = mapOf("keyword" to keywordStyle)).background)
    }

    @Test
    fun dark_default_carries_canonical_background() {
        assertEquals(Color(0xFF1E1E1E), SyntaxTheme.DarkDefault.background)
    }

    @Test
    fun light_default_carries_canonical_background() {
        assertEquals(Color(0xFFFFFFFF), SyntaxTheme.LightDefault.background)
    }

    @Test
    fun solarized_dark_provides_required_capture_keys_and_background() {
        val theme = SyntaxTheme.SolarizedDark
        assertEquals(true, theme.styles.containsKey("keyword"))
        assertEquals(true, theme.styles.containsKey("string"))
        assertEquals(true, theme.styles.containsKey("comment"))
        assertEquals(theme.styles["keyword"], theme.resolve("keyword.return"))
        assertEquals(Color(0xFF002B36), theme.background)
    }

    @Test
    fun solarized_light_provides_required_capture_keys_and_background() {
        val theme = SyntaxTheme.SolarizedLight
        assertEquals(true, theme.styles.containsKey("keyword"))
        assertEquals(true, theme.styles.containsKey("string"))
        assertEquals(true, theme.styles.containsKey("comment"))
        assertEquals(theme.styles["keyword"], theme.resolve("keyword.return"))
        assertEquals(Color(0xFFFDF6E3), theme.background)
    }
}
