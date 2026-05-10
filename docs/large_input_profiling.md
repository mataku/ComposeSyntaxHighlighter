# Large input profiling

Internal reference for `:core` highlight-path optimisation. Numbers
are produced by `LargeInputBenchmark` in `:benchmark` and updated
whenever a relevant change ships.

## Environment

| Field | Value |
| --- | --- |
| Hardware | Apple M3 Pro |
| Cores | 12 (12 physical / 12 logical) |
| RAM | 36 GB |
| OS | macOS 26.4.1 (build 25E253) |
| JDK | OpenJDK 21.0.11 (Homebrew) |
| JVM args | `-Xms2g -Xmx2g -XX:+AlwaysPreTouch` |
| `JVM_WARMUP_ITERATIONS` | 100 |
| `JVM_MEASURE_ITERATIONS` | 50 |
| Run date | 2026-05-10 |
| Source | `BenchmarkConfig.printEnvironment()` output at run time |

On-device measurements live in [_Android device measurements (Firebase Test Lab)_](#android-device-measurements-firebase-test-lab) below. The Android section is flagship-class only and intentionally does not extrapolate older-device multipliers; host-JVM and flagship-Android microbenchmark numbers are not faithful proxies for mid-range Android single-shot latency.

## Decomposition (latest)

### 100 lines

| Stage | Min (ms) | Median (ms) | Max (ms) |
|-------|---------:|------------:|---------:|
| parse                          | 1.122 | 1.130 | 1.154 |
| Utf8ByteIndex                  | 0.014 | 0.014 | 0.017 |
| captures iterator only         | 0.002 | 0.002 | 0.008 |
| captures drain (count)         | 3.338 | 3.425 | 3.521 |
| captures only                  | 3.339 | 3.481 | 3.501 |
| + theme.resolve                | 3.356 | 3.502 | 3.552 |
| full highlight (+ addStyle)    | 4.834 | 4.969 | 5.073 |

#### Run-to-run variance band

| Stage | Run 1 median (ms) | Run 2 median (ms) | Run 3 median (ms) | Spread % |
|-------|------------------:|------------------:|------------------:|---------:|
| parse                          | 1.130 | 1.136 | 1.130 | 0.5 |
| Utf8ByteIndex                  | 0.015 | 0.015 | 0.014 | 6.7 |
| captures iterator only         | 0.002 | 0.002 | 0.002 | 0.0 |
| captures drain (count)         | 3.498 | 3.525 | 3.425 | 2.9 |
| captures only                  | 3.403 | 3.538 | 3.481 | 3.9 |
| + theme.resolve                | 3.513 | 3.563 | 3.502 | 1.7 |
| full highlight (+ addStyle)    | 5.009 | 5.080 | 4.969 | 2.2 |

### 1k lines

| Stage | Min (ms) | Median (ms) | Max (ms) |
|-------|---------:|------------:|---------:|
| parse                          | 10.940 | 10.988 | 11.038 |
| Utf8ByteIndex                  | 0.138  | 0.144  | 0.158  |
| captures iterator only         | 0.002  | 0.002  | 0.010  |
| captures drain (count)         | 33.295 | 33.451 | 47.458 |
| captures only                  | 33.178 | 33.308 | 33.969 |
| + theme.resolve                | 33.956 | 34.091 | 35.215 |
| full highlight (+ addStyle)    | 48.610 | 48.753 | 50.467 |

#### Run-to-run variance band

| Stage | Run 1 median (ms) | Run 2 median (ms) | Run 3 median (ms) | Spread % |
|-------|------------------:|------------------:|------------------:|---------:|
| parse                          | 11.016 | 10.973 | 10.988 | 0.4 |
| Utf8ByteIndex                  | 0.141  | 0.141  | 0.144  | 2.1 |
| captures iterator only         | 0.002  | 0.002  | 0.002  | 0.0 |
| captures drain (count)         | 33.269 | 33.821 | 33.451 | 1.7 |
| captures only                  | 34.134 | 34.413 | 33.308 | 3.2 |
| + theme.resolve                | 34.488 | 34.274 | 34.091 | 1.2 |
| full highlight (+ addStyle)    | 47.271 | 49.370 | 48.753 | 4.3 |

### 5k lines

| Stage | Min (ms) | Median (ms) | Max (ms) |
|-------|---------:|------------:|---------:|
| parse                          | 54.849  | 55.015  | 56.019  |
| Utf8ByteIndex                  | 0.747   | 0.758   | 0.780   |
| captures iterator only         | 0.002   | 0.002   | 0.002   |
| captures drain (count)         | 166.398 | 167.060 | 171.225 |
| captures only                  | 166.196 | 170.418 | 172.873 |
| + theme.resolve                | 167.421 | 172.395 | 187.267 |
| full highlight (+ addStyle)    | 238.607 | 239.822 | 266.277 |

#### Run-to-run variance band

| Stage | Run 1 median (ms) | Run 2 median (ms) | Run 3 median (ms) | Spread % |
|-------|------------------:|------------------:|------------------:|---------:|
| parse                          | 54.709  | 54.989  | 55.015  | 0.6 |
| Utf8ByteIndex                  | 0.738   | 0.744   | 0.758   | 2.7 |
| captures iterator only         | 0.002   | 0.002   | 0.002   | 0.0 |
| captures drain (count)         | 165.377 | 167.358 | 167.060 | 1.2 |
| captures only                  | 170.402 | 168.273 | 170.418 | 1.3 |
| + theme.resolve                | 168.090 | 174.048 | 172.395 | 3.5 |
| full highlight (+ addStyle)    | 244.153 | 243.121 | 239.822 | 1.8 |

## Android device measurements (Firebase Test Lab)

Captured by `LargeInputAndroidBenchmark` and `IncrementalHighlighterAndroidBenchmark`
running on Firebase Test Lab against a physical Pixel 10. **Flagship-class only**
— see "Why no lower-spec device" below.

### Device

| Field | Value |
| --- | --- |
| Model / Device ID | Pixel 10 / `frankel` |
| SoC | Tensor G5, 8 cores @ 3.78 GHz |
| RAM (visible / nominal) | 12.1 GB / 16 GB |
| API / OS | 36 / Android 16 (REL) |
| Build fingerprint | `google/frankel/frankel:16/BD1A.250702.001/13724644:user/release-keys` |
| BenchmarkRule cpuLocked | `true` (FTL applied thermal/freq locking) |
| BenchmarkRule compilationMode | `verify` (default — JIT-warmed, not AOT speedProfile) |
| Run date | 2026-05-10 |
| Source raw JSON | [`docs/references/2026-05-10-android-ftl-pixel10-benchmarkData.json`](references/2026-05-10-android-ftl-pixel10-benchmarkData.json) |

`androidx.benchmark.junit4.BenchmarkRule.measureRepeated` reports `min`,
`median`, `max` per test — the same Min/Median/Max surface as the
host-JVM tables above, although the host harness is custom.

### Decomposition

#### 100 lines

| Stage                       | Min (ms) | Median (ms) | Max (ms) |
|-----------------------------|---------:|------------:|---------:|
| Utf8ByteIndex               | 0.046    | 0.049       | 0.076    |
| captures iterator only      | 0.000    | 0.000       | 0.001    |
| captures drain (count)      | 1.437    | 2.058       | 2.251    |
| captures only               | 1.946    | 2.061       | 3.328    |
| + theme.resolve             | 1.832    | 2.048       | 3.534    |
| full highlight (+ addStyle) | 2.877    | 3.142       | 4.031    |

#### 1k lines

| Stage                       | Min (ms) | Median (ms) | Max (ms) |
|-----------------------------|---------:|------------:|---------:|
| Utf8ByteIndex               | 0.522    | 0.601       | 1.203    |
| captures iterator only      | 0.000    | 0.000       | 0.001    |
| captures drain (count)      | 19.189   | 20.372      | 31.023   |
| captures only               | 18.855   | 20.273      | 37.077   |
| + theme.resolve             | 15.287   | 18.036      | 24.480   |
| full highlight (+ addStyle) | 27.573   | 30.349      | 38.314   |

`parse` is intentionally excluded — its tight per-iteration `Parser`/`Tree`
allocation pattern outpaces ktreesitter's GC-driven `Cleaner` cleanup and
hits Scudo OOM at 100 lines on Android devices. The standalone
`LargeInputAndroidParseBenchmark` (kept compiled but excluded from FTL
`--test-targets`) preserves the methodology for local re-enablement once
ktreesitter exposes explicit `Parser.close()` / `Tree.close()`. 5k-line
variants are excluded for the same reason; `LargeInput5kSmokeTest`
(one-shot, non-benchmark) verifies that 5k inputs highlight correctly
on-device without crashing.

### Incremental highlighter

#### 100 lines

| Stage                     | Min (ms) | Median (ms) | Max (ms) |
|---------------------------|---------:|------------:|---------:|
| full highlight (baseline) | 2.952    | 3.131       | 4.666    |
| first call                | 3.207    | 3.317       | 7.380    |
| insert near end           | 0.223    | 0.241       | 0.714    |
| insert at middle          | 1.099    | 1.174       | 1.885    |
| paste at middle           | 1.204    | 1.338       | 3.177    |
| theme only                | 0.084    | 0.089       | 0.271    |
| same text x5              | 0.474    | 0.544       | 0.967    |

#### 1k lines

| Stage                     | Min (ms) | Median (ms) | Max (ms) |
|---------------------------|---------:|------------:|---------:|
| full highlight (baseline) | 28.872   | 30.491      | 47.713   |
| first call                | 28.877   | 31.447      | 68.098   |
| insert near end           | 2.266    | 2.609       | 7.728    |
| insert at middle          | 3.778    | 4.696       | 8.240    |
| paste at middle           | 2.631    | 2.781       | 18.752   |
| theme only                | 0.893    | 0.997       | 4.121    |
| same text x5              | 5.024    | 5.694       | 10.832   |

#### Speedup vs baseline (median, 1k lines)

| Insert near end | Insert at middle | Paste at middle | Theme only |
|----------------:|-----------------:|----------------:|-----------:|
| **11.7×**       | **6.5×**         | **11.0×**       | **30.6×**  |

The 1k Pixel 10 envelope sits well clear of the host-JVM acceptance
gates (≥2× / ≥3×) — `theme only` clears the 1k gate by an order of
magnitude, and the interior-edit class clears the 2× gate by 3-6×.
100-line numbers stay within the small-input tolerance bands defined
in the host-JVM section.

### Why no lower-spec device

The original plan paired Pixel 10 with an older device for an old / new
spread. Two physical devices were tried in succession; both flaked on
the FTL run before the suite completed:

- **Pixel 6a** (`bluejay`, Tensor G1, 6 GB RAM, API 32) — `insertAtMiddle_100lines`
  consistently hit Scudo OOM
- **Pixel 8a** (`akita`, Tensor G3, 8 GB RAM, API 34) — same class of
  failure on a different test method

The structural cause is ktreesitter 0.24.1's lack of explicit
`Parser.close()` / `Tree.close()` — native cleanup is GC-driven via
`Cleaner`, and `BenchmarkRule.measureRepeated`'s tight loop generates
wrappers faster than the JVM heap pressure threshold needed to trigger
cleanup. **Real-world consumers do not hit this** (one keystroke per
several hundred milliseconds, with surrounding GC pressure from app
state). Lower-spec on-device functional verification is provided
separately by `LargeInput5kSmokeTest`.

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
  167.060 / 170.418 = 0.980. 98.0% of the bucket is irreducible
  native + JNI + `Pair` / `QueryMatch` return-object cost; the
  remaining Kotlin-side wrapper overhead (outer `forEach` + Pair
  destructure + inner `match.captures` loop) is 3.358 ms — 1.4%
  of the 239.822 ms full-highlight bucket. The `addStyle` bucket
  (`full highlight − + theme.resolve` = 67.427 ms, 28.1% of full
  highlight) carries ~20× the absolute headroom for the same level
  of effort. `captures iterator only` measured at sub-microsecond
  median across all sizes, ruling out `Sequence` builder construction
  as a target. See `## Decomposition (latest)` — 5k lines for the
  underlying medians.
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

#### Measured (host JVM, Apple M3 Pro)

##### 100 lines

| Stage | Min (ms) | Median (ms) | Max (ms) |
|-------|---------:|------------:|---------:|
| full highlight (baseline)      | 4.827 | 4.908 | 4.985 |
| first call                     | 5.152 | 5.220 | 5.492 |
| insert near end                | 0.115 | 0.139 | 0.150 |
| insert at middle               | 1.938 | 1.967 | 1.998 |
| paste at middle                | 2.019 | 2.143 | 2.177 |
| theme only                     | 0.033 | 0.035 | 0.037 |
| same text x5                   | 0.172 | 0.178 | 0.202 |

###### Run-to-run variance band

| Stage | Run 1 median (ms) | Run 2 median (ms) | Run 3 median (ms) | Spread % |
|-------|------------------:|------------------:|------------------:|---------:|
| full highlight (baseline)      | 4.754 | 4.949 | 4.908 | 4.0  |
| first call                     | 4.970 | 5.191 | 5.220 | 4.8  |
| insert near end                | 0.137 | 0.118 | 0.139 | 15.3 |
| insert at middle               | 1.893 | 1.944 | 1.967 | 3.8  |
| paste at middle                | 2.176 | 2.160 | 2.143 | 1.5  |
| theme only                     | 0.035 | 0.035 | 0.035 | 0.0  |
| same text x5                   | 0.178 | 0.177 | 0.178 | 0.6  |

##### 1k lines

| Stage | Min (ms) | Median (ms) | Max (ms) |
|-------|---------:|------------:|---------:|
| full highlight (baseline)      | 47.758 | 48.312 | 50.999 |
| first call                     | 50.205 | 50.306 | 52.326 |
| insert near end                | 0.938  | 0.963  | 2.549  |
| insert at middle               | 1.142  | 1.162  | 1.207  |
| paste at middle                | 1.104  | 1.121  | 1.170  |
| theme only                     | 0.321  | 0.329  | 0.345  |
| same text x5                   | 1.503  | 1.645  | 3.179  |

###### Run-to-run variance band

| Stage | Run 1 median (ms) | Run 2 median (ms) | Run 3 median (ms) | Spread % |
|-------|------------------:|------------------:|------------------:|---------:|
| full highlight (baseline)      | 48.846 | 48.068 | 48.312 | 1.6 |
| first call                     | 50.401 | 49.802 | 50.306 | 1.2 |
| insert near end                | 0.963  | 0.968  | 0.963  | 0.5 |
| insert at middle               | 1.155  | 1.163  | 1.162  | 0.7 |
| paste at middle                | 1.116  | 1.122  | 1.121  | 0.5 |
| theme only                     | 0.326  | 0.325  | 0.329  | 1.2 |
| same text x5                   | 1.640  | 1.645  | 1.645  | 0.3 |

##### 5k lines

| Stage | Min (ms) | Median (ms) | Max (ms) |
|-------|---------:|------------:|---------:|
| full highlight (baseline)      | 238.458 | 239.619 | 266.878 |
| first call                     | 246.181 | 248.092 | 306.080 |
| insert near end                | 4.727   | 4.764   | 98.070  |
| insert at middle               | 4.999   | 5.033   | 67.005  |
| paste at middle                | 5.266   | 5.338   | 21.932  |
| theme only                     | 1.493   | 1.513   | 66.275  |
| same text x5                   | 7.581   | 7.650   | 77.613  |

###### Run-to-run variance band

| Stage | Run 1 median (ms) | Run 2 median (ms) | Run 3 median (ms) | Spread % |
|-------|------------------:|------------------:|------------------:|---------:|
| full highlight (baseline)      | 234.170 | 243.301 | 239.619 | 3.8 |
| first call                     | 245.277 | 249.929 | 248.092 | 1.9 |
| insert near end                | 4.750   | 4.773   | 4.764   | 0.5 |
| insert at middle               | 5.037   | 5.053   | 5.033   | 0.4 |
| paste at middle                | 5.312   | 5.321   | 5.338   | 0.5 |
| theme only                     | 1.510   | 1.521   | 1.513   | 0.7 |
| same text x5                   | 7.652   | 7.685   | 7.650   | 0.5 |

#### Speedup vs baseline (median)

| Size | Insert near end | Insert at middle | Paste at middle | Theme only |
|---|---|---|---|---|
| 5k  | **50.3×** | **47.6×** | **44.9×** | **158.4×** |
| 1k  | **50.2×** | **41.6×** | **43.1×** | **146.8×** |
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
