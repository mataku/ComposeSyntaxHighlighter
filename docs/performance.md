# Performance guide

User-facing notes on how `compose-syntax-highlight` runs, when to opt
into the async or incremental paths, and what the benchmark numbers in
the README actually measure.

## Synchronous default

`SyntaxHighlightedText` is synchronous by default: when the composition
recomputes, `highlight()` runs on the calling thread. For typical inline
code (a few dozen lines) that is the right choice — the work is
sub-millisecond and adding a coroutine round-trip would only introduce a
one-frame plain-text flicker. As a guideline, the synchronous path is
fine up to a few hundred lines.

## Async highlight path

For larger blocks, opt in to the async path:

```kotlin
SyntaxHighlightedText(
  code = code,
  language = Languages.Kotlin,
  async = true,
)
```

When `async = true`, the rendered text starts as plain `code` styled with
`theme.baseStyle` and updates to the highlighted form once `highlight()`
completes on `Dispatchers.Default` (override via `asyncContext` if you
have your own pool). On `code` change, the state immediately resets to
the new plain text so the displayed code never lags behind the request.

The async path is targeted at static code blocks. Each `code` change
cancels the previous coroutine, but the in-flight native parse
(JNI-side, via tree-sitter) cannot be cancelled mid-flight — it runs to
completion before the next parse starts. Live-editor usage where `code`
changes on every keystroke is **not** the intended scenario for this
library — see [Editing code](#editing-code) below.

## Theme switching (tree cache)

`rememberHighlightedString` caches the parsed tree per `(code, language)`,
so toggling between Light and Dark themes on the same code re-applies
styles without re-parsing — Light↔Dark on a 5k-line file skips the parse
cost. The async wrapper (`rememberHighlightedStringAsync`) does not
cache the tree, so theme changes there re-trigger a full async parse.

## Non-Composable contexts

For non-Composable contexts (e.g. precomputing in a `ViewModel` and
exposing the `AnnotatedString` as state), call `highlight()` directly
off the main thread:

```kotlin
val annotated = withContext(Dispatchers.Default) {
  highlight(code, Languages.Kotlin, theme)
}
```

This is the same primitive `rememberHighlightedStringAsync` uses
internally, exposed for callers that own their own state machine.

## Editing code

For editor-style scenarios where the same source string changes
incrementally (typing, paste, undo), prefer the `:material3-text-field`
module — it packages this pattern as `SyntaxHighlightedTextField` and
`rememberSyntaxHighlightedString` (see "Editing code" in the README).
If you need to wire `IncrementalHighlighter` from `:core` manually
(custom dispatcher, non-Compose state machine, etc.):

```kotlin
val engine = remember(language) { IncrementalHighlighter(language) }
DisposableEffect(engine) { onDispose { engine.close() } }
val annotated by produceState(initialValue = AnnotatedString(code), code, theme) {
  value = withContext(Dispatchers.Default) { engine.update(code, theme) }
}
```

`IncrementalHighlighter` is single-threaded; route every `update` call
through one background dispatcher. Boundary edits (appending a new
top-level declaration, prepending at byte 0) degrade to ≈ baseline cost
by design — interior edits and theme-only re-calls are 40×+ faster than
a full re-highlight at 5k lines on the host JVM. See
[`large_input_profiling.md`](large_input_profiling.md) for per-stage
decomposition and the per-size acceptance numbers.

## What the benchmarks measure

The benchmark targets the `highlight()` function, which is the same
call used internally by `SyntaxHighlightedText`. It covers the full
end-to-end pipeline: tree-sitter parsing, highlight-query matching,
UTF-8 byte-to-char index mapping, and building the final
`AnnotatedString` with `SpanStyle` applied. The returned
`AnnotatedString` is ready to be passed directly to Compose `Text`.

`Language` instances pre-compile the tree-sitter highlights query on
first access, so repeated highlighting of different code snippets with
the same language is fast. The README reports two distinct scenarios
from the perspective of a Compose app:

- **First use [Cold]**: the first time you display a code block with a
  given language. This triggers one-time tree-sitter query compilation
  under the hood, so it is slower.
- **Subsequent use [Warm]**: displaying another code block with the
  same language after the first. The query is already compiled, so
  this reflects the actual per-call cost during normal app usage.

The reported numbers come from a single machine on realistic code
samples (~75–150 lines) and illustrate relative differences between
languages, not absolute guarantees.

## Benchmark environment (full)

Host JVM (per-language Cold/Warm and large-input scaling):

```
OS: Mac OS X (26.4.1)
Arch: aarch64
JVM: OpenJDK 64-Bit Server VM 21.0.11
Processors: 12
Max heap: 2048 MB
Total heap: 2048 MB
JVM args: [-XX:+AlwaysPreTouch, -Xms2g, -Xmx2g, -Dfile.encoding=UTF-8, -ea]
Warmup iterations: 100
Measure iterations: 50
```

Android (large-input scaling, flagship-only):

- Device: Pixel 10 (Tensor G5)
- API / OS: 36 / Android 16
- Harness: `androidx.benchmark.junit4.BenchmarkRule` on Firebase Test Lab
- `BenchmarkRule cpuLocked = true` (FTL applied thermal/freq locking)
- `BenchmarkRule compilationMode = verify` (JIT-warmed, not AOT)

Android measurements are flagship-class only. BenchmarkRule's tight
allocation loop crashes Scudo on lower-spec devices until ktreesitter
exposes explicit native cleanup; `LargeInput5kSmokeTest` covers
on-device functional verification on those devices. See
[`large_input_profiling.md`](large_input_profiling.md#android-device-measurements-firebase-test-lab)
for the full Android methodology and per-stage decomposition.

## See also

- [`large_input_profiling.md`](large_input_profiling.md) — internal
  reference for `:core` highlight-path optimisation: per-stage
  decomposition (parse / Utf8ByteIndex / captures / theme / addStyle),
  run-to-run variance, Android device caveats, and optimisation history.
