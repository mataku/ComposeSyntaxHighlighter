package io.github.mataku.compose.syntax.core

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle

@Composable
fun rememberHighlightedString(
    code: String,
    language: Language,
    theme: SyntaxTheme,
): AnnotatedString = remember(code, language, theme) {
    highlight(code, language, theme)
}

@Composable
fun SyntaxHighlightedText(
    code: String,
    language: Language,
    theme: SyntaxTheme,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        text = rememberHighlightedString(code, language, theme),
        modifier = modifier,
        style = style,
    )
}
