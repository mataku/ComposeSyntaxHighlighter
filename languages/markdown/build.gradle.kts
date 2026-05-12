plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("markdown")
  grammarSubmodulePath.set("tree-sitter-markdown/tree-sitter-markdown")
  parserClassName.set("TreeSitterMarkdown")
  sources.set(listOf("src/parser.c", "src/scanner.c"))
  queries.set(listOf("queries/highlights.scm"))
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter-grammars/tree-sitter-markdown (MIT)")
}

dependencies {
  commonMainImplementation(projects.languages.markdownInline)
}
