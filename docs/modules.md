# Module reference

Per-module Maven coordinates, public API, and source-set notes. Every target is JVM-based — there is no `expect/actual`; production code lives in `commonMain`. Versions live in `gradle/libs.versions.toml`.

## `:core-api`

Cross-module SPI consumed by language modules and `:core`. Re-exports:

- `Language` — concrete class wrapping a KTreeSitter `Language` + a highlights query.
- `kTreeSitterLanguage(parser, highlightsQuery)` — factory used by language modules.
- `Languages` — receiver object that language modules attach extension `val`s to (e.g. `Languages.Kotlin`).

## `:core`

Maven coordinates: `io.github.mataku:compose-syntax-highlight-core:<version>`.

Public API in `commonMain`:

- `SyntaxTheme(baseStyle, background, keyword, function, ..., extras)` — typed `SpanStyle?` fields per tree-sitter capture (the 14 used by every built-in theme), plus `extras: Map<String, SpanStyle>` for grammar-specific captures. Provides `SyntaxTheme.DarkDefault` / `SyntaxTheme.LightDefault` and the named built-in palettes.
- `LocalSyntaxTheme` — composition local that defaults to `SyntaxTheme.DarkDefault`.
- `highlight(code, language, theme)` / `rememberHighlightedString(...)` — one-shot highlighting.
- `IncrementalHighlighter(language)` — single-threaded incremental engine. `AutoCloseable`; route every `update`/`close` call through one coroutine. Drives editable surfaces.
- Re-exports `Language` and `kTreeSitterLanguage` from `:core-api`.

Implementation files: `core/src/commonMain/kotlin/io/github/mataku/compose/highlight/core/`.

## `:material3`

Maven coordinates: `io.github.mataku:compose-syntax-highlight-material3:<version>`.

Public API in `commonMain`:

- `SyntaxHighlightedText(code, language, theme, style, modifier)` — read-only composable that renders highlighted code via `androidx.compose.material3.Text`, pulling the default `style` from `LocalTextStyle` (Material3) with `FontFamily.Monospace`.

Depends on `:core` for `rememberHighlightedString`, `SyntaxTheme`, and `LocalSyntaxTheme`. Future Material releases (e.g. M4) ship as sibling modules without touching `:core`.

Implementation file: `material3/src/commonMain/kotlin/io/github/mataku/compose/highlight/material3/SyntaxHighlightedText.kt`.

## `:material3-text-field`

Maven coordinates: `io.github.mataku:compose-syntax-highlight-material3-text-field:<version>`.

Public API in `commonMain`:

- `SyntaxHighlightedTextField(state, language, ...)` — editable composable that layers a transparent `BasicTextField` beneath a `Text` painting the highlighted form. Both children share `TextStyle` and `ScrollState` so glyphs sit at identical positions; the `BasicTextField` owns cursor/selection/IME, the overlay `Text` owns colour.
- `rememberSyntaxHighlightedString(state, language, theme)` — `State<AnnotatedString>` driven by an `IncrementalHighlighter` whose lifetime tracks `(state, language)`. Theme changes do not rebuild the engine.

Depends on `:core` for `IncrementalHighlighter`, `SyntaxTheme`, and `LocalSyntaxTheme`.

Implementation files: `material3-text-field/src/commonMain/kotlin/io/github/mataku/compose/highlight/material3/textfield/`.

## `:languages:<name>`

Maven coordinates: `io.github.mataku:compose-syntax-highlight-<name>:<version>`. Each language module:

- Applies the custom `compose-syntax-highlight-language` convention plugin.
- Vendors a `tree-sitter-<lang>` grammar submodule.
- Exposes a single `val <Lang>Language: Language` from `commonMain` and an extension `Languages.<Name>` forwarder.

Build internals (plugin DSL, generated tasks, KMP source-set layout, NDK/CMake setup, ABI-14 pin trap, platform notes): see [`build-logic.md`](build-logic.md).

## `:benchmark`

JVM and Android instrumented benchmarks for the highlight pipeline. Excluded from `./gradlew jvmTest` by default — run `:benchmark:jvmTest` for the host-JVM suite, or the Android instrumented variant from a connected device.

- `commonTest` / `jvmTest` — host-JVM benchmarks (`HighlightBenchmark`, `LargeInputBenchmark`, `IncrementalHighlighterBenchmark`). Numbers feeding `README.md` and [`large_input_profiling.md`](large_input_profiling.md) come from these.
- `androidInstrumentedTest` — Android benchmarks driven by `androidx.benchmark:benchmark-junit4` with `AndroidBenchmarkRunner` (`testBuildType = "benchmark"`).

KMP forbids `dependsOn` between source sets in different test trees, so `androidInstrumentedTest` shares `BenchmarkSamples` / `BenchmarkConfig` with `commonTest` by physically including its Kotlin srcDir (see `benchmark/build.gradle.kts`).

## `:composeApp`

Demo app (Android only). Wires every shipped language and theme for manual smoke testing; not published.
