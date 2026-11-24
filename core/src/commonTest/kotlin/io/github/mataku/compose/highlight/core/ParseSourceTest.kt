package io.github.mataku.compose.highlight.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ParseSourceTest {

  @Test
  fun text_without_lone_surrogates_is_returned_as_is() {
    val text = "a😀b"
    assertSame(text, text.replaceLoneSurrogates())
  }

  @Test
  fun lone_high_and_low_surrogates_are_replaced_in_place() {
    assertEquals("a\uFFFDb\uFFFDc", "a\uD800b\uDC00c".replaceLoneSurrogates())
  }

  @Test
  fun high_surrogate_at_the_end_is_replaced() {
    assertEquals("a\uFFFD", "a\uD83D".replaceLoneSurrogates())
  }

  @Test
  fun reversed_pair_is_two_lone_surrogates() {
    assertEquals("\uFFFD\uFFFD", "\uDE00\uD83D".replaceLoneSurrogates())
  }

  @Test
  fun pair_next_to_lone_surrogate_is_kept() {
    assertEquals("\uFFFD😀", "\uD83D😀".replaceLoneSurrogates())
  }
}
