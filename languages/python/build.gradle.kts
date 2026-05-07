plugins {
  id("compose-highlight-language")
}

composeHighlightLanguage {
  languageName.set("python")
  grammarSubmodulePath.set("tree-sitter-python")
  parserClassName.set("TreeSitterPython")
  sources.set(listOf("src/parser.c", "src/scanner.c"))
  queries.set(listOf("queries/highlights.scm"))
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-python (MIT)")
}
