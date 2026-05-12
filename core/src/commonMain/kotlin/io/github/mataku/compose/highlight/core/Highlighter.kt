package io.github.mataku.compose.highlight.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import io.github.mataku.compose.highlight.api.Injection
import io.github.mataku.compose.highlight.api.Language
import io.github.treesitter.ktreesitter.Parser
import io.github.treesitter.ktreesitter.Tree

/**
 * Returns an [AnnotatedString] of [code] with [theme] styles applied to tree-sitter capture
 * spans defined by [language]. Each capture name is resolved through [SyntaxTheme.resolve],
 * which falls back along dotted prefixes (e.g. `string.escape` -> `string`) before yielding
 * `null`.
 *
 * Constructs a fresh tree-sitter [Parser] on every call. Compose callers should prefer
 * [rememberHighlightedString] or [SyntaxHighlightedText], both of which memoize the result.
 */
fun highlight(
  code: String,
  language: Language,
  theme: SyntaxTheme,
): AnnotatedString {
  val parser = Parser(language.parser)
  val tree = parser.parse(code)
  return applyStyles(code, tree, language, theme)
}

/**
 * Builds the styled [AnnotatedString] for [code] given a pre-parsed [tree]. Internal entry
 * point shared by [highlight] (one-shot) and the Composable wrappers (which cache the tree
 * across theme-only changes).
 */
internal fun applyStyles(
  code: String,
  tree: Tree,
  language: Language,
  theme: SyntaxTheme,
): AnnotatedString {
  val byteToChar = Utf8ByteIndex(code)
  val emptySpan = SpanStyle()

  return buildAnnotatedString {
    append(code)
    if (theme.baseStyle != emptySpan && code.isNotEmpty()) {
      addStyle(theme.baseStyle, 0, code.length)
    }
    language.query(tree.rootNode).captures().forEach { (_, match) ->
      match.captures.forEach { capture ->
        val style = theme.resolve(capture.name) ?: return@forEach
        val start = byteToChar.charIndexAt(capture.node.startByte.toInt())
        val end = byteToChar.charIndexAt(capture.node.endByte.toInt())
        if (start < end) addStyle(style, start, end)
      }
    }
    if (language.injections.isNotEmpty()) {
      for (injection in language.injections) {
        applyInjection(code, tree, byteToChar, injection, theme, parentCharOffset = 0)
      }
    }
  }
}

private fun AnnotatedString.Builder.applyInjection(
  code: String,
  parentTree: Tree,
  parentByteToChar: Utf8ByteIndex,
  injection: Injection,
  theme: SyntaxTheme,
  parentCharOffset: Int,
) {
  injection.injectionsQuery(parentTree.rootNode).captures().forEach { (_, match) ->
    match.captures.forEach { capture ->
      if (capture.name != INJECTION_CONTENT_CAPTURE) return@forEach
      val byteStart = capture.node.startByte.toInt()
      val byteEnd = capture.node.endByte.toInt()
      val charStart = parentByteToChar.charIndexAt(byteStart)
      val charEnd = parentByteToChar.charIndexAt(byteEnd)
      if (charStart >= charEnd) return@forEach

      val substring = code.substring(charStart, charEnd)
      val subParser = Parser(injection.target.parser)
      val subTree = subParser.parse(substring)
      val subByteToChar = Utf8ByteIndex(substring)
      val absoluteCharOffset = parentCharOffset + charStart

      injection.target.query(subTree.rootNode).captures().forEach { (_, subMatch) ->
        subMatch.captures.forEach { subCapture ->
          val style = theme.resolve(subCapture.name) ?: return@forEach
          val sStart = subByteToChar.charIndexAt(subCapture.node.startByte.toInt())
          val sEnd = subByteToChar.charIndexAt(subCapture.node.endByte.toInt())
          if (sStart < sEnd) {
            addStyle(style, absoluteCharOffset + sStart, absoluteCharOffset + sEnd)
          }
        }
      }

      if (injection.target.injections.isNotEmpty()) {
        for (nested in injection.target.injections) {
          applyInjection(
            code = substring,
            parentTree = subTree,
            parentByteToChar = subByteToChar,
            injection = nested,
            theme = theme,
            parentCharOffset = absoluteCharOffset,
          )
        }
      }
    }
  }
}

private const val INJECTION_CONTENT_CAPTURE = "injection.content"
