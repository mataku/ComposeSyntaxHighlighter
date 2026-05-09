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

  @Test fun parse_100lines() = parse(BenchmarkSamples.Kotlin)

  @Test fun utf8ByteIndex_100lines() = utf8ByteIndex(BenchmarkSamples.Kotlin)

  @Test fun capturesIteratorOnly_100lines() = capturesIteratorOnly(BenchmarkSamples.Kotlin)

  @Test fun capturesDrainCount_100lines() = capturesDrainCount(BenchmarkSamples.Kotlin)

  @Test fun capturesOnly_100lines() = capturesOnly(BenchmarkSamples.Kotlin)

  @Test fun capturesPlusThemeResolve_100lines() = capturesPlusThemeResolve(BenchmarkSamples.Kotlin)

  @Test fun fullHighlight_100lines() = fullHighlight(BenchmarkSamples.Kotlin)

  // --- 1k lines ---

  @Test fun parse_1kLines() = parse(BenchmarkSamples.KotlinLarge1k)

  @Test fun utf8ByteIndex_1kLines() = utf8ByteIndex(BenchmarkSamples.KotlinLarge1k)

  @Test fun capturesIteratorOnly_1kLines() = capturesIteratorOnly(BenchmarkSamples.KotlinLarge1k)

  @Test fun capturesDrainCount_1kLines() = capturesDrainCount(BenchmarkSamples.KotlinLarge1k)

  @Test fun capturesOnly_1kLines() = capturesOnly(BenchmarkSamples.KotlinLarge1k)

  @Test fun capturesPlusThemeResolve_1kLines() = capturesPlusThemeResolve(BenchmarkSamples.KotlinLarge1k)

  @Test fun fullHighlight_1kLines() = fullHighlight(BenchmarkSamples.KotlinLarge1k)

  // --- 5k lines ---

  @Test fun parse_5kLines() = parse(BenchmarkSamples.KotlinLarge5k)

  @Test fun utf8ByteIndex_5kLines() = utf8ByteIndex(BenchmarkSamples.KotlinLarge5k)

  @Test fun capturesIteratorOnly_5kLines() = capturesIteratorOnly(BenchmarkSamples.KotlinLarge5k)

  @Test fun capturesDrainCount_5kLines() = capturesDrainCount(BenchmarkSamples.KotlinLarge5k)

  @Test fun capturesOnly_5kLines() = capturesOnly(BenchmarkSamples.KotlinLarge5k)

  @Test fun capturesPlusThemeResolve_5kLines() = capturesPlusThemeResolve(BenchmarkSamples.KotlinLarge5k)

  @Test fun fullHighlight_5kLines() = fullHighlight(BenchmarkSamples.KotlinLarge5k)

  // --- stage helpers ---

  private fun parse(code: String) {
    benchmarkRule.measureRepeated {
      val parser = Parser(Languages.Kotlin.parser)
      parser.parse(code)
    }
  }

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
