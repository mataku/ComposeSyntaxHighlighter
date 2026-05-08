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
