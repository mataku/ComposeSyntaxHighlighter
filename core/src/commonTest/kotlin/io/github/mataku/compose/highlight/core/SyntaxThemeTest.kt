package io.github.mataku.compose.highlight.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SyntaxThemeTest {
  private val keywordStyle = SpanStyle(color = Color.Red)
  private val keywordReturnStyle = SpanStyle(color = Color.Magenta)

  private val bundledThemes = listOf(
    "DarkDefault" to SyntaxTheme.DarkDefault,
    "LightDefault" to SyntaxTheme.LightDefault,
    "SolarizedDark" to SyntaxTheme.SolarizedDark,
    "SolarizedLight" to SyntaxTheme.SolarizedLight,
    "GitHubDark" to SyntaxTheme.GitHubDark,
    "GitHubLight" to SyntaxTheme.GitHubLight,
    "OneDark" to SyntaxTheme.OneDark,
    "OneLight" to SyntaxTheme.OneLight,
    "Dracula" to SyntaxTheme.Dracula,
  )

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

  @Test
  fun every_property_participates_in_equals_and_hashCode() {
    val base = SyntaxTheme()
    val marker = SpanStyle(color = Color.Magenta)
    val mutations = listOf(
      "baseStyle" to base.copy(baseStyle = marker),
      "background" to base.copy(background = Color.Red),
      "keyword" to base.copy(keyword = marker),
      "function" to base.copy(function = marker),
      "type" to base.copy(type = marker),
      "string" to base.copy(string = marker),
      "stringEscape" to base.copy(stringEscape = marker),
      "number" to base.copy(number = marker),
      "boolean" to base.copy(boolean = marker),
      "comment" to base.copy(comment = marker),
      "constant" to base.copy(constant = marker),
      "property" to base.copy(property = marker),
      "variable" to base.copy(variable = marker),
      "namespace" to base.copy(namespace = marker),
      "operator" to base.copy(operator = marker),
      "punctuation" to base.copy(punctuation = marker),
      "extras" to base.copy(extras = mapOf("attribute" to marker)),
    )
    mutations.forEach { (name, mutated) ->
      assertNotEquals(base, mutated, "$name is missing from equals()")
      assertNotEquals(base.hashCode(), mutated.hashCode(), "$name is missing from hashCode()")
    }
  }

  @Test
  fun structurally_equal_themes_agree_on_hashCode() {
    val extras = mapOf("attribute" to SpanStyle(color = Color.Green))
    val a = SyntaxTheme(keyword = keywordStyle, extras = extras)
    val b = SyntaxTheme(keyword = keywordStyle, extras = mapOf("attribute" to SpanStyle(color = Color.Green)))
    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun copy_preserves_unspecified_properties() {
    val original = SyntaxTheme.DarkDefault
    val copy = original.copy(keyword = keywordStyle)
    assertEquals(keywordStyle, copy.keyword)
    assertEquals(original.string, copy.string)
    assertEquals(original.background, copy.background)
    assertEquals(original.extras, copy.extras)
  }

  @Test
  fun alias_captures_resolve_to_their_synonym_field() {
    val theme = SyntaxTheme(
      keyword = keywordStyle,
      string = SpanStyle(color = Color.Yellow),
      stringEscape = SpanStyle(color = Color.Cyan),
      number = SpanStyle(color = Color.Green),
      variable = SpanStyle(color = Color.Blue),
    )
    assertEquals(theme.stringEscape, theme.resolve("escape"))
    assertEquals(theme.number, theme.resolve("float"))
    assertEquals(theme.keyword, theme.resolve("conditional"))
    assertEquals(theme.keyword, theme.resolve("repeat"))
    assertEquals(theme.keyword, theme.resolve("include"))
    assertEquals(theme.keyword, theme.resolve("exception"))
    assertEquals(theme.variable, theme.resolve("parameter"))
    assertEquals(theme.string, theme.resolve("character"))
  }

  @Test
  fun dotted_capture_reaches_its_alias_through_the_prefix_walk() {
    val stringStyle = SpanStyle(color = Color.Yellow)
    val theme = SyntaxTheme(string = stringStyle)
    assertEquals(stringStyle, theme.resolve("character.special"))
  }

  @Test
  fun explicit_extras_entry_wins_over_alias() {
    val explicit = SpanStyle(color = Color.Green)
    val theme = SyntaxTheme(
      number = SpanStyle(color = Color.Red),
      extras = mapOf("float" to explicit),
    )
    assertEquals(explicit, theme.resolve("float"))
  }

  @Test
  fun bundled_themes_style_every_markdown_text_capture() {
    bundledThemes.forEach { (name, theme) ->
      listOf(
        "text.title",
        "text.strong",
        "text.emphasis",
        "text.literal",
        "text.uri",
        "text.reference",
      ).forEach { capture ->
        assertNotNull(theme.resolve(capture), "$name leaves $capture unstyled")
      }
      assertEquals(FontWeight.Bold, theme.resolve("text.title")?.fontWeight, "$name: text.title")
      assertEquals(FontWeight.Bold, theme.resolve("text.strong")?.fontWeight, "$name: text.strong")
      assertEquals(FontStyle.Italic, theme.resolve("text.emphasis")?.fontStyle, "$name: text.emphasis")
      assertEquals(
        TextDecoration.Underline,
        theme.resolve("text.uri")?.textDecoration,
        "$name: text.uri",
      )
    }
  }

  @Test
  fun markdown_defaults_reuse_colors_the_theme_already_defines() {
    val theme = SyntaxTheme.DarkDefault
    assertEquals(theme.keyword?.color, theme.resolve("text.title")?.color)
    assertEquals(theme.string, theme.resolve("text.literal"))
    assertEquals(theme.constant?.color, theme.resolve("text.uri")?.color)
    assertEquals(theme.function, theme.resolve("text.reference"))
  }

  @Test
  fun explicit_extras_override_the_derived_markdown_default() {
    val custom = SpanStyle(color = Color.Magenta)
    val theme = SyntaxTheme.DarkDefault.let {
      it.copy(extras = it.extras + mapOf("text.title" to custom))
    }
    assertEquals(custom, theme.resolve("text.title"))
    assertNotNull(theme.resolve("text.strong"))
  }
}
