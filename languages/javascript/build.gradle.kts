plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("javascript")
  grammars {
    create("javascript") {
      submodulePath.set("tree-sitter-javascript")
      parserClassName.set("TreeSitterJavascript")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-javascript (MIT)")
}
