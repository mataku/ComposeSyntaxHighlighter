plugins {
  id("compose-highlight-language")
}

composeHighlightLanguage {
  languageName.set("rust")
  grammarSubmodulePath.set("tree-sitter-rust")
  parserClassName.set("TreeSitterRust")
  sources.set(listOf("src/parser.c", "src/scanner.c"))
  queries.set(listOf("queries/highlights.scm"))
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-rust (MIT)")
}
