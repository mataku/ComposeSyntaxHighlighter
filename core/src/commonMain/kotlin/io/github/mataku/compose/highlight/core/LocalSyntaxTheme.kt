package io.github.mataku.compose.highlight.core

import androidx.compose.runtime.compositionLocalOf

/**
 * Composition local providing the active [SyntaxTheme]. Defaults to [SyntaxTheme.DarkDefault].
 * [SyntaxHighlightedText] reads from this local when its `theme` parameter is omitted.
 */
val LocalSyntaxTheme = compositionLocalOf { SyntaxTheme.DarkDefault }
