package io.github.mataku.compose.highlight.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ParserInputTest {

  @Test
  fun ascii_input_is_returned_as_is() {
    val code = "val a = 1\n"
    assertSame(code, toParserInput(code))
  }

  @Test
  fun non_ascii_characters_expand_to_their_utf8_byte_length() {
    assertEquals("\"xxxxxxxxx\"", toParserInput("\"é😀ア\""))
  }

  @Test
  fun unicode_whitespace_and_nul_become_placeholders() {
    assertEquals("axxxbxc", toParserInput("a　b\u0000c"))
  }

  @Test
  fun only_leading_bom_becomes_spaces() {
    assertEquals("   axxx", toParserInput("﻿a﻿"))
  }

  @Test
  fun lone_surrogate_expands_like_utf8_byte_index_counts_it() {
    val code = "a\uD800b"
    assertEquals(Utf8ByteIndex(code).byteSize, toParserInput(code).length)
  }

  @Test
  fun result_length_equals_utf8_byte_size() {
    val code = "fun 関数() = \"😀 \" // コメント\n"
    assertEquals(Utf8ByteIndex(code).byteSize, toParserInput(code).length)
  }
}
