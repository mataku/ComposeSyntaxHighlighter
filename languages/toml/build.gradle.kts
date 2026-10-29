plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("toml")
  grammars {
    create("toml") {
      submodulePath.set("tree-sitter-toml")
      parserClassName.set("TreeSitterToml")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter-grammars/tree-sitter-toml (MIT)")
  licenseCopyright.set("Copyright (c) Ika <ikatyang@gmail.com> (https://github.com/ikatyang)")
}
