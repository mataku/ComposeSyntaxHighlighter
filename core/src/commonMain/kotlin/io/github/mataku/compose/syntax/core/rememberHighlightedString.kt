package io.github.mataku.compose.syntax.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString

@Composable
expect fun rememberHighlightedString(
    code: String,
    language: Language,
    theme: SyntaxTheme,
): AnnotatedString
