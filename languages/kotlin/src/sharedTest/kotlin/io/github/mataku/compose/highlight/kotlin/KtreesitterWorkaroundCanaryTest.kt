package io.github.mataku.compose.highlight.kotlin

import io.github.mataku.compose.highlight.api.Languages
import io.github.treesitter.ktreesitter.Parser
import kotlin.test.Test
import kotlin.test.assertNotEquals

/**
 * Asserts that the ktreesitter bugs worked around by core's `ParserInput.kt` still exist.
 * A failure here after bumping ktreesitter means upstream fixed them: delete `parseForHighlight`
 * / `toParserInput` (call `Parser.parse` directly again) and delete this test.
 */
class KtreesitterWorkaroundCanaryTest {

  @Test
  fun raw_parse_still_miscounts_non_ascii_byte_length() {
    val code = "val s = \"😀日\""
    val tree = Parser(Languages.Kotlin.parser).parse(code)
    assertNotEquals(
      code.encodeToByteArray().size,
      tree.rootNode.endByte.toInt(),
      "ktreesitter now reports UTF-8 byte offsets for non-ASCII input " +
        "(tree-sitter/kotlin-tree-sitter#84 / #78). Remove the ParserInput.kt workaround.",
    )
  }

  @Test
  fun raw_node_text_still_slices_by_byte_offset_as_char_index() {
    // Trailing newlines keep `it` inside the parsed range on Kotlin/Native, where the parse is truncated.
    val code = "val s = \"日\"\nit\n\n\n"
    val tree = Parser(Languages.Kotlin.parser).parse(code)
    val start = code.substring(0, code.lastIndexOf("it")).encodeToByteArray().size
    val node = checkNotNull(tree.rootNode.descendant(start.toUInt(), (start + 2).toUInt()))
    assertNotEquals(
      "it",
      node.text()?.toString(),
      "ktreesitter Node.text() now slices by UTF-8 bytes (tree-sitter/kotlin-tree-sitter#78). " +
        "Remove the ParserInput.kt workaround.",
    )
  }
}
