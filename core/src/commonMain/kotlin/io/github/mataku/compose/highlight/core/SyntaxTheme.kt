package io.github.mataku.compose.highlight.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Maps tree-sitter highlight-query capture names to [SpanStyle] values.
 *
 * @property baseStyle Applied to the entire string before any capture-specific style. Use this
 *   to set the default text color and font properties.
 * @property styles Capture-name-to-style overrides. Keys are tree-sitter capture names such as
 *   `keyword`, `string`, or `string.escape`. Resolution falls back along dotted prefixes — see
 *   [resolve].
 * @property background Optional background color hint for the surrounding container. Not
 *   applied automatically by [SyntaxHighlightedText]; consumers may read it to color a code
 *   block's container.
 */
data class SyntaxTheme(
    val baseStyle: SpanStyle = SpanStyle(),
    val styles: Map<String, SpanStyle> = emptyMap(),
    val background: Color? = null,
) {
    /**
     * Returns the [SpanStyle] for [captureName], falling back along dotted prefixes.
     *
     * For a capture like `string.escape`, the lookup tries `string.escape`, then `string`, and
     * finally returns `null` if no entry exists. This mirrors how tree-sitter highlight queries
     * group fine-grained captures under broader categories.
     */
    fun resolve(captureName: String): SpanStyle? {
        styles[captureName]?.let { return it }
        var dot = captureName.lastIndexOf('.')
        while (dot > 0) {
            val prefix = captureName.substring(0, dot)
            styles[prefix]?.let { return it }
            dot = prefix.lastIndexOf('.')
        }
        return null
    }

    companion object {
        /** VSCode-inspired neutral dark theme. Used as the default for [LocalSyntaxTheme]. */
        val DarkDefault: SyntaxTheme by lazy {
            SyntaxTheme(
                baseStyle = SpanStyle(color = Color(0xFFE0E0E0)),
                styles = mapOf(
                    "keyword" to SpanStyle(color = Color(0xFFC586C0), fontWeight = FontWeight.Bold),
                    "function" to SpanStyle(color = Color(0xFFDCDCAA)),
                    "type" to SpanStyle(color = Color(0xFF4EC9B0)),
                    "string" to SpanStyle(color = Color(0xFFCE9178)),
                    "string.escape" to SpanStyle(color = Color(0xFFD7BA7D)),
                    "number" to SpanStyle(color = Color(0xFFB5CEA8)),
                    "boolean" to SpanStyle(color = Color(0xFF569CD6)),
                    "comment" to SpanStyle(color = Color(0xFF6A9955)),
                    "constant" to SpanStyle(color = Color(0xFF4FC1FF)),
                    "property" to SpanStyle(color = Color(0xFF9CDCFE)),
                    "variable" to SpanStyle(color = Color(0xFF9CDCFE)),
                    "namespace" to SpanStyle(color = Color(0xFF4EC9B0)),
                    "operator" to SpanStyle(color = Color(0xFFD4D4D4)),
                    "punctuation" to SpanStyle(color = Color(0xFFD4D4D4)),
                ),
                background = Color(0xFF1E1E1E),
            )
        }

        /** VSCode-inspired neutral light theme. */
        val LightDefault: SyntaxTheme by lazy {
            SyntaxTheme(
                baseStyle = SpanStyle(color = Color(0xFF1F1F1F)),
                styles = mapOf(
                    "keyword" to SpanStyle(color = Color(0xFFAF00DB), fontWeight = FontWeight.Bold),
                    "function" to SpanStyle(color = Color(0xFF795E26)),
                    "type" to SpanStyle(color = Color(0xFF267F99)),
                    "string" to SpanStyle(color = Color(0xFFA31515)),
                    "string.escape" to SpanStyle(color = Color(0xFFEE0000)),
                    "number" to SpanStyle(color = Color(0xFF098658)),
                    "boolean" to SpanStyle(color = Color(0xFF0000FF)),
                    "comment" to SpanStyle(color = Color(0xFF008000)),
                    "constant" to SpanStyle(color = Color(0xFF0070C1)),
                    "property" to SpanStyle(color = Color(0xFF001080)),
                    "variable" to SpanStyle(color = Color(0xFF001080)),
                    "namespace" to SpanStyle(color = Color(0xFF267F99)),
                    "operator" to SpanStyle(color = Color(0xFF000000)),
                    "punctuation" to SpanStyle(color = Color(0xFF000000)),
                ),
                background = Color(0xFFFFFFFF),
            )
        }

        /** Ethan Schoonover's Solarized Dark (base03 background). Attribution in META-INF/NOTICE. */
        val SolarizedDark: SyntaxTheme by lazy {
            SyntaxTheme(
                baseStyle = SpanStyle(color = Color(0xFF839496)),
                styles = mapOf(
                    "keyword" to SpanStyle(color = Color(0xFF859900), fontWeight = FontWeight.Bold),
                    "function" to SpanStyle(color = Color(0xFF268BD2)),
                    "type" to SpanStyle(color = Color(0xFFB58900)),
                    "string" to SpanStyle(color = Color(0xFF2AA198)),
                    "string.escape" to SpanStyle(color = Color(0xFFCB4B16)),
                    "number" to SpanStyle(color = Color(0xFFD33682)),
                    "boolean" to SpanStyle(color = Color(0xFF6C71C4)),
                    "comment" to SpanStyle(color = Color(0xFF586E75)),
                    "constant" to SpanStyle(color = Color(0xFFCB4B16)),
                    "property" to SpanStyle(color = Color(0xFF268BD2)),
                    "variable" to SpanStyle(color = Color(0xFF839496)),
                    "namespace" to SpanStyle(color = Color(0xFFB58900)),
                    "operator" to SpanStyle(color = Color(0xFF93A1A1)),
                    "punctuation" to SpanStyle(color = Color(0xFF586E75)),
                ),
                background = Color(0xFF002B36),
            )
        }

        /** Ethan Schoonover's Solarized Light (base3 background). Attribution in META-INF/NOTICE. */
        val SolarizedLight: SyntaxTheme by lazy {
            SyntaxTheme(
                baseStyle = SpanStyle(color = Color(0xFF657B83)),
                styles = mapOf(
                    "keyword" to SpanStyle(color = Color(0xFF859900), fontWeight = FontWeight.Bold),
                    "function" to SpanStyle(color = Color(0xFF268BD2)),
                    "type" to SpanStyle(color = Color(0xFFB58900)),
                    "string" to SpanStyle(color = Color(0xFF2AA198)),
                    "string.escape" to SpanStyle(color = Color(0xFFCB4B16)),
                    "number" to SpanStyle(color = Color(0xFFD33682)),
                    "boolean" to SpanStyle(color = Color(0xFF6C71C4)),
                    "comment" to SpanStyle(color = Color(0xFF93A1A1)),
                    "constant" to SpanStyle(color = Color(0xFFCB4B16)),
                    "property" to SpanStyle(color = Color(0xFF268BD2)),
                    "variable" to SpanStyle(color = Color(0xFF657B83)),
                    "namespace" to SpanStyle(color = Color(0xFFB58900)),
                    "operator" to SpanStyle(color = Color(0xFF586E75)),
                    "punctuation" to SpanStyle(color = Color(0xFF93A1A1)),
                ),
                background = Color(0xFFFDF6E3),
            )
        }

        /** GitHub Primer Dark syntax tokens. Attribution in META-INF/NOTICE. */
        val GitHubDark: SyntaxTheme by lazy {
            SyntaxTheme(
                baseStyle = SpanStyle(color = Color(0xFFC9D1D9)),
                styles = mapOf(
                    "keyword" to SpanStyle(color = Color(0xFFFF7B72), fontWeight = FontWeight.Bold),
                    "function" to SpanStyle(color = Color(0xFFD2A8FF)),
                    "type" to SpanStyle(color = Color(0xFFFFA657)),
                    "string" to SpanStyle(color = Color(0xFFA5D6FF)),
                    "string.escape" to SpanStyle(color = Color(0xFF79C0FF)),
                    "number" to SpanStyle(color = Color(0xFF79C0FF)),
                    "boolean" to SpanStyle(color = Color(0xFF79C0FF)),
                    "comment" to SpanStyle(color = Color(0xFF8B949E)),
                    "constant" to SpanStyle(color = Color(0xFF79C0FF)),
                    "property" to SpanStyle(color = Color(0xFF79C0FF)),
                    "variable" to SpanStyle(color = Color(0xFFFFA657)),
                    "namespace" to SpanStyle(color = Color(0xFFFF7B72)),
                    "operator" to SpanStyle(color = Color(0xFFFF7B72)),
                    "punctuation" to SpanStyle(color = Color(0xFFC9D1D9)),
                ),
                background = Color(0xFF0D1117),
            )
        }

        /** GitHub Primer Light syntax tokens. Attribution in META-INF/NOTICE. */
        val GitHubLight: SyntaxTheme by lazy {
            SyntaxTheme(
                baseStyle = SpanStyle(color = Color(0xFF1F2328)),
                styles = mapOf(
                    "keyword" to SpanStyle(color = Color(0xFFCF222E), fontWeight = FontWeight.Bold),
                    "function" to SpanStyle(color = Color(0xFF8250DF)),
                    "type" to SpanStyle(color = Color(0xFF953800)),
                    "string" to SpanStyle(color = Color(0xFF0A3069)),
                    "string.escape" to SpanStyle(color = Color(0xFF0550AE)),
                    "number" to SpanStyle(color = Color(0xFF0550AE)),
                    "boolean" to SpanStyle(color = Color(0xFF0550AE)),
                    "comment" to SpanStyle(color = Color(0xFF6E7781)),
                    "constant" to SpanStyle(color = Color(0xFF0550AE)),
                    "property" to SpanStyle(color = Color(0xFF0550AE)),
                    "variable" to SpanStyle(color = Color(0xFF953800)),
                    "namespace" to SpanStyle(color = Color(0xFFCF222E)),
                    "operator" to SpanStyle(color = Color(0xFFCF222E)),
                    "punctuation" to SpanStyle(color = Color(0xFF1F2328)),
                ),
                background = Color(0xFFFFFFFF),
            )
        }

        /** Atom One Dark (atom/atom one-dark-syntax). Attribution in META-INF/NOTICE. */
        val OneDark: SyntaxTheme by lazy {
            SyntaxTheme(
                baseStyle = SpanStyle(color = Color(0xFFABB2BF)),
                styles = mapOf(
                    "keyword" to SpanStyle(color = Color(0xFFC678DD), fontWeight = FontWeight.Bold),
                    "function" to SpanStyle(color = Color(0xFF61AFEF)),
                    "type" to SpanStyle(color = Color(0xFFE5C07B)),
                    "string" to SpanStyle(color = Color(0xFF98C379)),
                    "string.escape" to SpanStyle(color = Color(0xFF56B6C2)),
                    "number" to SpanStyle(color = Color(0xFFD19A66)),
                    "boolean" to SpanStyle(color = Color(0xFFD19A66)),
                    "comment" to SpanStyle(color = Color(0xFF5C6370)),
                    "constant" to SpanStyle(color = Color(0xFFD19A66)),
                    "property" to SpanStyle(color = Color(0xFFE06C75)),
                    "variable" to SpanStyle(color = Color(0xFFE06C75)),
                    "namespace" to SpanStyle(color = Color(0xFFE5C07B)),
                    "operator" to SpanStyle(color = Color(0xFFC678DD)),
                    "punctuation" to SpanStyle(color = Color(0xFFABB2BF)),
                ),
                background = Color(0xFF282C34),
            )
        }

        /** Atom One Light (atom/atom one-light-syntax). Attribution in META-INF/NOTICE. */
        val OneLight: SyntaxTheme by lazy {
            SyntaxTheme(
                baseStyle = SpanStyle(color = Color(0xFF383A42)),
                styles = mapOf(
                    "keyword" to SpanStyle(color = Color(0xFFA626A4), fontWeight = FontWeight.Bold),
                    "function" to SpanStyle(color = Color(0xFF4078F2)),
                    "type" to SpanStyle(color = Color(0xFFC18401)),
                    "string" to SpanStyle(color = Color(0xFF50A14F)),
                    "string.escape" to SpanStyle(color = Color(0xFF0184BC)),
                    "number" to SpanStyle(color = Color(0xFF986801)),
                    "boolean" to SpanStyle(color = Color(0xFF986801)),
                    "comment" to SpanStyle(color = Color(0xFFA0A1A7)),
                    "constant" to SpanStyle(color = Color(0xFF986801)),
                    "property" to SpanStyle(color = Color(0xFFE45649)),
                    "variable" to SpanStyle(color = Color(0xFFE45649)),
                    "namespace" to SpanStyle(color = Color(0xFFC18401)),
                    "operator" to SpanStyle(color = Color(0xFFA626A4)),
                    "punctuation" to SpanStyle(color = Color(0xFF383A42)),
                ),
                background = Color(0xFFFAFAFA),
            )
        }

        /** Dracula (dark only — there is no canonical light variant). Attribution in META-INF/NOTICE. */
        val Dracula: SyntaxTheme by lazy {
            SyntaxTheme(
                baseStyle = SpanStyle(color = Color(0xFFF8F8F2)),
                styles = mapOf(
                    "keyword" to SpanStyle(color = Color(0xFFFF79C6), fontWeight = FontWeight.Bold),
                    "function" to SpanStyle(color = Color(0xFF50FA7B)),
                    "type" to SpanStyle(color = Color(0xFF8BE9FD)),
                    "string" to SpanStyle(color = Color(0xFFF1FA8C)),
                    "string.escape" to SpanStyle(color = Color(0xFFFFB86C)),
                    "number" to SpanStyle(color = Color(0xFFBD93F9)),
                    "boolean" to SpanStyle(color = Color(0xFFBD93F9)),
                    "comment" to SpanStyle(color = Color(0xFF6272A4)),
                    "constant" to SpanStyle(color = Color(0xFFBD93F9)),
                    "property" to SpanStyle(color = Color(0xFF50FA7B)),
                    "variable" to SpanStyle(color = Color(0xFFF8F8F2)),
                    "namespace" to SpanStyle(color = Color(0xFF8BE9FD)),
                    "operator" to SpanStyle(color = Color(0xFFFF79C6)),
                    "punctuation" to SpanStyle(color = Color(0xFFF8F8F2)),
                ),
                background = Color(0xFF282A36),
            )
        }
    }
}
