package io.github.mataku.compose.highlight.benchmark

import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.Utf8ByteIndex
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.kotlin.Kotlin
import io.github.treesitter.ktreesitter.Parser
import kotlin.system.measureNanoTime
import kotlin.test.Test

class LargeInputBenchmark {

  @Test
  fun runAll() {
    BenchmarkConfig.printEnvironment()
    val cases = listOf(
      "100 lines" to BenchmarkSamples.Kotlin,
      "1k lines" to BenchmarkSamples.KotlinLarge1k,
      "5k lines" to BenchmarkSamples.KotlinLarge5k,
    )

    cases.forEach { (label, code) ->
      println()
      println("=== $label (${code.length} chars / ${code.lines().size} lines) ===")

      val results = mutableListOf<BenchmarkConfig.BenchmarkResult>()

      results += run("$label / parse") {
        val parser = Parser(Languages.Kotlin.parser)
        parser.parse(code)
      }

      results += run("$label / Utf8ByteIndex") {
        Utf8ByteIndex(code)
      }

      val parser = Parser(Languages.Kotlin.parser)
      val tree = parser.parse(code)
      val query = Languages.Kotlin.query
      results += run("$label / capture iteration") {
        query.captures(tree.rootNode).forEach { (_, match) ->
          match.captures.forEach { capture ->
            BenchmarkConfig.theme.resolve(capture.name)
          }
        }
      }

      results += run("$label / full highlight") {
        highlight(code, Languages.Kotlin, BenchmarkConfig.theme)
      }

      BenchmarkConfig.printMarkdown(results)
    }
  }

  private fun run(name: String, block: () -> Unit): BenchmarkConfig.BenchmarkResult {
    repeat(BenchmarkConfig.JVM_WARMUP_ITERATIONS) { block() }
    val times = List(BenchmarkConfig.JVM_MEASURE_ITERATIONS) {
      measureNanoTime { block() }
    }
    return BenchmarkConfig.reportStatistics(name, times)
  }
}
