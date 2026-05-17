package io.github.mataku.compose.highlight.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.kotlin.Kotlin
import io.github.treesitter.ktreesitter.Parser
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// `parse` decomposition stage benchmarks for Android, kept separate from
// LargeInputAndroidBenchmark because the tight measureRepeated loop allocates a fresh
// Parser/Tree per iteration with zero JVM-heap pressure. ktreesitter's `Parser` and
// `Tree` rely on `java.lang.ref.Cleaner` (GC-driven) for native cleanup and expose no
// explicit `close()` API, so when JVM GC does not fire (because the wrappers themselves
// are tiny) the native AST memory accumulates until Scudo rejects malloc — observed as
// SIGSEGV at libktreesitter.so + 0x24ee0 (allocation NULL-deref inside an internal
// array_push) on Pixel 10 (frankel, API 36) FTL physical runs even at 100-line input.
//
// This class is **intentionally excluded from `--test-targets`** in both
// `.github/workflows/android_ftl_benchmark.yml` and `scripts/run-android-ftl-benchmark.sh`.
// It stays compiled so it can be re-enabled (manually via emulator / connected device)
// once a workaround surfaces — for example, an upstream ktreesitter PR exposing
// `Parser.close()` / `Tree.close()` that lets us deterministically free native handles
// per iteration. The host-JVM `LargeInputBenchmark` covers the parse stage today.
@RunWith(AndroidJUnit4::class)
class LargeInputAndroidParseBenchmark {

  @get:Rule
  val benchmarkRule = BenchmarkRule()

  @Test fun parse_100lines() = parse(BenchmarkSamples.Kotlin)

  @Test fun parse_1kLines() = parse(BenchmarkSamples.KotlinLarge1k)

  private fun parse(code: String) {
    benchmarkRule.measureRepeated {
      val parser = Parser(Languages.Kotlin.parser)
      parser.parse(code)
    }
  }
}
