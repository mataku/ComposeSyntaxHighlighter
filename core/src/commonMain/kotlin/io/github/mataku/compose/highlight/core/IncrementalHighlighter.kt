package io.github.mataku.compose.highlight.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi
import io.github.mataku.compose.highlight.api.Language
import io.github.treesitter.ktreesitter.Parser
import io.github.treesitter.ktreesitter.Query
import io.github.treesitter.ktreesitter.Tree

/**
 * A stateful, single-threaded highlighter that reuses prior parse / capture / span work
 * across successive [update] calls. Use this for editor-style scenarios where the source
 * code changes incrementally; for one-shot or whole-file recomposition the existing
 * [highlight] / [rememberHighlightedString] paths remain the right choice.
 *
 * Thread-safety: instances are **not** thread-safe. Every [update] and [close] call must
 * happen on the same thread (or be externally serialised). Sharing an instance across a
 * UI thread and a background dispatcher is unsafe — the engine holds its own
 * tree-sitter [Query] cursor whose state would be corrupted by concurrent access.
 *
 * The first call to [update] runs the same code path as [highlight] (full parse + full
 * query iteration). Subsequent calls reuse the previous parse tree via tree-sitter's
 * incremental parse, run the highlights query only over the byte range affected by the
 * edit, and splice the resulting captures into the cached span list. When the new code
 * equals the previous code, [update] short-circuits to a theme re-resolve only.
 *
 * Instances must be closed; closing is idempotent. After closing, [update] throws
 * [IllegalStateException]. Native memory of the underlying [Tree] / [Parser] / [Query]
 * is reclaimed by ktreesitter's `Cleaner` once the engine becomes garbage-collectible.
 */
@OptIn(InternalSyntaxHighlightApi::class)
class IncrementalHighlighter(
  private val language: Language,
) : AutoCloseable {

  private val parser: Parser = Parser(language.parser)
  private val query: Query = Query(language.parser, language.highlightsQuery)

  private var oldCode: String = ""
  private var oldTree: Tree? = null
  private val captureSpans: MutableList<CaptureSpan> = mutableListOf()
  private var closed: Boolean = false

  fun update(newCode: String, theme: SyntaxTheme): AnnotatedString {
    check(!closed) { "IncrementalHighlighter has been closed" }
    when {
      oldTree == null -> runFirstCall(newCode)
      newCode == oldCode -> Unit // theme-only short-circuit: state already current
      // Incremental path lands in Task 6.
    }
    return assembleAnnotatedString(newCode, theme)
  }

  override fun close() {
    closed = true
  }

  private fun runFirstCall(newCode: String) {
    val tree = parser.parse(newCode)
    captureSpans.clear()
    query.byteRange = UInt.MIN_VALUE..UInt.MAX_VALUE
    query.captures(tree.rootNode).forEach { (_, match) ->
      match.captures.forEach { capture ->
        captureSpans += CaptureSpan(
          startByte = capture.node.startByte.toInt(),
          endByte = capture.node.endByte.toInt(),
          captureName = capture.name,
        )
      }
    }
    oldCode = newCode
    oldTree = tree
  }

  private fun assembleAnnotatedString(text: String, theme: SyntaxTheme): AnnotatedString {
    val byteToChar = Utf8ByteIndex(text)
    val emptySpan = SpanStyle()
    val ranges = ArrayList<AnnotatedString.Range<SpanStyle>>(captureSpans.size + 1)
    if (theme.baseStyle != emptySpan && text.isNotEmpty()) {
      ranges += AnnotatedString.Range(theme.baseStyle, 0, text.length)
    }
    for (span in captureSpans) {
      val style = theme.resolve(span.captureName) ?: continue
      val charStart = byteToChar.charIndexAt(span.startByte)
      val charEnd = byteToChar.charIndexAt(span.endByte)
      if (charStart < charEnd) {
        ranges += AnnotatedString.Range(style, charStart, charEnd)
      }
    }
    return AnnotatedString(text = text, spanStyles = ranges, paragraphStyles = emptyList())
  }
}
