package io.github.mataku.compose.syntax.core

import androidx.compose.ui.text.SpanStyle

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
}
