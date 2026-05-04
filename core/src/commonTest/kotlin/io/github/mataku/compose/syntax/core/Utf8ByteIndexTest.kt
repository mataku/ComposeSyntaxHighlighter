package io.github.mataku.compose.syntax.core

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
}
