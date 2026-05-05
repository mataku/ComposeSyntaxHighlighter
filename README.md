# Compose Highlight

Compose Multiplatform syntax highlighter for Android (and any JVM-based Compose target). Built on tree-sitter for accurate, fast incremental highlighting.

> **Status:** 0.1.0. Android supported via the published AAR. The JVM artifact ships for Compose Desktop interop. iOS lands in a later release; for browser apps prefer a JS-side highlighter (e.g. highlight.js, Shiki) and keep this library on JVM-based targets.

## Installation

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories { mavenCentral() }
}
```

```kotlin
// build.gradle.kts (commonMain)
// Declare only the language modules you need.
implementation("io.github.mataku:compose-highlight-kotlin:0.1.0")
implementation("io.github.mataku:compose-highlight-swift:0.1.0")
```

## Basic usage

```kotlin
import io.github.mataku.compose.highlight.core.SyntaxHighlightedText
import io.github.mataku.compose.highlight.kotlin.KotlinLanguage

@Composable
fun MyScreen() {
    SyntaxHighlightedText(
        code = """
            fun main() {
                println("hello")
            }
        """.trimIndent(),
        language = KotlinLanguage,
    )
}
```

The default theme is `SyntaxTheme.darkDefault()`. To override per-screen:

```kotlin
import io.github.mataku.compose.highlight.core.LocalSyntaxTheme
import io.github.mataku.compose.highlight.core.SyntaxTheme

CompositionLocalProvider(LocalSyntaxTheme provides SyntaxTheme.lightDefault()) {
    SyntaxHighlightedText(code = code, language = KotlinLanguage)
}
```

Or pass `theme` directly:

```kotlin
SyntaxHighlightedText(code = code, language = KotlinLanguage, theme = myCustomTheme)
```

## Supported

| Platform     | Status     | Notes                                              |
|--------------|------------|----------------------------------------------------|
| Android      | yes        | minSdk 26                                          |
| Desktop JVM  | yes        | KTreeSitter native lib must be on `java.library.path` |
| Web (wasmJs) | not in scope | use highlight.js / Shiki on the JS side          |
| iOS          | wip        | later release                                      |

| Language | Artifact                         |
|----------|----------------------------------|
| Kotlin   | `compose-highlight-kotlin` |
| Swift    | `compose-highlight-swift`  |

## Custom theme

```kotlin
val myTheme = SyntaxTheme(
    baseStyle = SpanStyle(color = Color.White),
    styles = mapOf(
        "keyword" to SpanStyle(color = Color.Magenta, fontWeight = FontWeight.Bold),
        "string" to SpanStyle(color = Color.Yellow),
        // ...
    ),
)
```

Capture name resolution falls back to parent prefixes — defining `keyword` covers `keyword.return`, `keyword.function`, etc. unless a more specific entry is provided.

## License

MIT. Bundled grammars (tree-sitter, KTreeSitter, fwcd/tree-sitter-kotlin, alex-pinkus/tree-sitter-swift) are MIT-licensed; full attributions are in the published `META-INF/NOTICE` of each language artifact.
