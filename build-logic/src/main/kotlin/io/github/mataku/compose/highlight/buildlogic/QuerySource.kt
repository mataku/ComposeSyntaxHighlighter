package io.github.mataku.compose.highlight.buildlogic

import java.io.File

// Query sources must stay ASCII-only: ktreesitter's Kotlin/Native Query passes the char count to ts_query_new as the byte length, so each non-ASCII character cuts bytes off the end of the query and the last pattern silently stops matching on iOS.
// A runtime test cannot stand in for this check because the truncation only shows when a test happens to exercise the last pattern.
fun readAsciiQuerySources(sources: List<File>): String {
  val texts = sources.map { it.readText() }
  val violations = sources.zip(texts).flatMap { (file, text) -> nonAsciiLocations(file, text) }
  if (violations.isNotEmpty()) {
    error(
      "Query sources must be ASCII-only because ktreesitter truncates non-ASCII query sources on Kotlin/Native. " +
        "Replace these characters:\n" + violations.joinToString("\n") { "  $it" },
    )
  }
  return texts.joinToString(separator = "\n")
}

private fun nonAsciiLocations(file: File, text: String): List<String> = text.split('\n').flatMapIndexed { lineIndex, line ->
  line.mapIndexedNotNull { columnIndex, char ->
    if (char.code < 0x80) {
      null
    } else {
      "${file.absoluteFile.normalize().path}:${lineIndex + 1}:${columnIndex + 1} U+${char.code.toString(16).uppercase().padStart(4, '0')}"
    }
  }
}
