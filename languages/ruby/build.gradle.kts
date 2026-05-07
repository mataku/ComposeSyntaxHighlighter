plugins {
  id("compose-highlight-language")
}

composeHighlightLanguage {
  languageName.set("ruby")
  grammarSubmodulePath.set("tree-sitter-ruby")
  parserClassName.set("TreeSitterRuby")
  sources.set(listOf("src/parser.c", "src/scanner.c"))
  queries.set(listOf("queries/highlights.scm"))
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-ruby (MIT)")
}
