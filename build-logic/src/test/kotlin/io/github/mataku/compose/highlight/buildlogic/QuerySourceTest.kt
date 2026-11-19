package io.github.mataku.compose.highlight.buildlogic

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QuerySourceTest {

  @Test
  fun joinsAsciiQuerySourcesWithNewline() {
    val first = tempQuery("(identifier) @variable\n")
    val second = tempQuery("[\n  \"if\"\n] @keyword\n")

    assertEquals(
      "(identifier) @variable\n\n[\n  \"if\"\n] @keyword\n",
      readAsciiQuerySources(listOf(first, second)),
    )
  }

  @Test
  fun acceptsTabsAndCarriageReturns() {
    val query = tempQuery("(a\t(b)) @x\r\n")

    assertEquals("(a\t(b)) @x\r\n", readAsciiQuerySources(listOf(query)))
  }

  @Test
  fun rejectsNonAsciiCharacterWithItsLocation() {
    val ascii = tempQuery("(identifier) @variable\n")
    val nonAscii = tempQuery("; first line\n; MIT — see LICENSE\n(comment) @comment\n")

    val error = assertFailsWith<IllegalStateException> { readAsciiQuerySources(listOf(ascii, nonAscii)) }

    val message = error.message!!
    assertTrue(message.contains("${nonAscii.absolutePath}:2:7"), message)
    assertTrue(message.contains("U+2014"), message)
  }

  @Test
  fun reportsEveryOffendingCharacter() {
    val query = tempQuery("; 日\n; —\n")

    val error = assertFailsWith<IllegalStateException> { readAsciiQuerySources(listOf(query)) }

    val message = error.message!!
    assertTrue(message.contains("${query.absolutePath}:1:3"), message)
    assertTrue(message.contains("${query.absolutePath}:2:3"), message)
  }

  private fun tempQuery(contents: String): File = File.createTempFile("highlights", ".scm").apply {
    writeText(contents)
    deleteOnExit()
  }
}
