plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("java")
  grammars {
    create("java") {
      submodulePath.set("tree-sitter-java")
      parserClassName.set("TreeSitterJava")
      sources.set(listOf("src/parser.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-java (MIT)")
  licenseCopyright.set("Copyright (c) 2017 Ayman Nadeem")
}
