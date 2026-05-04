package io.github.mataku.compose.syntax.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

internal fun baseAnnotated(code: String, theme: SyntaxTheme): AnnotatedString =
    buildAnnotatedString {
        append(code)
        if (theme.baseStyle != SpanStyle()) {
            addStyle(theme.baseStyle, 0, code.length)
        }
    }

internal suspend fun highlightAsync(
    code: String,
    language: Language,
    theme: SyntaxTheme,
): AnnotatedString {
    val module = WebTreeSitterRuntime.module()
    val wtsLanguage = WebTreeSitterRuntime.loadGrammar(language.key, language.grammarBytesProvider)
    val parser = wtsCreateParser(module)
    wtsParserSetLanguage(parser, wtsLanguage)
    val tree = wtsParserParse(parser, code)
    val query = wtsLanguageQuery(wtsLanguage, language.highlightsQuery)
    val emptySpan = SpanStyle()
    val codeLength = code.length

    val annotated = buildAnnotatedString {
        append(code)
        if (theme.baseStyle != emptySpan) {
            addStyle(theme.baseStyle, 0, code.length)
        }
        val captures = wtsQueryCaptures(query, wtsTreeRootNode(tree))
        val count = captures.length
        for (i in 0 until count) {
            val capture = wtsCaptureAt(captures, i)
            val style = theme.resolve(capture.name) ?: continue
            val start = (capture.node.startIndex / 2).coerceIn(0, codeLength)
            val end = (capture.node.endIndex / 2).coerceIn(0, codeLength)
            if (start < end) addStyle(style, start, end)
        }
    }

    wtsQueryDelete(query)
    wtsTreeDelete(tree)
    wtsParserDelete(parser)

    return annotated
}
