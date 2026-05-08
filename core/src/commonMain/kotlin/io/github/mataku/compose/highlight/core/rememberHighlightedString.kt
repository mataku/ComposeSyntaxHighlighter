package io.github.mataku.compose.highlight.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import io.github.mataku.compose.highlight.api.Language
import io.github.treesitter.ktreesitter.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Composable wrapper around [highlight] that caches the parsed tree-sitter [Tree] across
 * theme-only changes. The first stage is keyed on `(code, language)` and re-parses only when
 * one of those changes; the second stage applies [theme] to the cached tree. Native memory of
 * the dropped tree is reclaimed by the JVM `Cleaner` registered in ktreesitter's `Parser`/`Tree`
 * `init` blocks (no explicit disposal API exists).
 */
@Composable
fun rememberHighlightedString(
  code: String,
  language: Language,
  theme: SyntaxTheme,
): AnnotatedString {
  val tree = remember(code, language) {
    Parser(language.parser).parse(code)
  }
  return remember(tree, theme) {
    applyStyles(code, tree, language, theme)
  }
}

/**
 * Async variant of [rememberHighlightedString] that runs [highlight] off the calling thread.
 *
 * The returned [State] holds the plain [code] (with [SyntaxTheme.baseStyle] applied) until the
 * highlight computation finishes, then updates to the styled [AnnotatedString]. Use this for
 * code blocks large enough that synchronous highlighting drops frames during recomposition;
 * for typical inline code the synchronous [rememberHighlightedString] is the right choice.
 *
 * When any of [code], [language], [theme], or [context] changes, the previous coroutine is
 * cancelled, the state immediately resets to the plain-text representation of the new [code],
 * and a fresh highlight computation is scheduled on [context]. An in-flight native `parse()`
 * call cannot be cancelled mid-flight (it is a JNI call); only the Kotlin coroutine is
 * cancelled, so its result is discarded but the native parse runs to completion.
 *
 * Unlike [rememberHighlightedString], the async variant does not cache the parsed tree across
 * theme-only changes — sharing a Tree across coroutines would require [io.github.treesitter.ktreesitter.Tree.copy]
 * for thread safety, which is not worth the complexity for the rare large-file + theme-toggle
 * combination. Theme changes therefore re-trigger a full async parse.
 *
 * @param context Coroutine context the highlight runs on. Defaults to [Dispatchers.Default]
 *   (the right choice for CPU-bound work). Override for tests or to share a thread pool.
 */
@Composable
fun rememberHighlightedStringAsync(
  code: String,
  language: Language,
  theme: SyntaxTheme,
  context: CoroutineContext = Dispatchers.Default,
): State<AnnotatedString> = produceState(
  initialValue = plainHighlightedString(code, theme),
  code,
  language,
  theme,
  context,
) {
  value = plainHighlightedString(code, theme)
  value = withContext(context) { highlight(code, language, theme) }
}

internal fun plainHighlightedString(code: String, theme: SyntaxTheme): AnnotatedString = buildAnnotatedString {
  append(code)
  if (theme.baseStyle != SpanStyle() && code.isNotEmpty()) {
    addStyle(theme.baseStyle, 0, code.length)
  }
}
