package io.github.mataku.compose.highlight.buildlogic

/**
 * Renders a JNI binding C file for a secondary grammar in a multi-grammar module.
 * Mirrors the output of `io.github.tree-sitter.ktreesitter-plugin`'s primary `binding.c`,
 * with the path/symbols adjusted for the secondary grammar's package and C symbol.
 *
 * - [packageName] e.g. `io.github.mataku.compose.highlight.markdownInline.internal` —
 *   used to compose the JNI function name `Java_<pkg>_<class>_<method>`.
 * - [className] e.g. `TreeSitterMarkdownInline` — the Kotlin object name on the JVM side.
 * - [cSymbol] e.g. `tree_sitter_markdown_inline` — the C function exported by parser.c.
 *   Also used as the Kotlin external function name (so JNI lookup matches).
 * - [headerName] e.g. `tree-sitter-markdown.h` — single shared header in this module's
 *   include dir declaring all grammars' externs.
 */
internal fun renderSecondaryBindingC(
  packageName: String,
  className: String,
  cSymbol: String,
  headerName: String,
): String {
  val jniPackage = packageName.replace('.', '_')
  val jniSymbol = cSymbol.replace("_", "_1")
  return """// Automatically generated file. DO NOT MODIFY

#include <jni.h>
#include <$headerName>

#ifndef __ANDROID__
#define NATIVE_FUNCTION(name) JNIEXPORT jlong JNICALL name(JNIEnv * _env, jclass _class)
#else
#define NATIVE_FUNCTION(name) JNIEXPORT jlong JNICALL name()
#endif

NATIVE_FUNCTION(Java_${jniPackage}_${className}_$jniSymbol) {
    return (jlong)$cSymbol();
}
"""
}
