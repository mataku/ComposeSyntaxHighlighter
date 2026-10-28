plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("json")
  grammars {
    create("json") {
      submodulePath.set("tree-sitter-json")
      parserClassName.set("TreeSitterJson")
      sources.set(listOf("src/parser.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-json (MIT)")
  licenseCopyright.set("Copyright (c) 2014 Max Brunsfeld")
}
