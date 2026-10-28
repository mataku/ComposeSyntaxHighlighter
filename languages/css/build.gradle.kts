plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("css")
  grammars {
    create("css") {
      submodulePath.set("tree-sitter-css")
      parserClassName.set("TreeSitterCss")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-css (MIT)")
  licenseCopyright.set("Copyright (c) 2018 Max Brunsfeld")
}
