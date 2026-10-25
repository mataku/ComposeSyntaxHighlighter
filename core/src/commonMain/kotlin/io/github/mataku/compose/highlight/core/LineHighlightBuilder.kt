package io.github.mataku.compose.highlight.core

internal fun interface CaptureSink {
  fun add(charStart: Int, charEnd: Int, captureName: String)
}

internal class LineHighlightBuilder(
  private val code: String,
  lineRanges: List<IntRange>?,
) : CaptureSink {
  private val lineStarts: IntArray = computeLineStarts(code)

  val lineCount: Int = when {
    code.isEmpty() -> 0
    code.last() == '\n' -> lineStarts.size - 1
    else -> lineStarts.size
  }

  private val selected: BooleanArray = BooleanArray(lineCount)

  private val spansByLine: Array<ArrayList<HighlightSpan>?> = arrayOfNulls(lineCount)

  val isEmptySelection: Boolean

  init {
    if (lineRanges == null) {
      selected.fill(true)
    } else {
      for (range in lineRanges) {
        val first = maxOf(range.first, 1)
        val last = minOf(range.last, lineCount)
        for (lineNumber in first..last) {
          selected[lineNumber - 1] = true
        }
      }
    }
    var any = false
    for (index in 0 until lineCount) {
      if (selected[index]) {
        any = true
        spansByLine[index] = ArrayList()
      }
    }
    isEmptySelection = !any
  }

  override fun add(charStart: Int, charEnd: Int, captureName: String) {
    if (charStart >= charEnd || lineCount == 0) return
    var index = lineIndexOf(charStart)
    while (index < lineCount && lineStarts[index] < charEnd) {
      val spans = spansByLine[index]
      if (spans != null) {
        val lineStart = lineStarts[index]
        val start = maxOf(charStart, lineStart)
        val end = minOf(charEnd, textEnd(index))
        if (start < end) {
          spans.add(HighlightSpan(start - lineStart, end - lineStart, captureName))
        }
      }
      index++
    }
  }

  fun build(): List<HighlightedLine> {
    val result = ArrayList<HighlightedLine>()
    for (index in 0 until lineCount) {
      val spans = spansByLine[index] ?: continue
      result.add(
        HighlightedLine(
          lineNumber = index + 1,
          text = code.substring(lineStarts[index], textEnd(index)),
          spans = spans.toList(),
        ),
      )
    }
    return result
  }

  private fun lineIndexOf(offset: Int): Int {
    var low = 0
    var high = lineStarts.size - 1
    while (low < high) {
      val mid = (low + high + 1) ushr 1
      if (lineStarts[mid] <= offset) low = mid else high = mid - 1
    }
    return low
  }

  private fun textEnd(index: Int): Int {
    val start = lineStarts[index]
    val isTerminated = index + 1 < lineStarts.size
    if (!isTerminated) return code.length
    var end = lineStarts[index + 1] - 1
    if (end > start && code[end - 1] == '\r') end--
    return end
  }

  private companion object {
    fun computeLineStarts(code: String): IntArray {
      var count = 1
      for (char in code) {
        if (char == '\n') count++
      }
      val starts = IntArray(count)
      var next = 1
      for (index in code.indices) {
        if (code[index] == '\n') starts[next++] = index + 1
      }
      return starts
    }
  }
}
