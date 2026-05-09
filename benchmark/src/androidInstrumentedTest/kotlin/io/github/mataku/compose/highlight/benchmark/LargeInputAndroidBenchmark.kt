package io.github.mataku.compose.highlight.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.Utf8ByteIndex
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.kotlin.Kotlin
import io.github.treesitter.ktreesitter.Parser
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LargeInputAndroidBenchmark {

  @get:Rule
  val benchmarkRule = BenchmarkRule()

  // --- 100 lines ---

  @Test fun utf8ByteIndex_100lines() = utf8ByteIndex(BenchmarkSamples.Kotlin)

  @Test fun capturesIteratorOnly_100lines() = capturesIteratorOnly(BenchmarkSamples.Kotlin)

  @Test fun capturesDrainCount_100lines() = capturesDrainCount(BenchmarkSamples.Kotlin)

  @Test fun capturesOnly_100lines() = capturesOnly(BenchmarkSamples.Kotlin)

  @Test fun capturesPlusThemeResolve_100lines() = capturesPlusThemeResolve(BenchmarkSamples.Kotlin)

  @Test fun fullHighlight_100lines() = fullHighlight(BenchmarkSamples.Kotlin)

  // --- 1k lines ---

  @Test fun utf8ByteIndex_1kLines() = utf8ByteIndex(BenchmarkSamples.KotlinLarge1k)

  @Test fun capturesIteratorOnly_1kLines() = capturesIteratorOnly(BenchmarkSamples.KotlinLarge1k)

  @Test fun capturesDrainCount_1kLines() = capturesDrainCount(BenchmarkSamples.KotlinLarge1k)

  @Test fun capturesOnly_1kLines() = capturesOnly(BenchmarkSamples.KotlinLarge1k)

  @Test fun capturesPlusThemeResolve_1kLines() = capturesPlusThemeResolve(BenchmarkSamples.KotlinLarge1k)

  @Test fun fullHighlight_1kLines() = fullHighlight(BenchmarkSamples.KotlinLarge1k)

  // 5k-line variants are intentionally omitted on Android. In a tight measureRepeated loop
  // the per-iteration Parser/Tree wrappers stay below JVM heap-pressure thresholds, so
  // ktreesitter's Cleaner-based native cleanup never fires and Scudo eventually rejects
  // malloc on physical devices. 5k is treated as an outlier for mobile and stays
  // host-JVM only (see :benchmark jvmTest LargeInputBenchmark).
  //
  // The `parse` decomposition stage is split out into LargeInputAndroidParseBenchmark
  // (excluded from FTL --test-targets) because its tight loop allocates Parser/Tree per
  // iteration with zero JVM-heap pressure, hitting the same Cleaner / Scudo limit even at
  // 100 lines once enough iterations accumulate. Kept in code for future Android tuning
  // (e.g. once ktreesitter exposes Parser.close() / Tree.close()).

  // --- stage helpers ---

  private fun utf8ByteIndex(code: String) {
    benchmarkRule.measureRepeated {
      Utf8ByteIndex(code)
    }
  }

  private fun capturesIteratorOnly(code: String) {
    val parser = Parser(Languages.Kotlin.parser)
    val tree = parser.parse(code)
    val query = Languages.Kotlin.query
    benchmarkRule.measureRepeated {
      query.captures(tree.rootNode).iterator()
    }
  }

  private fun capturesDrainCount(code: String) {
    val parser = Parser(Languages.Kotlin.parser)
    val tree = parser.parse(code)
    val query = Languages.Kotlin.query
    benchmarkRule.measureRepeated {
      query.captures(tree.rootNode).count()
    }
  }

  private fun capturesOnly(code: String) {
    val parser = Parser(Languages.Kotlin.parser)
    val tree = parser.parse(code)
    val query = Languages.Kotlin.query
    benchmarkRule.measureRepeated {
      query.captures(tree.rootNode).forEach { (_, match) ->
        match.captures.forEach { _ -> }
      }
    }
  }

  private fun capturesPlusThemeResolve(code: String) {
    val parser = Parser(Languages.Kotlin.parser)
    val tree = parser.parse(code)
    val query = Languages.Kotlin.query
    benchmarkRule.measureRepeated {
      query.captures(tree.rootNode).forEach { (_, match) ->
        match.captures.forEach { capture ->
          BenchmarkConfig.theme.resolve(capture.name)
        }
      }
    }
  }

  private fun fullHighlight(code: String) {
    benchmarkRule.measureRepeated {
      highlight(code, Languages.Kotlin, BenchmarkConfig.theme)
    }
  }
}
