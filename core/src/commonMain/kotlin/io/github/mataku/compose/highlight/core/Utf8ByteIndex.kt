package io.github.mataku.compose.highlight.core

internal class Utf8ByteIndex(text: String) {
  private val byteToChar: IntArray
  private val totalBytes: Int

  init {
    val builder = ArrayList<Int>(text.length + 1)
    var byteCount = 0
    var i = 0
    while (i < text.length) {
      val cp = text.codePointAtCompat(i)
      val bytes = utf8Length(cp)
      repeat(bytes) { builder.add(i) }
      byteCount += bytes
      i += if (cp >= 0x10000) 2 else 1
    }
    builder.add(text.length)
    byteToChar = IntArray(builder.size) { builder[it] }
    totalBytes = byteCount
  }

  fun charIndexAt(byteOffset: Int): Int {
    val b = byteOffset.coerceIn(0, totalBytes)
    return byteToChar[b]
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
