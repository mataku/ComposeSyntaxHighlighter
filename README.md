# Compose Syntax Highlight

Compose Multiplatform syntax highlighter for Android (and any JVM-based Compose target). Built on tree-sitter for accurate, fast incremental highlighting.

This is still an experimental project. Android is supported via the published AAR; the JVM artifact ships for Compose Desktop interop. iOS lands in a later release; for browser apps prefer a JS-side highlighter (e.g. highlight.js, Shiki) and keep this library on JVM-based targets.

**API documentation:** https://mataku.github.io/ComposeSyntaxHighlighter/ (latest release)

## Installation

Artifacts are published on Maven Central. Add `mavenCentral()` to your repositories and depend on a Material binding plus whichever language modules you need:

```kotlin
// build.gradle.kts (commonMain)
// Material3 binding — ships SyntaxHighlightedText backed by androidx.compose.material3.Text.
// Pulls in compose-syntax-highlight-core transitively, so you don't need to declare :core yourself.
implementation("io.github.mataku:compose-syntax-highlight-material3:$latestVersion")

// Language artifacts — one per language you want to highlight.
implementation("io.github.mataku:compose-syntax-highlight-kotlin:$latestHighlightKotlinVersion")
implementation("io.github.mataku:compose-syntax-highlight-swift:$latestHighlightSwiftVersion")
```

If you don't render through a Material binding — for example, you build your own `Text` on top of the produced `AnnotatedString`, or you only need `highlight()` to feed an existing UI — depend on `:core` directly instead of the Material binding:

```kotlin
implementation("io.github.mataku:compose-syntax-highlight-core:$latestVersion")
// and language dependencies you want to apply syntax highlight
implementation("io.github.mataku:compose-syntax-highlight-kotlin:$latestHighlightKotlinVersion")
```

`compose-syntax-highlight-core` ships `highlight()`, `rememberHighlightedString()`, the `SyntaxTheme` data class, and `LocalSyntaxTheme`. It depends only on `compose.runtime` and `compose.ui`, so it stays usable wherever you build your own UI on top of `AnnotatedString`. `compose-syntax-highlight-material3` adds the `SyntaxHighlightedText` composable backed by `androidx.compose.material3.Text` — drop it in if you render code blocks under a `MaterialTheme`. Each `compose-syntax-highlight-<lang>` artifact ships its tree-sitter grammar and the `Language` value you pass to the composable. Bumping `compose-syntax-highlight-core` to pick up new themes does not require updating the Material binding or the language artifacts.

## Usage

```kotlin
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.kotlin.kotlin
import io.github.mataku.compose.highlight.material3.SyntaxHighlightedText

@Composable
fun MyScreen() {
  SyntaxHighlightedText(
    code = """
            fun main() {
                println("hello")
            }
        """.trimIndent(),
    language = Languages.Kotlin,
  )
}
```

The default theme is `SyntaxTheme.DarkDefault`. To override it for a single call, pass `theme` directly:

```kotlin
SyntaxHighlightedText(
  code = code,
  language = Languages.Kotlin,
  theme = SyntaxTheme.LightDefault
)
```

Or override for an entire subtree with `CompositionLocalProvider`:

```kotlin
import io.github.mataku.compose.highlight.core.LocalSyntaxTheme
import io.github.mataku.compose.highlight.core.SyntaxTheme

CompositionLocalProvider(LocalSyntaxTheme provides SyntaxTheme.LightDefault) {
  SyntaxHighlightedText(code = code, language = Languages.Kotlin)
}
```

### Background and padding

`SyntaxHighlightedText` paints `theme.background` behind the text when it is set (every built-in theme defines one). Layering, from outside in, is `modifier` → background → `contentPadding` → text, so use `modifier` for outer margin and `contentPadding` for the inset between the background edge and the text:

```kotlin
SyntaxHighlightedText(
  code = code,
  language = Languages.Kotlin,
  modifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp), // outside the background
  contentPadding = PaddingValues(16.dp), // inside the background
)
```

To suppress the background and paint your own container, override the theme:

```kotlin
SyntaxHighlightedText(
  code = code,
  language = Languages.Kotlin,
  theme = SyntaxTheme.DarkDefault.copy(background = null),
)
```

### Selection

`SyntaxHighlightedText` wraps its output in a `SelectionContainer` by default, so users can highlight the rendered code with the platform's native text-selection UI (which also surfaces a Copy action on Android and Desktop). Pass `selectable = false` to render without a selection scope:

```kotlin
SyntaxHighlightedText(
  code = code,
  language = Languages.Kotlin,
  selectable = false,
)
```

## Built-in themes

All themes are static `SyntaxTheme` values on `SyntaxTheme.Companion`, shipped in `compose-syntax-highlight-core`. Attributions for the third-party themes are bundled in the artifact's `META-INF/NOTICE`.

| Theme                       | Variant     | Inspired by / source                                                                                |
|-----------------------------|-------------|-----------------------------------------------------------------------------------------------------|
| `SyntaxTheme.DarkDefault`   | dark        | VSCode-inspired neutral palette (default for `LocalSyntaxTheme`)                                    |
| `SyntaxTheme.LightDefault`  | light       | VSCode-inspired neutral palette                                                                     |
| `SyntaxTheme.SolarizedDark` | dark        | [Solarized](https://github.com/altercation/solarized) by Ethan Schoonover                           |
| `SyntaxTheme.SolarizedLight`| light       | [Solarized](https://github.com/altercation/solarized) by Ethan Schoonover                           |
| `SyntaxTheme.GitHubDark`    | dark        | [GitHub Primer](https://github.com/primer/primer-primitives) syntax tokens                          |
| `SyntaxTheme.GitHubLight`   | light       | [GitHub Primer](https://github.com/primer/primer-primitives) syntax tokens                          |
| `SyntaxTheme.OneDark`       | dark        | [Atom One Dark](https://github.com/atom/atom) (one-dark-syntax)                                     |
| `SyntaxTheme.OneLight`      | light       | [Atom One Light](https://github.com/atom/atom) (one-light-syntax)                                   |
| `SyntaxTheme.Dracula`       | dark only   | [Dracula](https://github.com/dracula/dracula-theme) (no canonical light variant)                    |

## Supported

| Platform     | Status       | Notes                                                 |
|--------------|--------------|-------------------------------------------------------|
| Android      | yes          | minSdk 26                                             |
| Desktop JVM  | yes          | KTreeSitter native lib must be on `java.library.path` |
| Web (wasmJs) | not in scope | use highlight.js / Shiki on the JS side               |
| iOS          | wip          | later release                                         |

| Module                               | Artifact                                       | Purpose                                       |
|--------------------------------------|------------------------------------------------|-----------------------------------------------|
| Core highlighter                     | `compose-syntax-highlight-core`                | `highlight()`, `SyntaxTheme`, builtin themes  |
| Material3 binding                    | `compose-syntax-highlight-material3`           | `SyntaxHighlightedText` for Material3         |

| Language | Artifact                                  |
|----------|-------------------------------------------|
| Kotlin   | `compose-syntax-highlight-kotlin`         |
| Swift    | `compose-syntax-highlight-swift`          |
| Ruby     | `compose-syntax-highlight-ruby`           |
| Rust     | `compose-syntax-highlight-rust`           |
| Python   | `compose-syntax-highlight-python`         |
| Go       | `compose-syntax-highlight-go`             |
| Java     | `compose-syntax-highlight-java`           |

## Custom theme

Each capture has its own named field, so the IDE autocompletes them — no string keys to remember:

```kotlin
val myTheme = SyntaxTheme(
  baseStyle = SpanStyle(color = Color.White),
  keyword = SpanStyle(color = Color.Magenta, fontWeight = FontWeight.Bold),
  string = SpanStyle(color = Color.Yellow),
  // ...
)
```

Unset fields fall back to a parent prefix — setting `keyword` covers `keyword.return`, `keyword.function`, etc., and `string.escape` falls back to `string` when `stringEscape` is unset. For grammar-specific captures not covered by a field (e.g. `keyword.return`, `variable.member`), use `extras`:

```kotlin
val myTheme = SyntaxTheme(
  keyword = SpanStyle(color = Color.Magenta),
  extras = mapOf("keyword.return" to SpanStyle(color = Color.Red)),
)
```

`extras` wins over typed fields for the same name.

## Performance

`Language` instances pre-compile the tree-sitter highlights query on first access, so repeated highlighting of different code snippets with the same language is fast. The table below shows the full `highlight()` call measured on a single machine for realistic code samples (~75–150 lines).

These values illustrate relative differences between languages, not absolute guarantees.

### What is measured?

The benchmark targets the `highlight()` function, which is the same call used internally by `SyntaxHighlightedText`. It covers the full end-to-end pipeline: tree-sitter parsing, highlight-query matching, UTF-8 byte-to-char index mapping, and building the final `AnnotatedString` with `SpanStyle` applied. The returned `AnnotatedString` is ready to be passed directly to Compose `Text`.

The benchmark measures two distinct scenarios from the perspective of a Compose app:

- First use: the first time you display a code block with a given language.
  This triggers one-time tree-sitter query compilation under the hood, so it is
  slower.
- Subsequent use: displaying another code block with the same language after
  the first. The query is already compiled, so this reflects the actual per-call
  cost during normal app usage.

Both values are shown below so you can judge the one-time initial impact and the ongoing per-call cost.

```
OS: Mac OS X (26.4.1)
Arch: aarch64
JVM: OpenJDK 64-Bit Server VM 21.0.11
Processors: 12
Max heap: 512 MB
Warmup iterations: 10
Measure iterations: 50
```

### First use [Cold] (includes query compilation)

| Language | Lines | First use [Cold] (ms) |
|----------|-------|----------------------|
| Kotlin   | 100   | 533.608              |
| Swift    | 100   | 378.779              |
| Ruby     | 100   | 49.571               |
| Rust     | 100   | 52.961               |
| Python   | 100   | 19.873               |
| Go       | 100   | 9.311                |
| Java     | 100   | 20.797               |

### Subsequent use [Warm] (query already compiled)

| Language | Lines | Mean (ms) | Median (ms) | StdDev (ms) | P99 (ms) |
|----------|-------|-----------|-------------|-------------|----------|
| Kotlin   | 100   | 5.348     | 5.327       | 0.270       | 6.564    |
| Swift    | 100   | 3.786     | 3.776       | 0.079       | 4.039    |
| Ruby     | 100   | 3.544     | 3.520       | 0.091       | 3.779    |
| Rust     | 100   | 3.508     | 3.521       | 0.099       | 3.693    |
| Python   | 100   | 3.464     | 3.460       | 0.069       | 3.614    |
| Go       | 100   | 2.337     | 2.335       | 0.045       | 2.438    |
| Java     | 100   | 3.296     | 3.287       | 0.106       | 3.600    |

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

The built-in `SyntaxTheme` palettes shipped in `compose-syntax-highlight-core` derive their colors from the following third-party themes (all MIT-licensed):

- [Solarized](https://github.com/altercation/solarized) — Copyright (c) 2011 Ethan Schoonover
- [GitHub Primer Syntax Themes](https://github.com/primer/primer-primitives) — Copyright (c) GitHub, Inc.
- [Atom One Dark / One Light](https://github.com/atom/atom) — Copyright (c) 2011-present GitHub, Inc.
- [Dracula](https://github.com/dracula/dracula-theme) — Copyright (c) 2016 Dracula Theme, LLC

Full attributions are in the published `META-INF/NOTICE` of each language artifact and of `compose-syntax-highlight-core`.
