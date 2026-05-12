package io.github.mataku.compose.highlight.markdownInline

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.markdownInline.internal.TreeSitterMarkdownInline
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitterMarkdownInline.language()), HIGHLIGHTS_QUERY)
}

/**
 * Inline-level Markdown highlighting.
 *
 * Designed to be applied to the byte ranges identified as `(inline)` nodes by the block
 * grammar (see `Languages.Markdown`); not generally useful on its own against whole
 * documents because it does not understand block structure (headings, fenced code, etc.).
 *
 * Capture names emitted by the upstream inline query (`text.emphasis`, `text.strong`,
 * `text.literal`, `text.uri`, `text.reference`) have no typed field on `SyntaxTheme`; style
 * them via `SyntaxTheme.extras`. See `Languages.Markdown` KDoc for example overlay.
 */
val Languages.MarkdownInline: Language get() = instance
