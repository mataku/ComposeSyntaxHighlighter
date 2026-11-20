package io.github.mataku.compose.highlight.benchmark

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.kotlin.Kotlin
import kotlin.system.measureNanoTime
import kotlin.test.Test

class TextLayoutBenchmark {

  @Test
  fun runAll() {
    BenchmarkConfig.printEnvironment()
    // cacheSize = 0: the measurer would otherwise return the cached layout of the previous iteration.
    val measurer = TextMeasurer(
      defaultFontFamilyResolver = createFontFamilyResolver(),
      defaultDensity = Density(1f),
      defaultLayoutDirection = LayoutDirection.Ltr,
      cacheSize = 0,
    )
    val style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp)
    val cases = listOf(
      "100 lines" to BenchmarkSamples.Kotlin,
      "1k lines" to BenchmarkSamples.KotlinLarge1k,
      "5k lines" to BenchmarkSamples.KotlinLarge5k,
    )

    cases.forEach { (label, code) ->
      val plain = AnnotatedString(code)
      val highlighted = highlight(code, Languages.Kotlin, BenchmarkConfig.theme)
      println()
      println("=== $label (${code.length} chars / ${code.lines().size} lines / ${highlighted.spanStyles.size} spans) ===")

      val results = listOf(
        run("$label / layout plain text") { measurer.measure(plain, style) },
        run("$label / layout highlighted text") { measurer.measure(highlighted, style) },
      )

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
