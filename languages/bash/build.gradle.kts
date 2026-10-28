plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("bash")
  grammars {
    create("bash") {
      submodulePath.set("tree-sitter-bash")
      parserClassName.set("TreeSitterBash")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-bash (MIT)")
  licenseCopyright.set("Copyright (c) 2017 Max Brunsfeld")
}
