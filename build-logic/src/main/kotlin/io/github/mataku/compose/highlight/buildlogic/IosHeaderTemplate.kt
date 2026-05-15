package io.github.mataku.compose.highlight.buildlogic

import java.io.File

private fun headerSymbol(languageName: String): String = "TREE_SITTER_${languageName.uppercase()}_H_"

private fun headerFileName(languageName: String): String = "tree-sitter-$languageName.h"

internal fun renderIosHeader(
  languageName: String,
  grammarCSymbols: List<String>,
): String {
  require(grammarCSymbols.isNotEmpty()) { "grammarCSymbols must not be empty" }
  val symbol = headerSymbol(languageName)
  return buildString {
    append("#ifndef ").append(symbol).append("\n")
    append("#define ").append(symbol).append("\n\n")
    append("typedef struct TSLanguage TSLanguage;\n\n")
    append("#ifdef __cplusplus\n")
    append("extern \"C\" {\n")
    append("#endif\n\n")
    for (cSymbol in grammarCSymbols) {
      append("const TSLanguage *").append(cSymbol).append("(void);\n")
    }
    append("\n#ifdef __cplusplus\n")
    append("}\n")
    append("#endif\n\n")
    append("#endif\n")
  }
}

fun writeIosHeader(
  dir: File,
  languageName: String,
  grammarCSymbols: List<String>,
) {
  val desired = renderIosHeader(languageName, grammarCSymbols)
  val file = File(dir, headerFileName(languageName))
  val existing = if (file.exists()) file.readText() else null
  if (existing != desired) {
    file.parentFile?.mkdirs()
    file.writeText(desired)
  }
}
