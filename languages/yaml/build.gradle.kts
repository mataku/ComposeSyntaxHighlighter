plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("yaml")
  grammars {
    create("yaml") {
      submodulePath.set("tree-sitter-yaml")
      parserClassName.set("TreeSitterYaml")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter-grammars/tree-sitter-yaml (MIT)")
  licenseCopyright.set("Copyright (c) 2024 tree-sitter-grammars contributors; Copyright (c) 2019-2021 Ika")
}
