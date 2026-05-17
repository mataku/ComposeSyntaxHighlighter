plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("ruby")
  grammars {
    create("ruby") {
      submodulePath.set("tree-sitter-ruby")
      parserClassName.set("TreeSitterRuby")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-ruby (MIT)")
  licenseCopyright.set("Copyright (c) 2016 Rob Rix")
}
