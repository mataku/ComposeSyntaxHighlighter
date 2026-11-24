package io.github.mataku.compose.highlight.core

import io.github.treesitter.ktreesitter.Parser
import io.github.treesitter.ktreesitter.Tree

/**
 * Parses [code] with every lone surrogate replaced by U+FFFD.
 *
 * JVM and Android encode a lone surrogate as `?` (one byte), while Kotlin/Native encodes it as U+FFFD and [Utf8ByteIndex] counts three bytes for it.
 * U+FFFD is three bytes on every target and one UTF-16 unit like the surrogate it replaces, so the tree's byte offsets match [Utf8ByteIndex] of the original [code].
 */
internal fun Parser.parseSource(code: String, oldTree: Tree? = null): Tree = parse(code.replaceLoneSurrogates(), oldTree = oldTree)

internal fun String.replaceLoneSurrogates(): String {
  var replaced: CharArray? = null
  var i = 0
  while (i < length) {
    val c = this[i]
    if (c.isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()) {
      i += 2
      continue
    }
    if (c.isSurrogate()) {
      val chars = replaced ?: toCharArray().also { replaced = it }
      chars[i] = '\uFFFD'
    }
    i++
  }
  return replaced?.concatToString() ?: this
}
