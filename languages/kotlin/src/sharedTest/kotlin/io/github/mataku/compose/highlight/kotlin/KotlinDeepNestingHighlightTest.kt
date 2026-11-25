package io.github.mataku.compose.highlight.kotlin

import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.highlightLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinDeepNestingHighlightTest {

  @Test
  fun every_call_in_deeply_nested_calls_is_highlighted() {
    // Each enclosing call keeps matches open until its closing parenthesis, so the cursor holds more matches than its limit here while staying above the start-depth bound.
    val depth = 200
    val code = "val v = " + "f(".repeat(depth) + "1" + ")".repeat(depth)
    val spans = highlightLines(code, Languages.Kotlin).single().spans
    val callStarts = spans.filter { it.captureName == "function" }.map { it.start }.toSet()
    assertEquals(List(depth) { 8 + 2 * it }.toSet(), callStarts)
  }

  @Test
  fun chain_deeper_than_the_start_depth_bound_highlights_only_its_shallow_end() {
    // A left-associative chain nests at least one level per operator, so its first operand sits far below the start-depth bound.
    val code = "val v = 1" + " + 1".repeat(5_000)
    val spans = highlightLines(code, Languages.Kotlin).single().spans
    val numberStarts = spans.filter { it.captureName == "number" }.map { it.start }.toSet()
    assertTrue(code.lastIndex in numberStarts, "the shallowest operand is highlighted")
    assertTrue(8 !in numberStarts, "the deepest operand is past the bound and left unhighlighted")
  }

  @Test
  fun deeply_nested_string_templates_highlight_without_failing() {
    // The scanner saves two bytes per open string, so 600 nested templates exceed the parser's 1,024-byte state buffer.
    val code = "val v = " + "\"\${".repeat(600) + "1" + "}\"".repeat(600)
    assertEquals(code, highlightLines(code, Languages.Kotlin).single().text)
  }
}
