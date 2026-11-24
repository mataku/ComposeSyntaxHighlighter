package io.github.mataku.compose.highlight.core

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Injection
import io.github.treesitter.ktreesitter.Node
import io.github.treesitter.ktreesitter.Parser
import io.github.treesitter.ktreesitter.QueryCursor
import io.github.treesitter.ktreesitter.Tree

/**
 * The captures of one injected range.
 * [spans] are UTF-16 offsets relative to the range start, so a range whose text is unchanged only moves its byte offsets on an edit.
 */
internal class InjectionBlock(
  var startByte: Int,
  var endByte: Int,
  val spans: List<HighlightSpan>,
)

/**
 * Keeps the captures of one [Injection] across [IncrementalHighlighter] updates, as blocks in document order.
 * An edit re-parses only the injected ranges that intersect the affected bytes; the other blocks are shifted.
 */
internal class IncrementalInjection(private val injection: Injection) {
  private val parser = Parser(injection.target.parser)
  private var blocks: MutableList<InjectionBlock> = mutableListOf()

  fun rebuild(code: String, index: Utf8ByteIndex, tree: Tree) {
    blocks = contentNodes(injection.injectionsQuery(tree.rootNode)).mapNotNullTo(ArrayList()) { blockFor(code, index, it) }
  }

  fun update(
    code: String,
    index: Utf8ByteIndex,
    tree: Tree,
    affectedRange: ByteRange,
    editOldEndByte: Int,
    delta: Int,
  ) {
    if (delta != 0) {
      for (block in blocks) {
        if (block.startByte >= editOldEndByte) {
          block.startByte += delta
          block.endByte += delta
        }
      }
    }

    val cursor = injection.injectionsQuery(tree.rootNode)
    cursor.byteRange = affectedRange.start.toUInt()..affectedRange.endExclusive.toUInt()
    val nodes = contentNodes(cursor)
    // A re-queried range can reach past the affected bytes, and its old block must go with it.
    var removeStart = affectedRange.start
    var removeEnd = affectedRange.endExclusive
    for (node in nodes) {
      removeStart = minOf(removeStart, node.startByte.toInt())
      removeEnd = maxOf(removeEnd, node.endByte.toInt())
    }
    blocks.removeAll { it.endByte > removeStart && it.startByte < removeEnd }

    val added = nodes.mapNotNull { blockFor(code, index, it) }
    if (added.isNotEmpty()) blocks = mergeByStart(blocks, added)
  }

  fun appendStyles(
    ranges: MutableList<AnnotatedString.Range<SpanStyle>>,
    index: Utf8ByteIndex,
    resolve: (String) -> SpanStyle?,
  ) {
    for (block in blocks) {
      val base = index.charIndexAt(block.startByte)
      for (span in block.spans) {
        val style = resolve(span.captureName) ?: continue
        ranges += AnnotatedString.Range(style, base + span.start, base + span.end)
      }
    }
  }

  private fun contentNodes(cursor: QueryCursor): List<Node> {
    val nodes = ArrayList<Node>()
    cursor.forEachContentNode { nodes += it }
    return nodes
  }

  private fun blockFor(code: String, index: Utf8ByteIndex, node: Node): InjectionBlock? {
    val charStart = index.charIndexAt(node.startByte.toInt())
    val charEnd = index.charIndexAt(node.endByte.toInt())
    if (charStart >= charEnd) return null
    val spans = ArrayList<HighlightSpan>()
    collectInjectedContent(parser, code.substring(charStart, charEnd), injection.target, charOffset = 0) { start, end, name ->
      spans += HighlightSpan(start, end, name)
    }
    return InjectionBlock(node.startByte.toInt(), node.endByte.toInt(), spans)
  }

  private fun mergeByStart(existing: List<InjectionBlock>, added: List<InjectionBlock>): MutableList<InjectionBlock> {
    val merged = ArrayList<InjectionBlock>(existing.size + added.size)
    var next = 0
    for (block in existing) {
      while (next < added.size && added[next].startByte < block.startByte) merged += added[next++]
      merged += block
    }
    while (next < added.size) merged += added[next++]
    return merged
  }
}
