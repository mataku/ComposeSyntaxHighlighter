import org.gradle.api.tasks.testing.Test

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
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter-grammars/tree-sitter-markdown (MIT)")
}

dependencies {
  commonMainImplementation(projects.languages.markdownInline)
}

tasks.named<Test>("jvmTest") {
  val inlineHostDir = project(":languages:markdown-inline").layout.buildDirectory
    .dir("host-cmake")
    .map { it.asFile.absolutePath }
  dependsOn(":languages:markdown-inline:buildHostCMake")
  doFirst {
    val existing = systemProperties["java.library.path"]?.toString().orEmpty()
    val sep = File.pathSeparator
    val combined = if (existing.isBlank()) {
      inlineHostDir.get()
    } else {
      "$existing$sep${inlineHostDir.get()}"
    }
    systemProperty("java.library.path", combined)
  }
}
