package io.github.mataku.compose.highlight.core

import io.github.mataku.compose.highlight.api.Injection
import io.github.mataku.compose.highlight.api.Language
import io.github.treesitter.ktreesitter.Node
import io.github.treesitter.ktreesitter.Parser
import io.github.treesitter.ktreesitter.QueryCapture
import io.github.treesitter.ktreesitter.QueryCursor
import io.github.treesitter.ktreesitter.Tree

// The cursor yields a match once per capture it holds, so reading every capture of each yielded match would visit a pattern's captures repeatedly.
internal inline fun QueryCursor.forEachCapture(action: (QueryCapture) -> Unit) {
  captures().forEach { (index, match) -> action(match.captures[index.toInt()]) }
}

internal fun collectCaptures(
  code: String,
  tree: Tree,
  language: Language,
  sink: CaptureSink,
) {
  val byteToChar = Utf8ByteIndex(code)
  language.query.boundedCursor(tree.rootNode).forEachCapture { capture ->
    val start = byteToChar.charIndexAt(capture.node.startByte.toInt())
    val end = byteToChar.charIndexAt(capture.node.endByte.toInt())
    if (start < end) sink.add(start, end, capture.name)
  }
  for (injection in language.injections) {
    collectInjection(code, tree, byteToChar, injection, sink, parentCharOffset = 0)
  }
}

private fun collectInjection(
  code: String,
  parentTree: Tree,
  parentByteToChar: Utf8ByteIndex,
  injection: Injection,
  sink: CaptureSink,
  parentCharOffset: Int,
) {
  val parser = Parser(injection.target.parser)
  injection.injectionsQuery.boundedCursor(parentTree.rootNode).forEachContentNode { node ->
    val charStart = parentByteToChar.charIndexAt(node.startByte.toInt())
    val charEnd = parentByteToChar.charIndexAt(node.endByte.toInt())
    if (charStart < charEnd) {
      collectInjectedContent(parser, code.substring(charStart, charEnd), injection.target, parentCharOffset + charStart, sink)
    }
  }
}

/** Visits each node an injections query captures as `injection.content`. */
internal inline fun QueryCursor.forEachContentNode(action: (Node) -> Unit) {
  forEachCapture { capture ->
    if (capture.name == INJECTION_CONTENT_CAPTURE) action(capture.node)
  }
}

/**
 * Parses [content] with [parser], which must be set to [target], and reports the captures of [target] and its nested injections.
 * Offsets are UTF-16 offsets into [content] shifted by [charOffset].
 */
internal fun collectInjectedContent(
  parser: Parser,
  content: String,
  target: Language,
  charOffset: Int,
  sink: CaptureSink,
) {
  val tree = parser.parseSource(content)
  val byteToChar = Utf8ByteIndex(content)
  target.query.boundedCursor(tree.rootNode).forEachCapture { capture ->
    val start = byteToChar.charIndexAt(capture.node.startByte.toInt())
    val end = byteToChar.charIndexAt(capture.node.endByte.toInt())
    if (start < end) sink.add(charOffset + start, charOffset + end, capture.name)
  }
  for (nested in target.injections) {
    collectInjection(content, tree, byteToChar, nested, sink, charOffset)
  }
}

private const val INJECTION_CONTENT_CAPTURE = "injection.content"
