package io.github.mataku.compose.highlight.core

import io.github.treesitter.ktreesitter.Parser
import io.github.treesitter.ktreesitter.Tree

/**
 * Parses [code] through [toParserInput] so that ktreesitter only ever sees ASCII.
 *
 * Workaround for ktreesitter mishandling non-ASCII input
 * (tree-sitter/kotlin-tree-sitter#78, tree-sitter/kotlin-tree-sitter#84):
 * - JVM/Android pass the source to tree-sitter as modified UTF-8, so supplementary characters
 *   and NUL shift every later byte offset.
 * - `Node.text()` slices the source by byte offsets as if they were char indices, which breaks
 *   `#eq?` / `#match?` predicates after any multibyte character.
 * - Kotlin/Native passes the char count as the byte length, truncating the parse.
 *
 * Remove once a ktreesitter release ships both fixes; `KtreesitterWorkaroundCanaryTest`
 * fails when that happens.
 */
internal fun Parser.parseForHighlight(code: String, oldTree: Tree? = null): Tree = parse(toParserInput(code), oldTree = oldTree)

/**
 * Replaces NUL and every non-ASCII character of [code] with as many ASCII characters as its
 * UTF-8 encoding has bytes, so byte offsets of the result equal UTF-8 byte offsets of [code] and
 * [Utf8ByteIndex] built from [code] maps parser offsets back to [code]'s char indices.
 *
 * The placeholder is a letter even for Unicode whitespace: Markdown, YAML, TOML and Python's
 * indentation treat only ASCII whitespace as whitespace, so mapping e.g. U+3000 to spaces would
 * turn indented prose into code blocks or list items. A leading BOM maps to spaces because
 * grammars such as JavaScript and Python skip it as whitespace.
 */
internal fun toParserInput(code: String): String {
  if (code.all { it.code in 1..0x7F }) return code
  val builder = StringBuilder(code.length)
  var i = 0
  while (i < code.length) {
    val c = code[i]
    when {
      c.code in 1..0x7F -> builder.append(c)

      c.code == 0 -> builder.append(PLACEHOLDER)

      i == 0 && c == BOM -> builder.append("   ")

      else -> {
        val cp = code.codePointAtCompat(i)
        repeat(utf8Length(cp)) { builder.append(PLACEHOLDER) }
        if (cp >= 0x10000) i++
      }
    }
    i++
  }
  return builder.toString()
}

private const val PLACEHOLDER = 'x'
private const val BOM = '﻿'
