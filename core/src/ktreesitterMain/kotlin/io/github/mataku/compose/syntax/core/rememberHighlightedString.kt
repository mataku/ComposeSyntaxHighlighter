package io.github.mataku.compose.syntax.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString

@Composable
actual fun rememberHighlightedString(
    code: String,
    language: Language,
    theme: SyntaxTheme,
): AnnotatedString = remember(code, language, theme) {
    highlight(code, language, theme)
}
