package io.github.mataku.compose.highlight.core

import io.github.treesitter.ktreesitter.InputEdit
import io.github.treesitter.ktreesitter.Point
import kotlin.test.Test
import kotlin.test.assertEquals

class EditDiffTest {

  private fun edit(old: String, new: String): InputEdit = synthesiseInputEdit(old, Utf8ByteIndex(old), new, Utf8ByteIndex(new))

  @Test
  fun insertion_at_end_ascii() {
    val e = edit("abc", "abcd")
    assertEquals(3u, e.startByte)
    assertEquals(3u, e.oldEndByte)
    assertEquals(4u, e.newEndByte)
    assertEquals(Point(0u, 3u), e.startPoint)
    assertEquals(Point(0u, 3u), e.oldEndPoint)
    assertEquals(Point(0u, 4u), e.newEndPoint)
  }

  @Test
  fun insertion_at_start() {
    val e = edit("abc", "Xabc")
    assertEquals(0u, e.startByte)
    assertEquals(0u, e.oldEndByte)
    assertEquals(1u, e.newEndByte)
  }

  @Test
  fun insertion_at_middle() {
    val e = edit("abef", "abcdef")
    assertEquals(2u, e.startByte)
    assertEquals(2u, e.oldEndByte)
    assertEquals(4u, e.newEndByte)
  }

  @Test
  fun deletion_in_middle() {
    val e = edit("abcdef", "abef")
    assertEquals(2u, e.startByte)
    assertEquals(4u, e.oldEndByte)
    assertEquals(2u, e.newEndByte)
  }

  @Test
  fun replacement_in_middle() {
    val e = edit("abXYef", "abZWef")
    assertEquals(2u, e.startByte)
    assertEquals(4u, e.oldEndByte)
    assertEquals(4u, e.newEndByte)
  }

  @Test
  fun no_change_yields_zero_length_edit_at_end() {
    val e = edit("abc", "abc")
    assertEquals(3u, e.startByte)
    assertEquals(3u, e.oldEndByte)
    assertEquals(3u, e.newEndByte)
  }

  @Test
  fun multibyte_insertion_byte_offsets_match_utf8() {
    // Insert "あ" (3 UTF-8 bytes) between "ab" and "cd".
    val e = edit("abcd", "abあcd")
    assertEquals(2u, e.startByte)
    assertEquals(2u, e.oldEndByte)
    assertEquals(5u, e.newEndByte) // 2 + 3 bytes for あ
  }

  @Test
  fun edit_across_lines_produces_correct_points() {
    val e = edit("aa\nbb\ncc", "aa\nbXb\ncc")
    assertEquals(Point(1u, 1u), e.startPoint)
    assertEquals(Point(1u, 1u), e.oldEndPoint)
    assertEquals(Point(1u, 2u), e.newEndPoint)
  }

  @Test
  fun edit_differing_only_in_low_surrogate_covers_the_whole_code_point() {
    // 😀 and 😁 share their high surrogate.
    val e = edit("a😀b", "a😁b")
    assertEquals(1u, e.startByte)
    assertEquals(5u, e.oldEndByte)
    assertEquals(5u, e.newEndByte)
  }

  @Test
  fun edit_differing_only_in_high_surrogate_covers_the_whole_code_point() {
    // U+1F600 and U+2F600 share their low surrogate.
    val e = edit("a\uD83D\uDE00b", "a\uD87D\uDE00b")
    assertEquals(1u, e.startByte)
    assertEquals(5u, e.oldEndByte)
    assertEquals(5u, e.newEndByte)
  }
}
