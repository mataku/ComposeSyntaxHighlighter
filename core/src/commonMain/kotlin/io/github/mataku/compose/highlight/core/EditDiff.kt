package io.github.mataku.compose.highlight.core

import io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi
import io.github.treesitter.ktreesitter.InputEdit
import io.github.treesitter.ktreesitter.Point

@InternalSyntaxHighlightApi
internal fun synthesiseInputEdit(
  oldCode: String,
  oldIndex: Utf8ByteIndex,
  newCode: String,
  newIndex: Utf8ByteIndex,
): InputEdit {
  val maxPrefix = minOf(oldCode.length, newCode.length)
  var prefix = 0
  while (prefix < maxPrefix && oldCode[prefix] == newCode[prefix]) prefix++

  val maxSuffix = minOf(oldCode.length - prefix, newCode.length - prefix)
  var suffix = 0
  while (
    suffix < maxSuffix &&
    oldCode[oldCode.length - 1 - suffix] == newCode[newCode.length - 1 - suffix]
  ) {
    suffix++
  }

  val oldEndChar = oldCode.length - suffix
  val newEndChar = newCode.length - suffix

  val startByte = oldIndex.byteIndexAt(prefix)
  val oldEndByte = oldIndex.byteIndexAt(oldEndChar)
  val newEndByte = newIndex.byteIndexAt(newEndChar)

  return InputEdit(
    startByte = startByte.toUInt(),
    oldEndByte = oldEndByte.toUInt(),
    newEndByte = newEndByte.toUInt(),
    startPoint = pointFor(oldIndex, startByte),
    oldEndPoint = pointFor(oldIndex, oldEndByte),
    newEndPoint = pointFor(newIndex, newEndByte),
  )
}

private fun pointFor(index: Utf8ByteIndex, byteOffset: Int): Point {
  val line = index.lineAt(byteOffset)
  val column = byteOffset - index.lineStartByte(line)
  return Point(line.toUInt(), column.toUInt())
}
