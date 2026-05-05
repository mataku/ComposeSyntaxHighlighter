# Compose Highlight

Compose Multiplatform syntax highlighter for Android (and any JVM-based Compose target). Built on
tree-sitter for accurate, fast incremental highlighting.

> **Status:** `0.1.0-SNAPSHOT` is available on Maven Central's snapshot repository while the first
> stable release is being prepared. Android is supported via the published AAR; the JVM artifact ships
> for Compose Desktop interop. iOS lands in a later release; for browser apps prefer a JS-side
> highlighter (e.g. highlight.js, Shiki) and keep this library on JVM-based targets.

## Installation

TBA — the first stable `0.1.0` release is being prepared and will be published to Maven Central.

### Trying the snapshot

A `0.1.0-SNAPSHOT` build is available on Maven Central's snapshot repository if you want to try the
library now. Add the snapshot repository and depend on the language modules you need:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
      mavenContent { snapshotsOnly() }
    }
  }
}
```

```kotlin
// build.gradle.kts (commonMain)
implementation("io.github.mataku:compose-highlight-core:0.1.0-SNAPSHOT")
implementation("io.github.mataku:compose-highlight-kotlin:0.1.0-SNAPSHOT")
implementation("io.github.mataku:compose-highlight-swift:0.1.0-SNAPSHOT")
```

`compose-highlight-core` ships the `SyntaxHighlightedText` composable and built-in
themes; each `compose-highlight-<lang>` artifact ships its tree-sitter grammar and
the `Language` value you pass to the composable. Bumping `compose-highlight-core` to
pick up new themes does not require updating the language artifacts.

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

The default theme is `SyntaxTheme.DarkDefault`. To override it for a single call, pass `theme`
directly:

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

Capture name resolution falls back to parent prefixes — defining `keyword` covers `keyword.return`,
`keyword.function`, etc. unless a more specific entry is provided.

## Performance

`Language` instances pre-compile the tree-sitter highlights query on first access, so repeated
highlighting of different code snippets with the same language is fast. The table below shows the
full `highlight()` call measured on a single machine. Values are for small code snippets
(~10–15 lines) and illustrate relative differences, not absolute guarantees.

```
OS: Mac OS X (26.4.1)
Arch: aarch64
JVM: OpenJDK 64-Bit Server VM 21.0.11
Processors: 12
Max heap: 512 MB
Warmup iterations: 10
Measure iterations: 50
```

| Language | Mean (ms) | Median (ms) | StdDev (ms) | Min (ms) | Max (ms) | P99 (ms) |
|----------|-----------|-------------|-------------|----------|----------|----------|
| Kotlin   | 0.510     | 0.509       | 0.010       | 0.493    | 0.539    | 0.539    |
| Swift    | 0.293     | 0.291       | 0.006       | 0.284    | 0.312    | 0.312    |
| Ruby     | 0.286     | 0.284       | 0.017       | 0.257    | 0.331    | 0.331    |
| Rust     | 0.279     | 0.277       | 0.005       | 0.272    | 0.297    | 0.297    |
| Python   | 0.208     | 0.209       | 0.006       | 0.194    | 0.230    | 0.230    |
| Go       | 0.200     | 0.198       | 0.017       | 0.184    | 0.310    | 0.310    |
| Java     | 0.255     | 0.254       | 0.008       | 0.233    | 0.275    | 0.275    |

Run `./gradlew :benchmark:jvmTest` to reproduce on your own machine.

## License

MIT. Bundled grammars (tree-sitter, KTreeSitter, fwcd/tree-sitter-kotlin,
alex-pinkus/tree-sitter-swift) are MIT-licensed; full attributions are in the published
`META-INF/NOTICE` of each language artifact.
