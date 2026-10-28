plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("html")
  grammars {
    create("html") {
      submodulePath.set("tree-sitter-html")
      parserClassName.set("TreeSitterHtml")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-html (MIT)")
  licenseCopyright.set("Copyright (c) 2014 Max Brunsfeld")
}
