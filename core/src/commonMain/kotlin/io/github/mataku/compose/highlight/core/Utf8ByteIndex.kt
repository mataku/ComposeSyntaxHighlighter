package io.github.mataku.compose.highlight.core

@io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi
class Utf8ByteIndex(text: String) {
  private val byteToChar: IntArray
  private val charToByte: IntArray
  private val lineStartBytes: IntArray
  private val totalBytes: Int

  init {
    val byteToCharBuilder = ArrayList<Int>(text.length + 1)
    val charToByteBuilder = IntArray(text.length + 1)
    val lineStarts = ArrayList<Int>().apply { add(0) }
    var byteCount = 0
    var i = 0
    while (i < text.length) {
      charToByteBuilder[i] = byteCount
      val cp = text.codePointAtCompat(i)
      val bytes = utf8Length(cp)
      repeat(bytes) { byteToCharBuilder.add(i) }
      byteCount += bytes
      val advance = if (cp >= 0x10000) 2 else 1
      if (advance == 2 && i + 1 < text.length) {
        charToByteBuilder[i + 1] = byteCount
      }
      if (cp == '\n'.code) {
        lineStarts.add(byteCount)
      }
      i += advance
    }
    charToByteBuilder[text.length] = byteCount
    byteToCharBuilder.add(text.length)
    byteToChar = IntArray(byteToCharBuilder.size) { byteToCharBuilder[it] }
    charToByte = charToByteBuilder
    lineStartBytes = lineStarts.toIntArray()
    totalBytes = byteCount
  }

  fun charIndexAt(byteOffset: Int): Int {
    val b = byteOffset.coerceIn(0, totalBytes)
    return byteToChar[b]
  }

  fun byteIndexAt(charOffset: Int): Int {
    val c = charOffset.coerceIn(0, charToByte.size - 1)
    return charToByte[c]
  }

  /** Total UTF-8 byte length of the source text. */
  val byteSize: Int get() = totalBytes

  /** Number of lines (1 for empty string, otherwise count of `\n` + 1). */
  val lineCount: Int get() = lineStartBytes.size

  /** Byte offset where line `line` starts (0-based). */
  fun lineStartByte(line: Int): Int = lineStartBytes[line.coerceIn(0, lineStartBytes.lastIndex)]

  /** Line number (0-based) containing the given byte offset. */
  fun lineAt(byteOffset: Int): Int {
    val b = byteOffset.coerceIn(0, totalBytes)
    var lo = 0
    var hi = lineStartBytes.lastIndex
    while (lo < hi) {
      val mid = (lo + hi + 1) ushr 1
      if (lineStartBytes[mid] <= b) lo = mid else hi = mid - 1
    }
    return lo
  }
}

private fun String.codePointAtCompat(index: Int): Int {
  val high = this[index]
  if (high.isHighSurrogate() && index + 1 < length) {
    val low = this[index + 1]
    if (low.isLowSurrogate()) {
      return 0x10000 + ((high.code - 0xD800) shl 10) + (low.code - 0xDC00)
    }
  }
  return high.code
}

private fun utf8Length(cp: Int): Int = when {
  cp < 0x80 -> 1
  cp < 0x800 -> 2
  cp < 0x10000 -> 3
  else -> 4
}
