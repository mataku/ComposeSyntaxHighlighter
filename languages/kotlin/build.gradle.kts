plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("kotlin")
  grammars {
    create("kotlin") {
      submodulePath.set("tree-sitter-kotlin")
      parserClassName.set("TreeSitterKotlin")
      sources.set(listOf("src/scanner.c", "src/parser.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("fwcd/tree-sitter-kotlin (MIT)")
  licenseCopyright.set("Copyright (c) 2019 fwcd")
}
