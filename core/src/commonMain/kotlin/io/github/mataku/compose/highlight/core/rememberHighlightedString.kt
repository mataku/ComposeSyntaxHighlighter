package io.github.mataku.compose.highlight.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString

@Composable
fun rememberHighlightedString(
    code: String,
    language: Language,
    theme: SyntaxTheme,
): AnnotatedString = remember(code, language, theme) {
    highlight(code, language, theme)
}
