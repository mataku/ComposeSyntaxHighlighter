package io.github.mataku.compose.highlight.markdown

import io.github.mataku.compose.highlight.api.Injection
import io.github.mataku.compose.highlight.api.InternalSyntaxHighlightApi
import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.markdown.internal.TreeSitterMarkdown
import io.github.mataku.compose.highlight.markdownInline.MarkdownInline
import io.github.treesitter.ktreesitter.Language as TsLanguage

private const val INJECTIONS_QUERY = """
((inline) @injection.content)
"""

@OptIn(InternalSyntaxHighlightApi::class)
private val instance: Language by lazy {
  val blockParser = TsLanguage(TreeSitterMarkdown.language())
  val injectionsQuery = blockParser.query(INJECTIONS_QUERY)
  kTreeSitterLanguage(
    blockParser,
    HIGHLIGHTS_QUERY,
    listOf(
      Injection(
        injectionsQuery = injectionsQuery,
        injectionsQuerySource = INJECTIONS_QUERY,
        target = Languages.MarkdownInline,
      ),
    ),
  )
}

/**
 * Markdown highlighting, covering both block structure (headings, list markers, fenced code
 * blocks, link destinations) and paragraph-body inline constructs (emphasis, strong, link
 * text, code spans, image components, hard line breaks, backslash escapes).
 *
 * Backed by the split block + inline grammars from
 * `tree-sitter-grammars/tree-sitter-markdown`. Uses the core sub-tree injection mechanism:
 * block parse runs on the whole document, inline highlights re-apply to each `(inline)` byte
 * range.
 *
 * Capture names emitted by the upstream queries (`text.title`, `text.literal`, `text.uri`,
 * `text.reference`, `text.emphasis`, `text.strong`) have no typed field on `SyntaxTheme`;
 * style them via `extras`:
 *
 * ```kotlin
 * theme.copy(
 *   extras = mapOf(
 *     "text.title"     to SpanStyle(fontWeight = FontWeight.Bold),
 *     "text.emphasis"  to SpanStyle(fontStyle = FontStyle.Italic),
 *     "text.strong"    to SpanStyle(fontWeight = FontWeight.Bold),
 *     "text.literal"   to SpanStyle(fontFamily = FontFamily.Monospace),
 *     "text.uri"       to SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
 *     "text.reference" to SpanStyle(color = Color.Cyan),
 *   ),
 * )
 * ```
 *
 * `punctuation.special` / `punctuation.delimiter` fall back to the typed `punctuation` field
 * via `SyntaxTheme.resolve`'s dotted-prefix lookup; `string.escape` falls back to the typed
 * `stringEscape` field.
 *
 * Fenced-code-block multi-language injection (highlighting Kotlin / Python / etc. inside
 * fenced code) is not supported — fenced bodies render under the base style. Future work
 * may add this via the same injection mechanism.
 */
val Languages.Markdown: Language get() = instance
