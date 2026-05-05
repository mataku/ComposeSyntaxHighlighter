package io.github.mataku.compose.highlight.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

data class SyntaxTheme(
    val baseStyle: SpanStyle = SpanStyle(),
    val styles: Map<String, SpanStyle> = emptyMap(),
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
        fun darkDefault(): SyntaxTheme = SyntaxTheme(
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
        )

        fun lightDefault(): SyntaxTheme = SyntaxTheme(
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
        )
    }
}
