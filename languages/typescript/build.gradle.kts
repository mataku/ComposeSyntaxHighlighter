plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("typescript")
  grammars {
    create("typescript") {
      submodulePath.set("tree-sitter-typescript/typescript")
      parserClassName.set("TreeSitterTypescript")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(
        listOf(
          "../../../javascript/queries/highlights.scm",
          "../queries/highlights.scm",
        ),
      )
    }
    create("tsx") {
      submodulePath.set("tree-sitter-typescript/tsx")
      parserClassName.set("TreeSitterTsx")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(
        listOf(
          "../../../javascript/queries/highlights.scm",
          "../queries/highlights.scm",
        ),
      )
      cSymbol.set("tree_sitter_tsx")
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-typescript (MIT); upstream queries also draw from tree-sitter/tree-sitter-javascript (MIT)")
  licenseCopyright.set("Copyright (c) 2017 Max Brunsfeld (tree-sitter-typescript); Copyright (c) 2014 Max Brunsfeld (tree-sitter-javascript)")
}
