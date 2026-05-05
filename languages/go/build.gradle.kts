plugins {
    id("compose-highlight-language")
}

composeHighlightLanguage {
    languageName.set("go")
    grammarSubmodulePath.set("tree-sitter-go")
    parserClassName.set("TreeSitterGo")
    sources.set(listOf("src/parser.c"))
    queries.set(listOf("queries/highlights.scm"))
    licenseSpdx.set("MIT")
    licenseSource.set("tree-sitter/tree-sitter-go (MIT)")
}
