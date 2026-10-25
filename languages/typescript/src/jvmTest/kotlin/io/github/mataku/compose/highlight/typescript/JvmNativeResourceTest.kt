package io.github.mataku.compose.highlight.typescript

import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.tsx.Tsx
import io.github.mataku.compose.highlight.tsx.internal.TreeSitterTsx
import io.github.mataku.compose.highlight.typescript.internal.TreeSitterTypescript
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class JvmNativeResourceTest {

  @Test
  fun javaLibraryPathDoesNotPointAtHostCMake() {
    assertFalse(System.getProperty("java.library.path").orEmpty().contains("host-cmake"))
  }

  @Test
  fun typescriptGrammarLibraryIsBundledAsClasspathResource() {
    assertNotNull(TreeSitterTypescript.libPath())
  }

  @Test
  fun tsxGrammarLibraryIsBundledAsClasspathResource() {
    assertNotNull(TreeSitterTsx.libPath())
  }

  @Test
  fun bothGrammarsHighlightInOneJvm() {
    val ts = highlight("const x: number = 1", Languages.Typescript, SyntaxTheme.DarkDefault)
    val tsx = highlight("const el = <div/>", Languages.Tsx, SyntaxTheme.DarkDefault)
    assertEquals("const x: number = 1", ts.text)
    assertEquals("const el = <div/>", tsx.text)
  }
}
