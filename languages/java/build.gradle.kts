plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("java")
  grammarSubmodulePath.set("tree-sitter-java")
  parserClassName.set("TreeSitterJava")
  sources.set(listOf("src/parser.c"))
  queries.set(listOf("queries/highlights.scm"))
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-java (MIT)")
}
