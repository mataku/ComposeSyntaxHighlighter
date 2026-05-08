package io.github.mataku.compose.highlight.core

internal data class CaptureSpan(
  val startByte: Int,
  val endByte: Int,
  val captureName: String,
)

internal data class ByteRange(val start: Int, val endExclusive: Int) {
  init {
    require(start <= endExclusive) { "start=$start must be <= endExclusive=$endExclusive" }
  }
}

/**
 * Take the smallest [ByteRange] that contains [edited] and every range in [changed].
 * Returns [edited] unchanged when [changed] is empty.
 */
internal fun unionRange(edited: ByteRange, changed: List<IntRange>): ByteRange {
  if (changed.isEmpty()) return edited
  var min = edited.start
  var max = edited.endExclusive
  for (r in changed) {
    if (r.first < min) min = r.first
    if (r.last + 1 > max) max = r.last + 1
  }
  return ByteRange(min, max)
}

/** In-place: shift `startByte`/`endByte` of every span at or after [after] by [delta]. */
internal fun shiftCaptureSpansSuffix(
  spans: MutableList<CaptureSpan>,
  after: Int,
  delta: Int,
) {
  if (delta == 0) return
  for (i in spans.indices) {
    val s = spans[i]
    if (s.startByte >= after) {
      spans[i] = s.copy(startByte = s.startByte + delta, endByte = s.endByte + delta)
    }
  }
}

/** In-place: remove every span overlapping [range] (uses `endByte > range.start && startByte < range.endExclusive`). */
internal fun removeCaptureSpansOverlapping(
  spans: MutableList<CaptureSpan>,
  range: ByteRange,
) {
  var w = 0
  for (i in spans.indices) {
    val s = spans[i]
    val overlaps = s.endByte > range.start && s.startByte < range.endExclusive
    if (!overlaps) {
      if (w != i) spans[w] = s
      w++
    }
  }
  while (spans.size > w) spans.removeAt(spans.size - 1)
}

/** Insert [span] preserving sort order by `(startByte, endByte)`. */
internal fun insertSorted(spans: MutableList<CaptureSpan>, span: CaptureSpan) {
  var lo = 0
  var hi = spans.size
  while (lo < hi) {
    val mid = (lo + hi) ushr 1
    val cmp = compareSpans(spans[mid], span)
    if (cmp <= 0) lo = mid + 1 else hi = mid
  }
  spans.add(lo, span)
}

private fun compareSpans(a: CaptureSpan, b: CaptureSpan): Int {
  val byStart = a.startByte.compareTo(b.startByte)
  if (byStart != 0) return byStart
  return a.endByte.compareTo(b.endByte)
}
