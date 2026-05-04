cmake_minimum_required(VERSION 3.12.0)

project(ktreesitter-kotlin-host LANGUAGES C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
add_compile_options(-Wall -Wextra -Wno-unused-parameter -Werror=implicit-function-declaration)

set(REPO_ROOT ${CMAKE_CURRENT_SOURCE_DIR}/..)
set(GRAMMAR_DIR ${REPO_ROOT}/tree-sitter-kotlin)
set(GENERATED_DIR ${REPO_ROOT}/build/generated)

set(KOTLIN_HEADER_DIR ${CMAKE_CURRENT_BINARY_DIR}/include)
file(MAKE_DIRECTORY ${KOTLIN_HEADER_DIR})
file(WRITE ${KOTLIN_HEADER_DIR}/tree-sitter-kotlin.h
"#ifndef TREE_SITTER_KOTLIN_H_\n#define TREE_SITTER_KOTLIN_H_\n#include <tree_sitter/parser.h>\n#ifdef __cplusplus\nextern \"C\" {\n#endif\nextern const TSLanguage *tree_sitter_kotlin(void);\n#ifdef __cplusplus\n}\n#endif\n#endif\n")

find_package(JNI REQUIRED)

include_directories(
    ${KOTLIN_HEADER_DIR}
    ${GRAMMAR_DIR}/src
    ${JNI_INCLUDE_DIRS}
)

add_library(ktreesitter-kotlin SHARED
    ${GENERATED_DIR}/src/jni/binding.c
    ${GRAMMAR_DIR}/src/scanner.c
    ${GRAMMAR_DIR}/src/parser.c
)
