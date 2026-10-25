package io.github.mataku.compose.highlight.core

import io.github.mataku.compose.highlight.api.Injection
import io.github.mataku.compose.highlight.api.Language
import io.github.treesitter.ktreesitter.Parser
import io.github.treesitter.ktreesitter.Tree

internal fun collectCaptures(
  code: String,
  tree: Tree,
  language: Language,
  sink: CaptureSink,
) {
  val byteToChar = Utf8ByteIndex(code)
  language.query(tree.rootNode).captures().forEach { (_, match) ->
    match.captures.forEach { capture ->
      val start = byteToChar.charIndexAt(capture.node.startByte.toInt())
      val end = byteToChar.charIndexAt(capture.node.endByte.toInt())
      if (start < end) sink.add(start, end, capture.name)
    }
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
  injection.injectionsQuery(parentTree.rootNode).captures().forEach { (_, match) ->
    match.captures.forEach { capture ->
      if (capture.name != INJECTION_CONTENT_CAPTURE) return@forEach
      val charStart = parentByteToChar.charIndexAt(capture.node.startByte.toInt())
      val charEnd = parentByteToChar.charIndexAt(capture.node.endByte.toInt())
      if (charStart >= charEnd) return@forEach

      val substring = code.substring(charStart, charEnd)
      val subTree = Parser(injection.target.parser).parse(substring)
      val subByteToChar = Utf8ByteIndex(substring)
      val absoluteCharOffset = parentCharOffset + charStart

      injection.target.query(subTree.rootNode).captures().forEach { (_, subMatch) ->
        subMatch.captures.forEach { subCapture ->
          val sStart = subByteToChar.charIndexAt(subCapture.node.startByte.toInt())
          val sEnd = subByteToChar.charIndexAt(subCapture.node.endByte.toInt())
          if (sStart < sEnd) {
            sink.add(absoluteCharOffset + sStart, absoluteCharOffset + sEnd, subCapture.name)
          }
        }
      }

      for (nested in injection.target.injections) {
        collectInjection(substring, subTree, subByteToChar, nested, sink, absoluteCharOffset)
      }
    }
  }
}

private const val INJECTION_CONTENT_CAPTURE = "injection.content"
