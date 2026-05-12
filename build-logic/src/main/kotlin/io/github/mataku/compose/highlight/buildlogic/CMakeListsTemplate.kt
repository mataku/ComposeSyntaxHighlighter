package io.github.mataku.compose.highlight.buildlogic

import java.io.File

private fun headerSymbol(languageName: String): String = "TREE_SITTER_${languageName.uppercase()}_H_"

private fun headerFileName(languageName: String): String = "tree-sitter-$languageName.h"

private fun headerDirVar(languageName: String): String = "${languageName.uppercase()}_HEADER_DIR"

private fun targetName(languageName: String): String = "ktreesitter-$languageName"

private fun renderSourcesBlock(sources: List<String>, prefix: String): String = sources.joinToString(separator = "\n") { "    $prefix/$it" }

internal fun renderAndroidCMakeLists(
  languageName: String,
  grammarSubmodulePath: String,
  sources: List<String>,
  cSymbol: String? = null,
): String {
  val resolvedSymbol = cSymbol ?: "tree_sitter_$languageName"
  val symbol = headerSymbol(languageName)
  val headerName = headerFileName(languageName)
  val headerVar = headerDirVar(languageName)
  val target = targetName(languageName)
  val sourcesBlock = renderSourcesBlock(sources, "\${GRAMMAR_DIR}")
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

set(GRAMMAR_DIR ${'$'}{CMAKE_CURRENT_SOURCE_DIR}/$grammarSubmodulePath)
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
"extern const TSLanguage *$resolvedSymbol(void);\n"
"#ifdef __cplusplus\n"
"}\n"
"#endif\n"
"#endif\n"
)

include_directories(
    ${'$'}{$headerVar}
    ${'$'}{GRAMMAR_DIR}/src
)

add_library($target SHARED
    ${'$'}{GENERATED_DIR}/src/jni/binding.c
$sourcesBlock
)

set_target_properties($target PROPERTIES DEFINE_SYMBOL "")
"""
}

internal fun renderHostCMakeLists(
  languageName: String,
  grammarSubmodulePath: String,
  sources: List<String>,
  cSymbol: String? = null,
): String {
  val resolvedSymbol = cSymbol ?: "tree_sitter_$languageName"
  val symbol = headerSymbol(languageName)
  val headerName = headerFileName(languageName)
  val headerVar = headerDirVar(languageName)
  val target = targetName(languageName)
  val sourcesBlock = renderSourcesBlock(sources, "\${GRAMMAR_DIR}")
  val headerOneLine = buildString {
    append("\"#ifndef ").append(symbol).append("\\n")
    append("#define ").append(symbol).append("\\n")
    append("#include <tree_sitter/parser.h>\\n")
    append("#ifdef __cplusplus\\n")
    append("extern \\\"C\\\" {\\n")
    append("#endif\\n")
    append("extern const TSLanguage *").append(resolvedSymbol).append("(void);\\n")
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
set(GRAMMAR_DIR ${'$'}{REPO_ROOT}/$grammarSubmodulePath)
set(GENERATED_DIR ${'$'}{REPO_ROOT}/build/generated)

set($headerVar ${'$'}{CMAKE_CURRENT_BINARY_DIR}/include)
file(MAKE_DIRECTORY ${'$'}{$headerVar})
file(WRITE ${'$'}{$headerVar}/$headerName
$headerOneLine)

find_package(JNI REQUIRED)

include_directories(
    ${'$'}{$headerVar}
    ${'$'}{GRAMMAR_DIR}/src
    ${'$'}{JNI_INCLUDE_DIRS}
)

add_library($target SHARED
    ${'$'}{GENERATED_DIR}/src/jni/binding.c
$sourcesBlock
)
"""
}

fun writeAndroidCMakeLists(
  file: File,
  languageName: String,
  grammarSubmodulePath: String,
  sources: List<String>,
  cSymbol: String? = null,
) {
  val desired = renderAndroidCMakeLists(languageName, grammarSubmodulePath, sources, cSymbol)
  writeIfChanged(file, desired)
}

fun writeHostCMakeLists(
  file: File,
  languageName: String,
  grammarSubmodulePath: String,
  sources: List<String>,
  cSymbol: String? = null,
) {
  val desired = renderHostCMakeLists(languageName, grammarSubmodulePath, sources, cSymbol)
  writeIfChanged(file, desired)
}

private fun writeIfChanged(file: File, desired: String) {
  val existing = if (file.exists()) file.readText() else null
  if (existing != desired) {
    file.parentFile?.mkdirs()
    file.writeText(desired)
  }
}
