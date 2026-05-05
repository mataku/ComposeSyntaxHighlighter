# Compose Highlight

Compose Multiplatform syntax highlighter for Android (and any JVM-based Compose target). Built on
tree-sitter for accurate, fast incremental highlighting.

This is still an experimental project. Android is supported via the published AAR; the JVM artifact ships for Compose Desktop interop. iOS lands in a later release; for browser apps prefer a JS-side highlighter (e.g. highlight.js, Shiki) and keep this library on JVM-based targets.

## Installation

Artifacts are published on Maven Central. Add `mavenCentral()` to your repositories and depend on
`compose-highlight-core` plus whichever language modules you need:

```kotlin
// build.gradle.kts (commonMain)
implementation("io.github.mataku:compose-highlight-core:0.1.0")
implementation("io.github.mataku:compose-highlight-kotlin:0.1.0")
implementation("io.github.mataku:compose-highlight-swift:0.1.0")
```

`compose-highlight-core` ships the `SyntaxHighlightedText` composable and built-in themes; each `compose-highlight-<lang>` artifact ships its tree-sitter grammar and the `Language` value you pass to the composable. Bumping `compose-highlight-core` to pick up new themes does not require updating the language artifacts.

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

The default theme is `SyntaxTheme.DarkDefault`. To override it for a single call, pass `theme` directly:

```kotlin
SyntaxHighlightedText(
  code = code,
  language = KotlinLanguage,
  theme = SyntaxTheme.LightDefault
)
```

Or override for an entire subtree with `CompositionLocalProvider`:

```kotlin
import io.github.mataku.compose.highlight.core.LocalSyntaxTheme
import io.github.mataku.compose.highlight.core.SyntaxTheme

CompositionLocalProvider(LocalSyntaxTheme provides SyntaxTheme.LightDefault) {
  SyntaxHighlightedText(code = code, language = KotlinLanguage)
}
```

## Supported

| Platform     | Status       | Notes                                                 |
|--------------|--------------|-------------------------------------------------------|
| Android      | yes          | minSdk 26                                             |
| Desktop JVM  | yes          | KTreeSitter native lib must be on `java.library.path` |
| Web (wasmJs) | not in scope | use highlight.js / Shiki on the JS side               |
| iOS          | wip          | later release                                         |

| Language | Artifact                   |
|----------|----------------------------|
| Kotlin   | `compose-highlight-kotlin` |
| Swift    | `compose-highlight-swift`  |
| Ruby     | `compose-highlight-ruby`   |
| Rust     | `compose-highlight-rust`   |
| Python   | `compose-highlight-python` |
| Go       | `compose-highlight-go`     |
| Java     | `compose-highlight-java`   |

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

## Performance

`Language` instances pre-compile the tree-sitter highlights query on first access, so repeated
highlighting of different code snippets with the same language is fast. The table below shows the
full `highlight()` call measured on a single machine for realistic code samples (~75–150 lines).
These values illustrate relative differences between languages, not absolute guarantees.

**What is measured?** The benchmark targets the `highlight()` function, which is the
same call used internally by `SyntaxHighlightedText`. It covers the full end-to-end
pipeline: tree-sitter parsing, highlight-query matching, UTF-8 byte-to-char index
mapping, and building the final `AnnotatedString` with `SpanStyle` applied. The
returned `AnnotatedString` is ready to be passed directly to Compose `Text`.

The benchmark measures two distinct scenarios:

- **Cold start**: first `highlight()` call on a fresh `Language` instance, which
  includes the one-time tree-sitter query compilation.
- **Steady state**: repeated `highlight()` calls after the query is compiled.

Both values are shown below so you can judge the first-frame impact and the
ongoing per-call cost.

```
OS: Mac OS X (26.4.1)
Arch: aarch64
JVM: OpenJDK 64-Bit Server VM 21.0.11
Processors: 12
Max heap: 512 MB
Warmup iterations: 10
Measure iterations: 50
```

### Cold start (includes query compilation)

| Language | Lines | Cold (ms) |
|----------|-------|-----------|
| Kotlin   | 76    | 721.420   |
| Swift    | 91    | 380.727   |
| Ruby     | 91    | 49.026    |
| Rust     | 115   | 50.675    |
| Python   | 92    | 19.693    |
| Go       | 153   | 9.237     |
| Java     | 120   | 20.709    |

### Steady state (query already compiled)

| Language | Lines | Mean (ms) | Median (ms) | StdDev (ms) | P99 (ms) |
|----------|-------|-----------|-------------|-------------|----------|
| Kotlin   | 76    | 4.477     | 4.479       | 0.257       | 5.587    |
| Swift    | 91    | 3.858     | 3.838       | 0.089       | 4.138    |
| Ruby     | 91    | 3.515     | 3.503       | 0.079       | 3.884    |
| Rust     | 115   | 1.414     | 1.405       | 0.035       | 1.523    |
| Python   | 92    | 3.357     | 3.370       | 0.094       | 3.562    |
| Go       | 153   | 2.909     | 2.900       | 0.062       | 3.028    |
| Java     | 120   | 3.187     | 3.202       | 0.079       | 3.369    |

Run `./gradlew :benchmark:jvmTest` to reproduce on your own machine.

## License

MIT. Bundled grammars are all MIT-licensed:

- tree-sitter, KTreeSitter
- fwcd/tree-sitter-kotlin
- alex-pinkus/tree-sitter-swift
- tree-sitter/tree-sitter-ruby
- tree-sitter/tree-sitter-rust
- tree-sitter/tree-sitter-python
- tree-sitter/tree-sitter-go
- tree-sitter/tree-sitter-java

Full attributions are in the published `META-INF/NOTICE` of each language artifact.
