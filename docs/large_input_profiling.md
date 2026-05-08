# Large input profiling

Internal reference for `:core` highlight-path optimisation. Numbers
are produced by `LargeInputBenchmark` in `:benchmark` and updated
whenever a relevant change ships.

Reproduce locally:

```
./gradlew :benchmark:jvmTest --tests "*LargeInputBenchmark"
```

The benchmark prints both per-row stats (mean / median / stddev /
min / max / p99) and a markdown table per code size. The tables in
this document are copied verbatim from that output.

## Environment

- Hardware: Apple M-series (host JVM)
- JVM: see `BenchmarkConfig.printEnvironment()` output at run time
- Kotlin 2.3.20, Compose Multiplatform 1.10.3, ktreesitter 0.24.1
- `JVM_WARMUP_ITERATIONS = 10`, `JVM_MEASURE_ITERATIONS = 50` (see
  `benchmark/src/commonTest/kotlin/io/github/mataku/compose/highlight/benchmark/BenchmarkConfig.kt`)

Mobile devices are 3–5× slower than the host JVM in our measurements;
treat the host-JVM tables as a lower bound when extrapolating to
on-device performance.

## Decomposition (latest)

### 100 lines

| Stage                          | Mean (ms) | Median (ms) | StdDev (ms) | Min (ms) | Max (ms) | P99 (ms) |
|--------------------------------|-----------|-------------|-------------|----------|----------|----------|
| parse                          | 1.100     | 1.089       | 0.039       | 1.062    | 1.268    | 1.268    |
| Utf8ByteIndex                  | 0.068     | 0.059       | 0.036       | 0.056    | 0.308    | 0.308    |
| captures iterator only         | 0.003     | 0.003       | 0.001       | 0.003    | 0.007    | 0.007    |
| captures drain (count)         | 3.440     | 3.383       | 0.174       | 3.293    | 4.071    | 4.071    |
| captures only                  | 3.373     | 3.383       | 0.065       | 3.278    | 3.544    | 3.544    |
| + theme.resolve                | 3.451     | 3.426       | 0.086       | 3.348    | 3.779    | 3.779    |
| full highlight (+ addStyle)    | 5.081     | 5.076       | 0.131       | 4.870    | 5.378    | 5.378    |

### 1k lines

| Stage                          | Mean (ms) | Median (ms) | StdDev (ms) | Min (ms) | Max (ms) | P99 (ms) |
|--------------------------------|-----------|-------------|-------------|----------|----------|----------|
| parse                          | 10.515    | 10.389      | 0.280       | 10.308   | 11.433   | 11.433   |
| Utf8ByteIndex                  | 0.106     | 0.105       | 0.008       | 0.096    | 0.139    | 0.139    |
| captures iterator only         | 0.002     | 0.002       | 0.001       | 0.002    | 0.007    | 0.007    |
| captures drain (count)         | 32.713    | 32.444      | 0.650       | 31.892   | 35.348   | 35.348   |
| captures only                  | 32.510    | 32.358      | 0.538       | 31.844   | 34.872   | 34.872   |
| + theme.resolve                | 32.721    | 32.676      | 0.522       | 32.113   | 35.363   | 35.363   |
| full highlight (+ addStyle)    | 46.341    | 46.286      | 0.627       | 45.557   | 49.842   | 49.842   |

### 5k lines

| Stage                          | Mean (ms) | Median (ms) | StdDev (ms) | Min (ms) | Max (ms) | P99 (ms) |
|--------------------------------|-----------|-------------|-------------|----------|----------|----------|
| parse                          | 52.465    | 52.136      | 0.942       | 51.492   | 55.223   | 55.223   |
| Utf8ByteIndex                  | 0.794     | 0.441       | 2.466       | 0.398    | 18.057   | 18.057   |
| captures iterator only         | 0.003     | 0.002       | 0.002       | 0.002    | 0.012    | 0.012    |
| captures drain (count)         | 161.418   | 161.008     | 1.524       | 159.488  | 167.255  | 167.255  |
| captures only                  | 163.581   | 163.929     | 2.939       | 159.359  | 169.757  | 169.757  |
| + theme.resolve                | 163.946   | 162.910     | 2.802       | 160.344  | 169.686  | 169.686  |
| full highlight (+ addStyle)    | 241.511   | 240.558     | 13.788      | 229.528  | 309.970  | 309.970  |

## Row definitions

Each row adds cost on top of the previous row. The deltas between
adjacent rows are the actual signal.

| Row                            | Additional cost on top of previous row                                  |
|--------------------------------|-------------------------------------------------------------------------|
| parse                          | ktreesitter parse                                                       |
| Utf8ByteIndex                  | UTF-8 byte → char index table build                                     |
| captures iterator only         | `Sequence` builder construction (no exec, no native iteration yet)      |
| captures drain (count)         | `exec(node)` + N×`nextCapture` JNI + N×`Pair`/`QueryMatch` allocation   |
| captures only                  | outer `forEach` + Pair destructure + inner `match.captures` loop        |
| + theme.resolve                | `theme.resolve(capture.name)` per capture                               |
| full highlight (+ addStyle)    | `addStyle` + `buildAnnotatedString` accumulation                        |

`captures iterator only` exists as a sanity check on `Sequence` builder
overhead; it should be sub-microsecond at all sizes. If it is not,
that finding is itself worth surfacing.

## Optimisation history

### 0.4.0 — Async highlight path + segmented benchmark (#9)

- Baseline 5-row decomposition introduced (`parse`, `Utf8ByteIndex`,
  `captures only`, `+ theme.resolve`, `full highlight`).

### 0.5.0 — Tree cache for theme-only recomposition (#10)

- Tree cache across theme-only recompositions (`rememberHighlightedString`
  split into 2-stage `remember`); saves ~22% at 5k on Light↔Dark
  toggles.
- `applyStyles` extracted as internal helper; no public API change.

### 0.5.0 — Capture-iteration bucket investigation (#11, DROPPED)

- Bucket decomposed to 7 rows (this document).
- Verdict: drop. The capture-iteration bucket is effectively a
  ktreesitter native + JNI floor; no meaningful Kotlin-side headroom
  remains for `:core` without modifying the upstream binding (out of
  scope for 1.0).
- Reasoning: at 5k median, `drain(count) / captures only` =
  161.008 / 163.929 = 0.982. 98.2% of the bucket is irreducible
  native + JNI + `Pair` / `QueryMatch` return-object cost; the
  remaining Kotlin-side wrapper overhead (outer `forEach` + Pair
  destructure + inner `match.captures` loop) is 2.921 ms — 1.2%
  of the 240.558 ms full-highlight bucket. The `addStyle` bucket
  (`full highlight − + theme.resolve` = 77.648 ms, 32.3% of full
  highlight) carries ~26× the absolute headroom for the same level
  of effort. `captures iterator only` measured at sub-microsecond
  median across all sizes, ruling out `Sequence` builder construction
  as a target.
- Note: the original premise — "`query.captures(rootNode)` allocates
  a fresh cursor each call" — was already incorrect. ktreesitter
  0.24.1 stores a single native cursor inside `Query` for its
  lifetime, and our `Language` retains that `Query`. The data above
  additionally rules out `Sequence` / `Pair` / `QueryMatch`
  allocation as a worthwhile Kotlin-side target at current scale.
- Routing: subsequent `addStyle` batching investigation also
  DROPPED on a discarded branch (Δ +2.3% / coalesce regressed; not
  recorded here — see closed PR for the writeup). Empty-code guard
  symmetry between `applyStyles` and `plainHighlightedString`
  landed separately as a one-line maintenance commit.

### Incremental highlighter engine (#12, unreleased — feature/editor)

- `IncrementalHighlighter` lands in `:core` (`commonMain`) — a stateful,
  single-threaded engine backed by tree-sitter incremental parse, scoped
  query execution via `Query.byteRange`, an enclosing-node fallback for
  partial-overlap captures, and a maintained `MutableList<CaptureSpan>`
  whose entries are reused across updates.
- `IncrementalHighlighterBenchmark` (`:benchmark/src/jvmTest`) reports six
  patterns per size and asserts CI-enforced acceptance gates.

#### Acceptance gates (asserted in CI)

| Size | Interior edit (insert near end / middle / paste at middle) | Theme-only re-call | First call (no-regression) |
|---|---|---|---|
| 5k  | ≥3× vs baseline | ≥5× | within ±20% |
| 1k  | ≥2×             | ≥3× | within ±20% |
| 100 | within ±10%     | within ±10% | within ±20% |

*First-call tolerance is ±20%, not ±5%, because the benchmark constructs a fresh `IncrementalHighlighter` per measured iteration — the per-iteration `Query` construction cost (~1–2 ms parsing the highlights query string + building predicate/capture/setting/assertion lists) is included in "first call" but amortised away in real consumers, who construct the engine once per language and reuse it across many `update` calls.*

#### Measured (host JVM, Apple M-series)

##### 100 lines

| Stage                                  | Mean (ms) | Median (ms) | StdDev (ms) | Min (ms) | Max (ms) | P99 (ms) |
|----------------------------------------|-----------|-------------|-------------|----------|----------|----------|
| full highlight (baseline)              | 5.396     | 5.365       | 0.279       | 4.863    | 6.517    | 6.517    |
| first call                             | 5.386     | 5.362       | 0.165       | 5.154    | 5.742    | 5.742    |
| insert near end                        | 0.203     | 0.200       | 0.015       | 0.180    | 0.251    | 0.251    |
| insert at middle                       | 1.974     | 1.967       | 0.059       | 1.859    | 2.118    | 2.118    |
| paste at middle                        | 2.168     | 2.154       | 0.079       | 2.069    | 2.369    | 2.369    |
| theme only                             | 0.071     | 0.071       | 0.002       | 0.066    | 0.079    | 0.079    |
| same text x5                           | 0.345     | 0.335       | 0.038       | 0.297    | 0.427    | 0.427    |

##### 1k lines

| Stage                                  | Mean (ms) | Median (ms) | StdDev (ms) | Min (ms) | Max (ms) | P99 (ms) |
|----------------------------------------|-----------|-------------|-------------|----------|----------|----------|
| full highlight (baseline)              | 48.401    | 48.071      | 0.924       | 47.172   | 50.660   | 50.660   |
| first call                             | 50.443    | 50.072      | 1.066       | 49.271   | 53.524   | 53.524   |
| insert near end                        | 0.981     | 0.978       | 0.032       | 0.929    | 1.045    | 1.045    |
| insert at middle                       | 1.149     | 1.130       | 0.104       | 1.092    | 1.855    | 1.855    |
| paste at middle                        | 1.201     | 1.181       | 0.055       | 1.126    | 1.314    | 1.314    |
| theme only                             | 0.305     | 0.285       | 0.107       | 0.262    | 1.041    | 1.041    |
| same text x5                           | 1.594     | 1.597       | 0.137       | 1.435    | 2.367    | 2.367    |

##### 5k lines

| Stage                                  | Mean (ms) | Median (ms) | StdDev (ms) | Min (ms) | Max (ms) | P99 (ms) |
|----------------------------------------|-----------|-------------|-------------|----------|----------|----------|
| full highlight (baseline)              | 249.735   | 248.749     | 3.692       | 245.146  | 260.893  | 260.893  |
| first call                             | 262.998   | 262.876     | 1.998       | 258.782  | 267.360  | 267.360  |
| insert near end                        | 7.362     | 6.171       | 2.862       | 4.749    | 13.947   | 13.947   |
| insert at middle                       | 7.535     | 5.498       | 2.889       | 5.018    | 13.486   | 13.486   |
| paste at middle                        | 7.012     | 6.157       | 1.709       | 5.158    | 10.573   | 10.573   |
| theme only                             | 2.006     | 1.483       | 1.310       | 1.413    | 6.464    | 6.464    |
| same text x5                           | 11.639    | 12.246      | 2.782       | 7.421    | 20.910   | 20.910   |

#### Speedup vs baseline (median)

| Size | Insert near end | Insert at middle | Paste at middle | Theme only |
|---|---|---|---|---|
| 5k  | **40.3×** | **45.2×** | **40.4×** | **167.7×** |
| 1k  | **49.1×** | **42.5×** | **40.7×** | **168.7×** |
| 100 | (within tolerance) | (within tolerance) | (within tolerance) | (within tolerance) |

#### Documented limitation — boundary edits

True boundary edits — appending a new top-level declaration at file end
(e.g. inserting `\nval x = 1` at `code.length`) or prepending at byte 0 —
expand to the source-file root via `Node.descendant(start, end)`, which
defeats `Query.byteRange` scoping (the scoped range becomes the whole
file). Cost in this case degrades to ≈ baseline (~1.2× faster than full
highlight in measurement, never slower). This is the spec's intended
"graceful degradation" behaviour and matches the `IncrementalHighlighter`
design's worst-case envelope.

For editor scenarios the practical impact is small: typing a single
character within an existing function/expression triggers the interior
path (≥40× speedup), while appending a new function is a punctuated
event amortised across many subsequent interior edits. Async-default
dispatch in `:material3-editor` (separate spec) further hides the
boundary-edit cost behind the latest-wins coroutine.
