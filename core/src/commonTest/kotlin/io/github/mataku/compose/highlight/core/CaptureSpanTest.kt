package io.github.mataku.compose.highlight.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CaptureSpanTest {

  @Test
  fun unionRange_empty_changed_returns_edited() {
    val edited = ByteRange(10, 20)
    assertEquals(edited, unionRange(edited, emptyList()))
  }

  @Test
  fun unionRange_expands_to_cover_changed_ranges() {
    val edited = ByteRange(10, 20)
    val changed = listOf(5..7, 15..30)
    assertEquals(ByteRange(5, 31), unionRange(edited, changed))
  }

  @Test
  fun unionRange_changed_inside_edited_returns_edited() {
    val edited = ByteRange(0, 100)
    val changed = listOf(10..20)
    assertEquals(edited, unionRange(edited, changed))
  }

  @Test
  fun byteRange_rejects_inverted_bounds() {
    assertFailsWith<IllegalArgumentException> { ByteRange(10, 5) }
  }

  @Test
  fun shift_suffix_moves_spans_at_or_after_after() {
    val spans = mutableListOf(
      CaptureSpan(0, 5, "a"),
      CaptureSpan(10, 15, "b"),
      CaptureSpan(20, 25, "c"),
    )
    shiftCaptureSpansSuffix(spans, after = 10, delta = 3)
    assertEquals(
      listOf(
        CaptureSpan(0, 5, "a"),
        CaptureSpan(13, 18, "b"),
        CaptureSpan(23, 28, "c"),
      ),
      spans,
    )
  }

  @Test
  fun shift_suffix_zero_delta_is_noop() {
    val spans = mutableListOf(CaptureSpan(0, 5, "a"))
    shiftCaptureSpansSuffix(spans, after = 0, delta = 0)
    assertEquals(listOf(CaptureSpan(0, 5, "a")), spans)
  }

  @Test
  fun shift_suffix_negative_delta_for_deletion() {
    val spans = mutableListOf(
      CaptureSpan(0, 5, "a"),
      CaptureSpan(10, 15, "b"),
    )
    shiftCaptureSpansSuffix(spans, after = 6, delta = -3)
    assertEquals(
      listOf(
        CaptureSpan(0, 5, "a"),
        CaptureSpan(7, 12, "b"),
      ),
      spans,
    )
  }

  @Test
  fun remove_overlapping_drops_spans_touching_range() {
    val spans = mutableListOf(
      CaptureSpan(0, 5, "before"),
      CaptureSpan(8, 12, "left_overlap"),
      CaptureSpan(11, 13, "inside"),
      CaptureSpan(13, 17, "right_overlap"),
      CaptureSpan(20, 25, "after"),
    )
    removeCaptureSpansOverlapping(spans, ByteRange(10, 15))
    assertEquals(
      listOf(
        CaptureSpan(0, 5, "before"),
        CaptureSpan(20, 25, "after"),
      ),
      spans,
    )
  }

  @Test
  fun remove_overlapping_keeps_adjacent_non_overlapping() {
    val spans = mutableListOf(
      CaptureSpan(0, 10, "ends_at_start"),
      CaptureSpan(15, 25, "starts_at_end"),
    )
    removeCaptureSpansOverlapping(spans, ByteRange(10, 15))
    assertEquals(
      listOf(
        CaptureSpan(0, 10, "ends_at_start"),
        CaptureSpan(15, 25, "starts_at_end"),
      ),
      spans,
    )
  }

  @Test
  fun insert_sorted_places_by_start_then_end() {
    val spans = mutableListOf(
      CaptureSpan(0, 5, "a"),
      CaptureSpan(10, 15, "c"),
    )
    insertSorted(spans, CaptureSpan(5, 10, "b"))
    insertSorted(spans, CaptureSpan(10, 12, "c_short"))
    assertEquals(
      listOf(
        CaptureSpan(0, 5, "a"),
        CaptureSpan(5, 10, "b"),
        CaptureSpan(10, 12, "c_short"),
        CaptureSpan(10, 15, "c"),
      ),
      spans,
    )
  }
}
