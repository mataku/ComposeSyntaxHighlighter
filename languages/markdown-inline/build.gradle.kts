plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("markdownInline")
  grammars {
    create("markdownInline") {
      submodulePath.set("tree-sitter-markdown/tree-sitter-markdown-inline")
      parserClassName.set("TreeSitterMarkdownInline")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
      cSymbol.set("tree_sitter_markdown_inline")
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter-grammars/tree-sitter-markdown (MIT)")
}
