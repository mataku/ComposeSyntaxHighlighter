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
| parse                          | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| Utf8ByteIndex                  | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| captures iterator only         | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| captures drain (count)         | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| captures only                  | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| + theme.resolve                | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| full highlight (+ addStyle)    | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |

### 1k lines

| Stage                          | Mean (ms) | Median (ms) | StdDev (ms) | Min (ms) | Max (ms) | P99 (ms) |
|--------------------------------|-----------|-------------|-------------|----------|----------|----------|
| parse                          | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| Utf8ByteIndex                  | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| captures iterator only         | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| captures drain (count)         | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| captures only                  | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| + theme.resolve                | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| full highlight (+ addStyle)    | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |

### 5k lines

| Stage                          | Mean (ms) | Median (ms) | StdDev (ms) | Min (ms) | Max (ms) | P99 (ms) |
|--------------------------------|-----------|-------------|-------------|----------|----------|----------|
| parse                          | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| Utf8ByteIndex                  | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| captures iterator only         | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| captures drain (count)         | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| captures only                  | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| + theme.resolve                | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |
| full highlight (+ addStyle)    | TBD       | TBD         | TBD         | TBD      | TBD      | TBD      |

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

### 0.4.0 (large-input static path)

- Baseline 5-row decomposition introduced (`parse`, `Utf8ByteIndex`,
  `captures only`, `+ theme.resolve`, `full highlight`).

### 0.5.0 (9b-1)

- Tree cache across theme-only recompositions (`rememberHighlightedString`
  split into 2-stage `remember`); saves ~22% at 5k on Light↔Dark
  toggles.
- `applyStyles` extracted as internal helper; no public API change.

### 0.5.0 (9b-1.5 #2)

- Bucket decomposed to 7 rows (this document).
- Verdict: TBD (filled in by the maintainer after reading the numbers).
