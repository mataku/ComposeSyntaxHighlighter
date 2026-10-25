package io.github.mataku.compose.highlight.core

import io.github.mataku.compose.highlight.api.Language
import io.github.treesitter.ktreesitter.Parser

/**
 * Returns the theme-independent highlight of [code] split into lines, with spans carrying tree-sitter capture names from [language].
 * Style a line with [toAnnotatedString].
 *
 * Lines are separated by `\n` only.
 * One trailing `\r` is stripped from each line, so CRLF input yields the same text and numbering as LF input.
 * There is no line after a final `\n`.
 *
 * [lineRanges] selects lines by 1-based, inclusive ranges; `null` selects every line.
 * Ranges are clipped to the existing lines and overlapping or adjacent ranges are merged, so each selected line is returned once, in ascending order.
 * When the selection is empty, nothing is parsed and an empty list is returned.
 *
 * Syntax errors in [code] never throw; the grammar's error recovery still yields captures for the parts it understood.
 * On JVM and Android, native library loading failures are not caught here: they surface at the first access of `Languages.<Name>` as `ExceptionInInitializerError`, and on later accesses as `NoClassDefFoundError`.
 * Both are `LinkageError`s.
 *
 * This function is pure and holds no shared state, so it can be called concurrently, for example from `Dispatchers.Default`.
 * It constructs a fresh tree-sitter [Parser] on every call.
 */
fun highlightLines(
  code: String,
  language: Language,
  lineRanges: List<IntRange>? = null,
): List<HighlightedLine> {
  val builder = LineHighlightBuilder(code, lineRanges)
  if (builder.isEmptySelection) return emptyList()
  val tree = Parser(language.parser).parse(code)
  collectCaptures(code, tree, language, builder)
  return builder.build()
}
