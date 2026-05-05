package io.github.mataku.compose.syntax.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import io.github.treesitter.ktreesitter.Parser

fun highlight(
    code: String,
    language: Language,
    theme: SyntaxTheme,
): AnnotatedString {
    val parser = Parser(language.parser)
    val tree = parser.parse(code)
    val query = language.parser.query(language.highlightsQuery)
    val byteToChar = Utf8ByteIndex(code)
    val emptySpan = SpanStyle()

    return buildAnnotatedString {
        append(code)
        if (theme.baseStyle != emptySpan) {
            addStyle(theme.baseStyle, 0, code.length)
        }
        query.captures(tree.rootNode).forEach { (_, match) ->
            match.captures.forEach { capture ->
                val style = theme.resolve(capture.name) ?: return@forEach
                val start = byteToChar.charIndexAt(capture.node.startByte.toInt())
                val end = byteToChar.charIndexAt(capture.node.endByte.toInt())
                if (start < end) addStyle(style, start, end)
            }
        }
    }
}
