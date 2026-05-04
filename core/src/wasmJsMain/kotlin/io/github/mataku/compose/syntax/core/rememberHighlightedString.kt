package io.github.mataku.compose.syntax.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.text.AnnotatedString

@Composable
actual fun rememberHighlightedString(
    code: String,
    language: Language,
    theme: SyntaxTheme,
): AnnotatedString {
    val state by produceState(
        initialValue = baseAnnotated(code, theme),
        code,
        language,
        theme,
    ) {
        value = try {
            highlightAsync(code, language, theme)
        } catch (t: Throwable) {
            wtsLogError("compose-syntax: highlightAsync failed for ${language.key}", t)
            baseAnnotated(code, theme)
        }
    }
    return state
}
