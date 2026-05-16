package io.github.mataku.compose.highlight.buildlogic

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ParserAbiTest {

  @Test
  fun extractsLanguageVersion() {
    val parserC = tempParserC(
      """
      #include "tree_sitter/parser.h"

      #define LANGUAGE_VERSION 14
      #define STATE_COUNT 100
      """.trimIndent(),
    )

    assertEquals(14, extractParserAbi(parserC))
  }

  @Test
  fun extractsLanguageVersionIgnoringWhitespaceVariants() {
    val parserC = tempParserC("#define   LANGUAGE_VERSION\t15\n")
    assertEquals(15, extractParserAbi(parserC))
  }

  @Test
  fun throwsWhenLanguageVersionMissing() {
    val parserC = tempParserC("#define STATE_COUNT 100\n")
    val error = assertFailsWith<IllegalStateException> { extractParserAbi(parserC) }
    assert(error.message!!.contains("LANGUAGE_VERSION"))
  }

  private fun tempParserC(contents: String): File = File.createTempFile("parser", ".c").apply {
    writeText(contents)
    deleteOnExit()
  }
}
