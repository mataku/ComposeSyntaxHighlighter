package io.github.mataku.compose.highlight.kotlin

import io.github.mataku.compose.highlight.kotlin.internal.TreeSitterKotlin
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class JvmNativeResourceTest {

  @Test
  fun javaLibraryPathDoesNotPointAtHostCMake() {
    assertFalse(System.getProperty("java.library.path").orEmpty().contains("host-cmake"))
  }

  @Test
  fun grammarLibraryIsBundledAsClasspathResource() {
    assertNotNull(TreeSitterKotlin.libPath())
  }
}
