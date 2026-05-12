plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("go")
  grammars {
    create("go") {
      submodulePath.set("tree-sitter-go")
      parserClassName.set("TreeSitterGo")
      sources.set(listOf("src/parser.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-go (MIT)")
}
