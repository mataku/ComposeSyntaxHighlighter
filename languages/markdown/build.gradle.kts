plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("markdown")
  grammars {
    create("markdown") {
      submodulePath.set("tree-sitter-markdown/tree-sitter-markdown")
      parserClassName.set("TreeSitterMarkdown")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
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
  licenseCopyright.set("Copyright (c) 2021 Matthias Deiml")
}
