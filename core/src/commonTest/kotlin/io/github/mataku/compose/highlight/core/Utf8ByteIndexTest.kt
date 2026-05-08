package io.github.mataku.compose.highlight.core

import kotlin.test.Test
import kotlin.test.assertEquals

class Utf8ByteIndexTest {
  @Test
  fun ascii_bytes_map_one_to_one() {
    val index = Utf8ByteIndex("hello")
    assertEquals(0, index.charIndexAt(0))
    assertEquals(3, index.charIndexAt(3))
    assertEquals(5, index.charIndexAt(5))
  }

  @Test
  fun multibyte_japanese_maps_byte_run_to_single_char() {
    val text = "あい"
    val index = Utf8ByteIndex(text)
    assertEquals(0, index.charIndexAt(0))
    assertEquals(0, index.charIndexAt(1))
    assertEquals(0, index.charIndexAt(2))
    assertEquals(1, index.charIndexAt(3))
    assertEquals(2, index.charIndexAt(6))
  }

  @Test
  fun emoji_surrogate_pair_maps_bytes_to_high_surrogate_index() {
    val text = "a😀b"
    val index = Utf8ByteIndex(text)
    assertEquals(0, index.charIndexAt(0))
    assertEquals(1, index.charIndexAt(1))
    assertEquals(1, index.charIndexAt(4))
    assertEquals(3, index.charIndexAt(5))
  }

  @Test
  fun byte_offset_past_end_clamps() {
    val index = Utf8ByteIndex("ab")
    assertEquals(2, index.charIndexAt(99))
  }

  @Test
  fun byteIndexAt_ascii_is_identity() {
    val index = Utf8ByteIndex("hello")
    assertEquals(0, index.byteIndexAt(0))
    assertEquals(3, index.byteIndexAt(3))
    assertEquals(5, index.byteIndexAt(5))
  }

  @Test
  fun byteIndexAt_japanese_three_bytes_per_char() {
    val index = Utf8ByteIndex("あい")
    assertEquals(0, index.byteIndexAt(0))
    assertEquals(3, index.byteIndexAt(1))
    assertEquals(6, index.byteIndexAt(2))
  }

  @Test
  fun byteIndexAt_emoji_surrogate_pair_returns_byte_at_high_surrogate() {
    val index = Utf8ByteIndex("a😀b")
    assertEquals(0, index.byteIndexAt(0))
    assertEquals(1, index.byteIndexAt(1))
    assertEquals(5, index.byteIndexAt(3))
    assertEquals(6, index.byteIndexAt(4))
  }

  @Test
  fun byteIndexAt_clamps_out_of_range() {
    val index = Utf8ByteIndex("ab")
    assertEquals(2, index.byteIndexAt(99))
    assertEquals(0, index.byteIndexAt(-5))
  }

  @Test
  fun line_count_for_empty_string_is_one() {
    assertEquals(1, Utf8ByteIndex("").lineCount)
  }

  @Test
  fun line_starts_for_three_line_string() {
    val index = Utf8ByteIndex("ab\ncd\nef")
    assertEquals(3, index.lineCount)
    assertEquals(0, index.lineStartByte(0))
    assertEquals(3, index.lineStartByte(1))
    assertEquals(6, index.lineStartByte(2))
  }

  @Test
  fun line_at_returns_correct_line_for_byte() {
    val index = Utf8ByteIndex("ab\ncd\nef")
    assertEquals(0, index.lineAt(0))
    assertEquals(0, index.lineAt(2))
    assertEquals(1, index.lineAt(3))
    assertEquals(1, index.lineAt(5))
    assertEquals(2, index.lineAt(6))
  }

  @Test
  fun line_starts_with_multibyte_chars() {
    val index = Utf8ByteIndex("あ\nい")
    assertEquals(2, index.lineCount)
    assertEquals(0, index.lineStartByte(0))
    assertEquals(4, index.lineStartByte(1))
  }

  @Test
  fun byte_size_is_utf8_length() {
    assertEquals(0, Utf8ByteIndex("").byteSize)
    assertEquals(5, Utf8ByteIndex("hello").byteSize)
    assertEquals(6, Utf8ByteIndex("あい").byteSize)
  }
}
