plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("python")
  grammars {
    create("python") {
      submodulePath.set("tree-sitter-python")
      parserClassName.set("TreeSitterPython")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-python (MIT)")
  licenseCopyright.set("Copyright (c) 2016 Max Brunsfeld")
}
