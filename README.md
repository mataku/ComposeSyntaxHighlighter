# Compose Syntax Highlight

Compose Multiplatform syntax highlighter for Android (and any JVM-based Compose target). Built on tree-sitter for accurate, fast highlighting — with an incremental engine for editor scenarios.

This is still an experimental project. Android is supported via the published AAR; the JVM artifact ships for Compose Desktop interop; iOS is in preview (iosArm64 + iosSimulatorArm64 klibs — Apple Silicon only, no iosX64). For browser apps prefer a JS-side highlighter (e.g. highlight.js, Shiki) and keep this library on JVM-based targets.

**API documentation:** https://mataku.github.io/ComposeSyntaxHighlighter/ (latest release)

## Preview

| Read-only viewer (`:material3`)      | Text field (`:material3-text-field`)      |
|:---:|:---:|
| <image src="./misc/swift_viewer_demo.png" width=270 /> | <video src="https://github.com/user-attachments/assets/a675e40d-5433-45fe-b48a-27ac27cb5b3a" /> |

## Installation

| Platform     | Status       | Notes                                                 |
|--------------|--------------|-------------------------------------------------------|
| Android      | available    | minSdk 26                                             |
| Desktop JVM  | available    | KTreeSitter native lib must be on `java.library.path` |
| iOS          | preview      | iosArm64 + iosSimulatorArm64 klibs; Apple Silicon only (no iosX64) |
| Web (wasmJs) | not in scope | use highlight.js / Shiki on the JS side               |

| Module                  | Artifact                                        | Latest                                                                                                                                                                                                                       |
|-------------------------|-------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `:core`                 | `compose-syntax-highlight-core`                 | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-core.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-core)                                |
| `:material3`            | `compose-syntax-highlight-material3`            | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-material3.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-material3)                      |
| `:material3-text-field` | `compose-syntax-highlight-material3-text-field` | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-material3-text-field.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-material3-text-field) |

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}
```

Add a Material binding plus whichever language modules you need:

```kotlin
// build.gradle.kts (commonMain)
// Material3 binding — ships SyntaxHighlightedText. Pulls in :core transitively.
implementation("io.github.mataku:compose-syntax-highlight-material3:$latestVersion")

// Language artifacts — one per language you want to highlight.
implementation("io.github.mataku:compose-syntax-highlight-kotlin:$latestHighlightKotlinVersion")
implementation("io.github.mataku:compose-syntax-highlight-swift:$latestHighlightSwiftVersion")
```

| Language | Artifact                          | Latest                                                                                                                                                                                            |
|----------|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Kotlin   | `compose-syntax-highlight-kotlin` | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-kotlin.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-kotlin) |
| Swift    | `compose-syntax-highlight-swift`  | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-swift.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-swift)   |
| Ruby     | `compose-syntax-highlight-ruby`   | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-ruby.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-ruby)     |
| Rust     | `compose-syntax-highlight-rust`   | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-rust.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-rust)     |
| Python   | `compose-syntax-highlight-python` | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-python.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-python) |
| Go       | `compose-syntax-highlight-go`     | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-go.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-go)         |
| Java     | `compose-syntax-highlight-java`   | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-java.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-java)     |
| Markdown | `compose-syntax-highlight-markdown` | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-markdown.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-markdown) |
| JavaScript | `compose-syntax-highlight-javascript` | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-javascript.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-javascript) |
| TypeScript / TSX | `compose-syntax-highlight-typescript` | [![](https://img.shields.io/maven-central/v/io.github.mataku/compose-syntax-highlight-typescript.svg?label=)](https://central.sonatype.com/artifact/io.github.mataku/compose-syntax-highlight-typescript) |

For an **editable** code surface, depend on `:material3-text-field` instead of (or alongside) `:material3` — it ships `SyntaxHighlightedTextField`:

```kotlin
implementation("io.github.mataku:compose-syntax-highlight-material3-text-field:$latestVersion")
```

### Compatible versions

Each language module is published with its own version, independent from the
core stack. When the versions on your classpath are incompatible — for example,
upgrading the core stack across a major bump without also upgrading language
modules — Gradle reports a `strictly` conflict on
`compose-syntax-highlight-api`. See
[`docs/compatibility.md`](docs/compatibility.md) for the matrix of compatible
versions per artefact.

### Without a Material binding

If you build your own `Text` on top of the produced `AnnotatedString`, or you only need `highlight()` to feed an existing UI, depend on `:core` directly:

```kotlin
implementation("io.github.mataku:compose-syntax-highlight-core:$latestVersion")
implementation("io.github.mataku:compose-syntax-highlight-kotlin:$latestHighlightKotlinVersion")
```

`:core` ships `highlight()`, `rememberHighlightedString()`, the `SyntaxTheme` data class, and `LocalSyntaxTheme`, with only `compose.runtime` and `compose.ui` as dependencies.

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

### Editing code

For an **editable** syntax-highlighted text surface, use `SyntaxHighlightedTextField` from `:material3-text-field`:

```kotlin
var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
  mutableStateOf(TextFieldValue("val greeting = \"Hello\""))
}
SyntaxHighlightedTextField(
  value = value,
  onValueChange = { value = it },
  language = Languages.Kotlin,
  theme = SyntaxTheme.DarkDefault,
  modifier = Modifier.fillMaxSize(),
)
```

The Composable is backed by `IncrementalHighlighter` from `:core`: typing
recomputes only the edited byte range, so highlighting stays interactive on
multi-thousand-line files. Highlighting is painted onto the single
`BasicTextField` text layout by a `VisualTransformation`, so the caret and the
colour never drift. For non-Material3 chrome or custom layouts, use
`rememberSyntaxHighlightVisualTransformation` directly:

```kotlin
val transformation = rememberSyntaxHighlightVisualTransformation(value.text, Languages.Kotlin)
// pass `transformation` to your own BasicTextField(visualTransformation = ...).
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

`SyntaxHighlightedText` is synchronous by default — fine up to a few
hundred lines. For larger inputs, opt into the async path or
`IncrementalHighlighter`; see [`docs/performance.md`](docs/performance.md)
for behaviour, code examples, and benchmark methodology.

### Large input scaling

`full highlight` medians (warm), Kotlin:

| Runtime                      | 100 lines | 1k lines | 5k lines  |
|------------------------------|-----------|----------|-----------|
| Host JVM (heap 2g)           | 5.0 ms    | 48.8 ms  | 239.8 ms  |
| Android (BenchmarkRule, FTL) | 3.1 ms    | 30.3 ms  | 143.1 ms¹ |

Environment:

- Host JVM: Apple M3 Pro, OpenJDK 21, heap 2g
- Android: Pixel 10 (Tensor G5, Android 16), Firebase Test Lab

¹ Flagship-class only — see [`docs/performance.md`](docs/performance.md#benchmark-environment-full).

### Per-language cost

Realistic code samples (~75–150 lines, single machine). First use
includes one-time tree-sitter query compilation; subsequent use
reflects normal app cost.

#### First use [Cold]

| Language | Lines | Min (ms) | Median (ms) | Max (ms) |
|----------|-------|---------:|------------:|---------:|
| Kotlin   | 100   | 1083.737 | 1150.319 | 1176.068 |
| Swift    | 100   | 728.042 | 751.514 | 760.699 |
| Ruby     | 100   | 397.201 | 412.628 | 440.905 |
| Rust     | 100   | 391.256 | 411.147 | 412.385 |
| Python   | 100   | 370.287 | 372.377 | 467.543 |
| Go       | 100   | 320.408 | 325.528 | 355.918 |
| Java     | 100   | 341.338 | 362.281 | 408.819 |

#### Subsequent use [Warm]

| Language | Lines | Min (ms) | Median (ms) | Max (ms) |
|----------|-------|---------:|------------:|---------:|
| Kotlin   | 100   | 4.707 | 4.959 | 5.053 |
| Swift    | 100   | 3.539 | 3.716 | 3.775 |
| Ruby     | 100   | 3.424 | 3.505 | 3.511 |
| Rust     | 100   | 3.356 | 3.437 | 3.452 |
| Python   | 100   | 3.390 | 3.471 | 3.474 |
| Go       | 100   | 2.341 | 2.346 | 2.353 |
| Java     | 100   | 3.040 | 3.153 | 3.205 |

Environment: Apple M3 Pro, OpenJDK 21, heap 2g, warmup 100 / measure 50.
Full config and methodology: [`docs/performance.md`](docs/performance.md#what-the-benchmarks-measure).

Run `./gradlew :benchmarks:jvm:jvmTest` to reproduce on your own machine.

## License

MIT. Bundled grammars are all MIT-licensed:

- tree-sitter, KTreeSitter
- [fwcd/tree-sitter-kotlin](https://github.com/fwcd/tree-sitter-kotlin) — Copyright (c) 2019 fwcd
- [alex-pinkus/tree-sitter-swift](https://github.com/alex-pinkus/tree-sitter-swift) — Copyright (c) 2021 alex-pinkus
- [tree-sitter/tree-sitter-ruby](https://github.com/tree-sitter/tree-sitter-ruby) — Copyright (c) 2016 Rob Rix
- [tree-sitter/tree-sitter-rust](https://github.com/tree-sitter/tree-sitter-rust) — Copyright (c) 2017 Maxim Sokolov
- [tree-sitter/tree-sitter-python](https://github.com/tree-sitter/tree-sitter-python) — Copyright (c) 2016 Max Brunsfeld
- [tree-sitter/tree-sitter-go](https://github.com/tree-sitter/tree-sitter-go) — Copyright (c) 2014 Max Brunsfeld
- [tree-sitter/tree-sitter-java](https://github.com/tree-sitter/tree-sitter-java) — Copyright (c) 2017 Ayman Nadeem
- [tree-sitter-grammars/tree-sitter-markdown](https://github.com/tree-sitter-grammars/tree-sitter-markdown) — Copyright (c) 2021 Matthias Deiml
- [tree-sitter/tree-sitter-javascript](https://github.com/tree-sitter/tree-sitter-javascript) — Copyright (c) 2014 Max Brunsfeld
- [tree-sitter/tree-sitter-typescript](https://github.com/tree-sitter/tree-sitter-typescript) — Copyright (c) 2017 Max Brunsfeld

The built-in `SyntaxTheme` palettes shipped in `compose-syntax-highlight-core` derive their colors from the following third-party themes (all MIT-licensed):

- [Solarized](https://github.com/altercation/solarized) — Copyright (c) 2011 Ethan Schoonover
- [GitHub Primer Syntax Themes](https://github.com/primer/primer-primitives) — Copyright (c) 2018 GitHub Inc.
- [Atom One Dark / One Light](https://github.com/atom/atom) — Copyright (c) 2011-2022 GitHub Inc.
- [Dracula](https://github.com/dracula/dracula-theme) — Copyright (c) 2023 Dracula Theme

Full attributions are in the published `META-INF/NOTICE` of each language artifact and of `compose-syntax-highlight-core`.
