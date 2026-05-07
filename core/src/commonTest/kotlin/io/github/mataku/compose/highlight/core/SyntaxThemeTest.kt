package io.github.mataku.compose.highlight.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SyntaxThemeTest {
  private val keywordStyle = SpanStyle(color = Color.Red)
  private val keywordReturnStyle = SpanStyle(color = Color.Magenta)

  @Test
  fun exact_match_wins() {
    val theme = SyntaxTheme(keyword = keywordStyle)
    assertEquals(keywordStyle, theme.resolve("keyword"))
  }

  @Test
  fun specific_capture_overrides_parent() {
    val theme = SyntaxTheme(
      keyword = keywordStyle,
      extras = mapOf("keyword.return" to keywordReturnStyle),
    )
    assertEquals(keywordReturnStyle, theme.resolve("keyword.return"))
  }

  @Test
  fun missing_specific_falls_back_to_parent() {
    val theme = SyntaxTheme(keyword = keywordStyle)
    assertEquals(keywordStyle, theme.resolve("keyword.function"))
    assertEquals(keywordStyle, theme.resolve("keyword.function.builtin"))
  }

  @Test
  fun no_match_returns_null() {
    val theme = SyntaxTheme(keyword = keywordStyle)
    assertNull(theme.resolve("string"))
    assertNull(theme.resolve("string.escape"))
  }

  @Test
  fun string_escape_field_resolves_for_dotted_capture() {
    val escape = SpanStyle(color = Color.Cyan)
    val theme = SyntaxTheme(stringEscape = escape)
    assertEquals(escape, theme.resolve("string.escape"))
  }

  @Test
  fun string_escape_falls_back_to_string_when_unset() {
    val stringStyle = SpanStyle(color = Color.Yellow)
    val theme = SyntaxTheme(string = stringStyle)
    assertEquals(stringStyle, theme.resolve("string.escape"))
  }

  @Test
  fun extras_overrides_typed_field_for_same_name() {
    val typed = SpanStyle(color = Color.Red)
    val override = SpanStyle(color = Color.Blue)
    val theme = SyntaxTheme(
      keyword = typed,
      extras = mapOf("keyword" to override),
    )
    assertEquals(override, theme.resolve("keyword"))
  }

  @Test
  fun extras_provides_capture_with_no_typed_field() {
    val attribute = SpanStyle(color = Color.Green)
    val theme = SyntaxTheme(extras = mapOf("attribute" to attribute))
    assertEquals(attribute, theme.resolve("attribute"))
    assertEquals(attribute, theme.resolve("attribute.builtin"))
  }

  @Test
  fun dark_default_provides_keyword_string_comment_styles() {
    val theme = SyntaxTheme.DarkDefault
    assertNotNull(theme.keyword)
    assertNotNull(theme.string)
    assertNotNull(theme.comment)
  }

  @Test
  fun light_default_resolves_keyword_function_via_parent_fallback() {
    val theme = SyntaxTheme.LightDefault
    assertEquals(theme.keyword, theme.resolve("keyword.function"))
  }

  @Test
  fun background_defaults_to_null_for_user_constructed_theme() {
    assertNull(SyntaxTheme().background)
    assertNull(SyntaxTheme(keyword = keywordStyle).background)
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
    assertNotNull(theme.keyword)
    assertNotNull(theme.string)
    assertNotNull(theme.comment)
    assertEquals(theme.keyword, theme.resolve("keyword.return"))
    assertEquals(Color(0xFF002B36), theme.background)
  }

  @Test
  fun solarized_light_provides_required_capture_keys_and_background() {
    val theme = SyntaxTheme.SolarizedLight
    assertNotNull(theme.keyword)
    assertNotNull(theme.string)
    assertNotNull(theme.comment)
    assertEquals(theme.keyword, theme.resolve("keyword.return"))
    assertEquals(Color(0xFFFDF6E3), theme.background)
  }

  @Test
  fun github_dark_provides_required_capture_keys_and_background() {
    val theme = SyntaxTheme.GitHubDark
    assertNotNull(theme.keyword)
    assertNotNull(theme.string)
    assertNotNull(theme.comment)
    assertEquals(theme.keyword, theme.resolve("keyword.return"))
    assertEquals(Color(0xFF0D1117), theme.background)
  }

  @Test
  fun github_light_provides_required_capture_keys_and_background() {
    val theme = SyntaxTheme.GitHubLight
    assertNotNull(theme.keyword)
    assertNotNull(theme.string)
    assertNotNull(theme.comment)
    assertEquals(theme.keyword, theme.resolve("keyword.return"))
    assertEquals(Color(0xFFFFFFFF), theme.background)
  }

  @Test
  fun one_dark_provides_required_capture_keys_and_background() {
    val theme = SyntaxTheme.OneDark
    assertNotNull(theme.keyword)
    assertNotNull(theme.string)
    assertNotNull(theme.comment)
    assertEquals(theme.keyword, theme.resolve("keyword.return"))
    assertEquals(Color(0xFF282C34), theme.background)
  }

  @Test
  fun one_light_provides_required_capture_keys_and_background() {
    val theme = SyntaxTheme.OneLight
    assertNotNull(theme.keyword)
    assertNotNull(theme.string)
    assertNotNull(theme.comment)
    assertEquals(theme.keyword, theme.resolve("keyword.return"))
    assertEquals(Color(0xFFFAFAFA), theme.background)
  }

  @Test
  fun dracula_provides_required_capture_keys_and_background() {
    val theme = SyntaxTheme.Dracula
    assertNotNull(theme.keyword)
    assertNotNull(theme.string)
    assertNotNull(theme.comment)
    assertEquals(theme.keyword, theme.resolve("keyword.return"))
    assertEquals(Color(0xFF282A36), theme.background)
  }
}
