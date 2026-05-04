plugins {
    id("compose-syntax-language")
}

composeSyntaxLanguage {
    languageName.set("swift")
    grammarSubmodulePath.set("tree-sitter-swift")
    parserClassName.set("TreeSitterSwift")
    sources.set(listOf("src/parser.c", "src/scanner.c"))
    queries.set(listOf("queries/highlights.scm"))
    licenseSpdx.set("MIT")
    licenseSource.set("alex-pinkus/tree-sitter-swift (MIT)")
}
