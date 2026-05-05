package io.github.mataku.compose.highlight.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

data class SyntaxTheme(
    val baseStyle: SpanStyle = SpanStyle(),
    val styles: Map<String, SpanStyle> = emptyMap(),
    val background: Color? = null,
) {
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
    }
}
