package io.github.mataku.compose.syntax.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import io.github.treesitter.ktreesitter.Parser

internal fun highlight(
    code: String,
    language: Language,
    theme: SyntaxTheme,
): AnnotatedString {
    val parser = Parser(language.parser)
    val tree = parser.parse(code)
    val query = language.parser.query(language.highlightsQuery)
    val byteToChar = Utf8ByteIndex(code)

    return buildAnnotatedString {
        append(code)
        if (theme.baseStyle != EMPTY_SPAN) {
            addStyle(theme.baseStyle, 0, code.length)
        }
        query.captures(tree.rootNode).forEach { (_, match) ->
            match.captures.forEach { capture ->
                val style = theme.resolve(capture.name) ?: return@forEach
                val start = byteToChar.charIndexAt(capture.node.startByte)
                val end = byteToChar.charIndexAt(capture.node.endByte)
                if (start < end) addStyle(style, start, end)
            }
        }
    }
}

private val EMPTY_SPAN = androidx.compose.ui.text.SpanStyle()

internal class Utf8ByteIndex(text: String) {
    private val byteToChar: IntArray
    private val totalBytes: Int

    init {
        val builder = ArrayList<Int>(text.length + 1)
        var byteCount = 0
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAtCompat(i)
            val bytes = utf8Length(cp)
            repeat(bytes) { builder.add(i) }
            byteCount += bytes
            i += if (cp >= 0x10000) 2 else 1
        }
        builder.add(text.length)
        byteToChar = IntArray(builder.size) { builder[it] }
        totalBytes = byteCount
    }

    fun charIndexAt(byteOffset: UInt): Int {
        val b = byteOffset.toInt().coerceIn(0, totalBytes)
        return byteToChar[b]
    }
}

private fun String.codePointAtCompat(index: Int): Int {
    val high = this[index]
    if (high.isHighSurrogate() && index + 1 < length) {
        val low = this[index + 1]
        if (low.isLowSurrogate()) {
            return 0x10000 + ((high.code - 0xD800) shl 10) + (low.code - 0xDC00)
        }
    }
    return high.code
}

private fun utf8Length(cp: Int): Int = when {
    cp < 0x80 -> 1
    cp < 0x800 -> 2
    cp < 0x10000 -> 3
    else -> 4
}
