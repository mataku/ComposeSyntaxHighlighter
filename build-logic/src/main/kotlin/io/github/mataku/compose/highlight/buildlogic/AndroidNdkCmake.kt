package io.github.mataku.compose.highlight.buildlogic

import java.io.File

/**
 * Writes a CMakeLists.txt for an Android NDK toolchain invocation that produces
 * libktreesitter-<languageName>.so. The library name matches the convention plugin's
 * `grammar { libraryName = "ktreesitter-$languageName" }` setting, which in turn matches
 * `System.loadLibrary("ktreesitter-$languageName")` in the ktreesitter-plugin-generated
 * Kotlin bindings. The CMakeLists is placed under <module>/android-cmake/<abi>/ and
 * invoked via direct `cmake` calls with -DCMAKE_TOOLCHAIN_FILE pointing at the NDK's
 * android.toolchain.cmake.
 *
 * Mirrors the body of writeHostCMakeLists / writeAndroidCMakeLists from
 * CMakeListsTemplate.kt: the source files are the per-grammar parser.c
 * (+ scanner.c when present) plus the binding.c emitted by ktreesitter-plugin
 * (or, for secondary grammars, build-logic's renderSecondaryBindingC).
 *
 * Source paths in the CMakeLists are written relative to the CMakeLists' own
 * directory (<module>/android-cmake/<abi>/), so each path traverses up two
 * components ("../../") to reach the module root, then the submodule's
 * tracked source file or the build-relative binding.c output.
 */
fun writeAndroidNdkCmakeLists(
  target: File,
  languageName: String,
  grammars: List<GrammarBuildSpec>,
) {
  require(grammars.isNotEmpty()) { "grammars must contain at least one entry" }
  // The ktreesitter-plugin-generated binding.c for the primary grammar includes
  // <tree-sitter-<primary>.h>, so the alias header must be named after the primary
  // grammar (not languageName). For single-grammar modules these are the same.
  val primaryName = grammars.first().name
  val headerSymbol = "TREE_SITTER_${primaryName.uppercase()}_H_"
  val headerFileName = "tree-sitter-$primaryName.h"
  val cmakeTargetName = "ktreesitter-$languageName"
  target.parentFile.mkdirs()
  target.writeText(buildString {
    appendLine("cmake_minimum_required(VERSION 3.22.1)")
    appendLine("project($cmakeTargetName C)")
    appendLine()
    appendLine("set(CMAKE_C_STANDARD 11)")
    appendLine("set(CMAKE_C_VISIBILITY_PRESET hidden)")
    appendLine("set(CMAKE_POSITION_INDEPENDENT_CODE ON)")
    appendLine()
    // binding.c includes <tree-sitter-<primary>.h>; supply it via a generated alias header.
    appendLine("set(LANGUAGE_HEADER_DIR \${CMAKE_CURRENT_BINARY_DIR}/include)")
    appendLine("file(MAKE_DIRECTORY \${LANGUAGE_HEADER_DIR})")
    appendLine("file(WRITE \${LANGUAGE_HEADER_DIR}/$headerFileName")
    appendLine("\"#ifndef $headerSymbol\\n\"")
    appendLine("\"#define $headerSymbol\\n\"")
    appendLine("\"#include <tree_sitter/parser.h>\\n\"")
    appendLine("\"#ifdef __cplusplus\\n\"")
    appendLine("\"extern \\\"C\\\" {\\n\"")
    appendLine("\"#endif\\n\"")
    for (g in grammars) {
      appendLine("\"extern const TSLanguage *${g.cSymbol}(void);\\n\"")
    }
    appendLine("\"#ifdef __cplusplus\\n\"")
    appendLine("\"}\\n\"")
    appendLine("\"#endif\\n\"")
    appendLine("\"#endif\\n\"")
    appendLine(")")
    appendLine()
    val sources = buildList {
      for (spec in grammars) {
        val srcRoot = "../../${spec.submodulePath}"
        for (rel in spec.sources) {
          add("$srcRoot/$rel")
        }
        add("../../${spec.bindingCPath}")
      }
    }
    appendLine("add_library($cmakeTargetName SHARED")
    for (src in sources) appendLine("  $src")
    appendLine(")")
    appendLine()
    appendLine("target_include_directories($cmakeTargetName PRIVATE \${LANGUAGE_HEADER_DIR})")
    for (spec in grammars) {
      appendLine("target_include_directories($cmakeTargetName PRIVATE ../../${spec.submodulePath}/src)")
    }
    appendLine("target_compile_definitions($cmakeTargetName PRIVATE TREE_SITTER_HIDE_SYMBOLS)")
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
