# `<Lang>HighlightTest.kt` template

Place at `languages/<lang>/src/jvmTest/kotlin/io/github/mataku/compose/highlight/<lang>/<Lang>HighlightTest.kt`. Substitute the placeholders and adjust the keyword / comment syntax to the language.

```kotlin
package io.github.mataku.compose.highlight.<lang>

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class <Lang>HighlightTest {

    private val keywordColor = Color.Red
    private val stringColor = Color.Green
    private val commentColor = Color.Blue
    private val numberColor = Color.Yellow
    private val baseColor = Color.White

    private val theme = SyntaxTheme(
        baseStyle = SpanStyle(color = baseColor),
        styles = mapOf(
            "keyword" to SpanStyle(color = keywordColor),
            "string" to SpanStyle(color = stringColor),
            "comment" to SpanStyle(color = commentColor),
            "number" to SpanStyle(color = numberColor),
            // If upstream uses a non-default capture for numeric literals
            // (e.g. Rust uses "constant.builtin"), add it here mapping to
            // numberColor. SyntaxTheme.resolve() does dotted-prefix fallback,
            // so "constant.builtin" falls back to "constant" if you prefer
            // to map the broader category.
        ),
    )

    @Test
    fun keyword_<example>_is_styled() {
        // Pick the shortest unambiguous keyword in the language. The end
        // index is the keyword length (e.g. "def" → 3, "fn" → 2, "func" → 4).
        val code = "<keyword> foo"
        val annotated = highlight(code, <Lang>Language, theme)
        val keywordSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == <keyword-length> }
        assertEquals(keywordColor, keywordSpan?.item?.color, "expected keyword 0..<keyword-length> styled with keywordColor; spans=${annotated.spanStyles}")
    }

    @Test
    fun string_literal_is_styled() {
        // For languages with strict span boundaries (Ruby, Kotlin), assert
        // the exact open..close range. For looser grammars (Swift, Rust) the
        // upstream highlights.scm may emit multiple sub-spans inside the
        // string — fall back to "any span has stringColor".
        val code = """<assignment-prefix>"hi""""
        val annotated = highlight(code, <Lang>Language, theme)
        assertTrue(
            annotated.spanStyles.any { it.item.color == stringColor },
            "expected at least one string-styled span; spans=${annotated.spanStyles}",
        )
    }

    @Test
    fun line_comment_is_styled() {
        // Use the language's line-comment marker (# Ruby, // Rust/Swift/Kotlin, -- Lua).
        val code = "<line-comment-marker> hello\n<assignment>"
        val annotated = highlight(code, <Lang>Language, theme)
        val commentEnd = code.indexOf('\n')
        assertTrue(
            annotated.spanStyles.any { it.start == 0 && it.end == commentEnd && it.item.color == commentColor },
            "expected comment 0..$commentEnd styled with commentColor; spans=${annotated.spanStyles}",
        )
    }

    @Test
    fun integer_literal_is_styled() {
        val code = "<assignment-of> 42"
        val annotated = highlight(code, <Lang>Language, theme)
        val start = code.indexOf("42")
        val end = start + 2
        assertTrue(
            annotated.spanStyles.any { it.start == start && it.end == end && it.item.color == numberColor },
            "expected number $start..$end styled with numberColor; spans=${annotated.spanStyles}",
        )
    }

    @Test
    fun base_style_covers_entire_code() {
        val code = "<keyword> foo"
        val annotated = highlight(code, <Lang>Language, theme)
        val baseSpan = annotated.spanStyles.firstOrNull { it.start == 0 && it.end == code.length && it.item.color == baseColor }
        assertTrue(baseSpan != null, "expected base style covering 0..${code.length}; spans=${annotated.spanStyles}")
    }
}
```

## Concrete examples in the repo

| Module | File |
|---|---|
| Kotlin | `languages/kotlin/src/jvmTest/kotlin/io/github/mataku/compose/highlight/kotlin/KotlinHighlightTest.kt` |
| Swift | `languages/swift/src/jvmTest/kotlin/io/github/mataku/compose/highlight/swift/SwiftHighlightTest.kt` |
| Ruby | `languages/ruby/src/jvmTest/kotlin/io/github/mataku/compose/highlight/ruby/RubyHighlightTest.kt` |
| Rust | `languages/rust/src/jvmTest/kotlin/io/github/mataku/compose/highlight/rust/RustHighlightTest.kt` |

Read whichever existing module is closest to the new language's syntax (curly-brace vs significant-indentation, explicit type annotations vs not, comment marker style) before writing the new test.

## Verifying upstream capture names

If a test fails because the upstream `highlights.scm` doesn't tag a construct the way you expect, grep the submodule directly:

```bash
grep -nE '@(keyword|string|comment|number|constant)' languages/<lang>/tree-sitter-<lang>/queries/highlights.scm
```

Then pick the actual capture name and add it to the test theme. Don't change the test assertions — change the theme map.
