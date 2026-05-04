plugins {
    id("compose-syntax-language")
}

composeSyntaxLanguage {
    languageName.set("kotlin")
    grammarSubmodulePath.set("tree-sitter-kotlin")
    parserClassName.set("TreeSitterKotlin")
    sources.set(listOf("src/scanner.c", "src/parser.c"))
    queries.set(listOf("queries/highlights.scm"))
    licenseSpdx.set("MIT")
    licenseSource.set("fwcd/tree-sitter-kotlin (MIT)")
}
