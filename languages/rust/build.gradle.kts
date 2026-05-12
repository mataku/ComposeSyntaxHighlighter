plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("rust")
  grammars {
    create("rust") {
      submodulePath.set("tree-sitter-rust")
      parserClassName.set("TreeSitterRust")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-rust (MIT)")
}
