package io.github.mataku.compose.highlight.buildlogic

import java.io.File

/**
 * Per-grammar build inputs consumed by [writeAndroidCMakeLists] / [writeHostCMakeLists].
 * One entry per grammar in a module.
 */
data class GrammarBuildSpec(
  val name: String,
  val submodulePath: String,
  val sources: List<String>,
  val cSymbol: String,
  /**
   * Path to the JNI binding C file for this grammar, relative to the module directory.
   * For the primary grammar this is the ktreesitter-plugin output
   * (`build/generated/src/jni/binding.c`). For secondary grammars this is our generated
   * `build/generated/src/jni/binding-<name>.c`.
   */
  val bindingCPath: String,
)

private fun headerSymbol(languageName: String): String = "TREE_SITTER_${languageName.uppercase()}_H_"

private fun headerFileName(languageName: String): String = "tree-sitter-$languageName.h"

private fun headerDirVar(languageName: String): String = "${languageName.uppercase()}_HEADER_DIR"

private fun targetName(languageName: String): String = "ktreesitter-$languageName"

private fun grammarDirVar(grammarName: String): String = "GRAMMAR_DIR_${grammarName.uppercase()}"

private fun renderAndroidSourcesBlock(grammars: List<GrammarBuildSpec>): String {
  val lines = mutableListOf<String>()
  for (g in grammars) {
    lines += "    \${CMAKE_CURRENT_SOURCE_DIR}/${g.bindingCPath}"
    for (src in g.sources) {
      lines += "    \${${grammarDirVar(g.name)}}/$src"
    }
  }
  return lines.joinToString("\n")
}

private fun renderHostSourcesBlock(grammars: List<GrammarBuildSpec>): String {
  val lines = mutableListOf<String>()
  for (g in grammars) {
    lines += "    \${REPO_ROOT}/${g.bindingCPath}"
    for (src in g.sources) {
      lines += "    \${${grammarDirVar(g.name)}}/$src"
    }
  }
  return lines.joinToString("\n")
}

private fun renderHeaderExterns(grammars: List<GrammarBuildSpec>): String =
  grammars.joinToString(separator = "\n") { g ->
    "\"extern const TSLanguage *${g.cSymbol}(void);\\n\""
  }

private fun renderAndroidGrammarDirAssignments(grammars: List<GrammarBuildSpec>): String =
  grammars.joinToString(separator = "\n") { g ->
    "set(${grammarDirVar(g.name)} \${CMAKE_CURRENT_SOURCE_DIR}/${g.submodulePath})"
  }

private fun renderHostGrammarDirAssignments(grammars: List<GrammarBuildSpec>): String =
  grammars.joinToString(separator = "\n") { g ->
    "set(${grammarDirVar(g.name)} \${REPO_ROOT}/${g.submodulePath})"
  }

internal fun renderAndroidCMakeLists(
  languageName: String,
  grammars: List<GrammarBuildSpec>,
): String {
  require(grammars.isNotEmpty()) { "grammars must contain at least one entry" }
  val symbol = headerSymbol(languageName)
  val headerName = headerFileName(languageName)
  val headerVar = headerDirVar(languageName)
  val target = targetName(languageName)
  val grammarDirAssignments = renderAndroidGrammarDirAssignments(grammars)
  val sourcesBlock = renderAndroidSourcesBlock(grammars)
  val externsBlock = renderHeaderExterns(grammars)
  return """cmake_minimum_required(VERSION 3.12.0)

project($target LANGUAGES C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_VISIBILITY_PRESET hidden)

if(MSVC)
    add_compile_options(/W3 /wd4244)
else()
    add_compile_options(-Wall -Wextra
                        -Wno-unused-parameter
                        -Werror=implicit-function-declaration)
endif()

add_compile_definitions(TREE_SITTER_HIDE_SYMBOLS)

$grammarDirAssignments
set(GENERATED_DIR ${'$'}{CMAKE_CURRENT_SOURCE_DIR}/build/generated)

# JNI headers come from the NDK sysroot on Android (no find_package needed).
# binding.c includes <$headerName>; supply a header dir containing it.
set($headerVar ${'$'}{CMAKE_CURRENT_BINARY_DIR}/include)
file(MAKE_DIRECTORY ${'$'}{$headerVar})
file(WRITE ${'$'}{$headerVar}/$headerName
"#ifndef $symbol\n"
"#define $symbol\n"
"#include <tree_sitter/parser.h>\n"
"#ifdef __cplusplus\n"
"extern \"C\" {\n"
"#endif\n"
$externsBlock
"#ifdef __cplusplus\n"
"}\n"
"#endif\n"
"#endif\n"
)

include_directories(
    ${'$'}{$headerVar}
${grammars.joinToString(separator = "\n") { "    \${${grammarDirVar(it.name)}}/src" }}
)

add_library($target SHARED
$sourcesBlock
)

set_target_properties($target PROPERTIES DEFINE_SYMBOL "")
"""
}

internal fun renderHostCMakeLists(
  languageName: String,
  grammars: List<GrammarBuildSpec>,
): String {
  require(grammars.isNotEmpty()) { "grammars must contain at least one entry" }
  val symbol = headerSymbol(languageName)
  val headerName = headerFileName(languageName)
  val headerVar = headerDirVar(languageName)
  val target = targetName(languageName)
  val grammarDirAssignments = renderHostGrammarDirAssignments(grammars)
  val sourcesBlock = renderHostSourcesBlock(grammars)
  val headerOneLine = buildString {
    append("\"#ifndef ").append(symbol).append("\\n")
    append("#define ").append(symbol).append("\\n")
    append("#include <tree_sitter/parser.h>\\n")
    append("#ifdef __cplusplus\\n")
    append("extern \\\"C\\\" {\\n")
    append("#endif\\n")
    for (g in grammars) {
      append("extern const TSLanguage *").append(g.cSymbol).append("(void);\\n")
    }
    append("#ifdef __cplusplus\\n")
    append("}\\n")
    append("#endif\\n")
    append("#endif\\n\"")
  }
  return """cmake_minimum_required(VERSION 3.12.0)

project($target-host LANGUAGES C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
add_compile_options(-Wall -Wextra -Wno-unused-parameter -Werror=implicit-function-declaration)

set(REPO_ROOT ${'$'}{CMAKE_CURRENT_SOURCE_DIR}/..)
$grammarDirAssignments
set(GENERATED_DIR ${'$'}{REPO_ROOT}/build/generated)

set($headerVar ${'$'}{CMAKE_CURRENT_BINARY_DIR}/include)
file(MAKE_DIRECTORY ${'$'}{$headerVar})
file(WRITE ${'$'}{$headerVar}/$headerName
$headerOneLine)

find_package(JNI REQUIRED)

include_directories(
    ${'$'}{$headerVar}
${grammars.joinToString(separator = "\n") { "    \${${grammarDirVar(it.name)}}/src" }}
    ${'$'}{JNI_INCLUDE_DIRS}
)

add_library($target SHARED
$sourcesBlock
)
"""
}

fun writeAndroidCMakeLists(
  file: File,
  languageName: String,
  grammars: List<GrammarBuildSpec>,
) {
  val desired = renderAndroidCMakeLists(languageName, grammars)
  writeIfChanged(file, desired)
}

fun writeHostCMakeLists(
  file: File,
  languageName: String,
  grammars: List<GrammarBuildSpec>,
) {
  val desired = renderHostCMakeLists(languageName, grammars)
  writeIfChanged(file, desired)
}

private fun writeIfChanged(file: File, desired: String) {
  val existing = if (file.exists()) file.readText() else null
  if (existing != desired) {
    file.parentFile?.mkdirs()
    file.writeText(desired)
  }
}
