cmake_minimum_required(VERSION 3.12.0)

project(ktreesitter-swift LANGUAGES C)

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

set(GRAMMAR_DIR ${CMAKE_CURRENT_SOURCE_DIR}/tree-sitter-swift)
set(GENERATED_DIR ${CMAKE_CURRENT_SOURCE_DIR}/build/generated)

# JNI headers come from the NDK sysroot on Android (no find_package needed).
# binding.c includes <tree-sitter-swift.h>; supply a header dir containing it.
set(SWIFT_HEADER_DIR ${CMAKE_CURRENT_BINARY_DIR}/include)
file(MAKE_DIRECTORY ${SWIFT_HEADER_DIR})
file(WRITE ${SWIFT_HEADER_DIR}/tree-sitter-swift.h
"#ifndef TREE_SITTER_SWIFT_H_\n"
"#define TREE_SITTER_SWIFT_H_\n"
"#include <tree_sitter/parser.h>\n"
"#ifdef __cplusplus\n"
"extern \"C\" {\n"
"#endif\n"
"extern const TSLanguage *tree_sitter_swift(void);\n"
"#ifdef __cplusplus\n"
"}\n"
"#endif\n"
"#endif\n"
)

include_directories(
    ${SWIFT_HEADER_DIR}
    ${GRAMMAR_DIR}/src
)

add_library(ktreesitter-swift SHARED
    ${GENERATED_DIR}/src/jni/binding.c
    ${GRAMMAR_DIR}/src/parser.c
    ${GRAMMAR_DIR}/src/scanner.c
)

set_target_properties(ktreesitter-swift PROPERTIES DEFINE_SYMBOL "")
