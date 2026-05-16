package io.github.mataku.compose.highlight.buildlogic

import java.io.File

/**
 * Writes a CMakeLists.txt for an Android NDK toolchain invocation that produces
 * libtree-sitter-<languageName>.so. The CMakeLists is placed under
 * <module>/android-cmake/<abi>/ and invoked via direct `cmake` calls with
 * -DCMAKE_TOOLCHAIN_FILE pointing at the NDK's android.toolchain.cmake.
 *
 * Mirrors the body of writeHostCMakeLists / writeAndroidCMakeLists from
 * CMakeListsTemplate.kt: the source files are the per-grammar parser.c
 * (+ scanner.c when present) plus the binding.c emitted by ktreesitter-plugin
 * (or, for secondary grammars, build-logic's renderSecondaryBindingC).
 *
 * Source paths in the CMakeLists are written relative to the CMakeLists' own
 * directory (<module>/android-cmake/<abi>/), so each path traverses up three
 * components ("../../../") to reach the module root, then the submodule's
 * tracked source file or the build-relative binding.c output.
 */
fun writeAndroidNdkCmakeLists(
  target: File,
  languageName: String,
  grammars: List<GrammarBuildSpec>,
) {
  target.parentFile.mkdirs()
  target.writeText(buildString {
    appendLine("cmake_minimum_required(VERSION 3.22.1)")
    appendLine("project(tree-sitter-$languageName C)")
    appendLine()
    appendLine("set(CMAKE_C_STANDARD 11)")
    appendLine("set(CMAKE_C_VISIBILITY_PRESET hidden)")
    appendLine("set(CMAKE_POSITION_INDEPENDENT_CODE ON)")
    appendLine()
    val sources = buildList {
      for (spec in grammars) {
        val srcRoot = "../../../${spec.submodulePath}"
        for (rel in spec.sources) {
          add("$srcRoot/$rel")
        }
        add("../../../${spec.bindingCPath}")
      }
    }
    appendLine("add_library(tree-sitter-$languageName SHARED")
    for (src in sources) appendLine("  $src")
    appendLine(")")
    appendLine()
    for (spec in grammars) {
      appendLine("target_include_directories(tree-sitter-$languageName PRIVATE ../../../${spec.submodulePath}/src)")
    }
    appendLine("target_compile_definitions(tree-sitter-$languageName PRIVATE TREE_SITTER_HIDE_SYMBOLS)")
  })
}

/**
 * Resolves the Android NDK root using the standard fallback chain.
 * Returns the directory containing build/cmake/android.toolchain.cmake.
 *
 * 1. $ANDROID_NDK_ROOT if set and points to a valid NDK install.
 * 2. $ANDROID_HOME/ndk/<requestedNdkVersion> otherwise.
 *
 * Throws IllegalStateException with an actionable message if neither resolves.
 */
fun resolveAndroidNdkRoot(requestedNdkVersion: String): File {
  System.getenv("ANDROID_NDK_ROOT")?.let { return File(it) }
  val androidHome = System.getenv("ANDROID_HOME")
    ?: error(
      "Cannot resolve Android NDK: neither ANDROID_NDK_ROOT nor ANDROID_HOME is set. " +
        "Set one of them and re-run the build."
    )
  val candidate = File(androidHome, "ndk/$requestedNdkVersion")
  require(candidate.isDirectory) {
    "Android NDK not found at ${candidate.absolutePath}. Install via Android Studio SDK Manager " +
      "or set ANDROID_NDK_ROOT to a valid NDK install."
  }
  return candidate
}
