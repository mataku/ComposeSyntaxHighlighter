package io.github.mataku.compose.highlight.buildlogic

import java.io.File

private val LANGUAGE_VERSION_REGEX = Regex("""#define\s+LANGUAGE_VERSION\s+(\d+)""")

fun extractParserAbi(parserC: File): Int {
  val text = parserC.readText()
  val match = LANGUAGE_VERSION_REGEX.find(text)
    ?: error("LANGUAGE_VERSION not found in ${parserC.absolutePath}")
  return match.groupValues[1].toInt()
}
