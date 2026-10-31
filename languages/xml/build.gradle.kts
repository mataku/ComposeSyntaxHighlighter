plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("xml")
  grammars {
    create("xml") {
      submodulePath.set("tree-sitter-xml/xml")
      parserClassName.set("TreeSitterXml")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("../queries/xml/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter-grammars/tree-sitter-xml (MIT)")
  licenseCopyright.set("Copyright (c) 2023 ObserverOfTime")
}
