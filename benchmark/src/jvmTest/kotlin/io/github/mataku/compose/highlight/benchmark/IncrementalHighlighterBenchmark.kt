package io.github.mataku.compose.highlight.benchmark

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.IncrementalHighlighter
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.kotlin.Kotlin
import kotlin.system.measureNanoTime
import kotlin.test.Test
import kotlin.test.assertTrue

class IncrementalHighlighterBenchmark {

  private val themeA: SyntaxTheme = BenchmarkConfig.theme
  private val themeB: SyntaxTheme = SyntaxTheme(
    baseStyle = SpanStyle(color = Color(0xFF202020)),
    keyword = SpanStyle(color = Color(0xFF8800FF)),
    string = SpanStyle(color = Color(0xFF008888)),
    comment = SpanStyle(color = Color(0xFF888888)),
  )

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

      val baseline = run("$label / full highlight (baseline)") {
        highlight(code, Languages.Kotlin, themeA)
      }
      results += baseline

      results += measureFirstCall(label, code)
      results += measureInsertNearEnd(label, code)
      results += measureInsertAtMiddle(label, code)
      results += measurePasteAtMiddle(label, code)
      results += measureThemeOnly(label, code)
      val sameText = measureSameTextRepeated(label, code)
      results += sameText

      BenchmarkConfig.printMarkdown(results)

      assertGates(label, baseline, results)
    }
  }

  /**
   * Measures [update] cost with a whole-file replacement edit (lines shuffled),
   * which forces tree-sitter to re-parse nearly the entire file — a cost profile
   * equivalent to [highlight]. Engine construction is outside the timed block.
   */
  private fun measureFirstCall(label: String, code: String): BenchmarkConfig.BenchmarkResult {
    val shuffledCode = code.lines().reversed().joinToString("\n")
    val engine = IncrementalHighlighter(Languages.Kotlin)
    try {
      repeat(BenchmarkConfig.JVM_WARMUP_ITERATIONS) {
        engine.update(code, themeA)
        engine.update(shuffledCode, themeA)
      }
      val times = List(BenchmarkConfig.JVM_MEASURE_ITERATIONS) {
        engine.update(shuffledCode, themeA)
        measureNanoTime { engine.update(code, themeA) }
      }
      return BenchmarkConfig.reportStatistics("$label / first call", times)
    } finally {
      engine.close()
    }
  }

  private fun measureInsertNearEnd(label: String, code: String): BenchmarkConfig.BenchmarkResult {
    val cut = (code.length - 5).coerceAtLeast(code.length / 2)
    val edited = code.substring(0, cut) + " " + code.substring(cut)
    return measureSecondUpdate("$label / insert near end", code, edited)
  }

  private fun measureInsertAtMiddle(label: String, code: String): BenchmarkConfig.BenchmarkResult {
    val mid = code.length / 2
    val edited = code.substring(0, mid) + " " + code.substring(mid)
    return measureSecondUpdate("$label / insert at middle", code, edited)
  }

  private fun measurePasteAtMiddle(label: String, code: String): BenchmarkConfig.BenchmarkResult {
    val mid = code.length / 2
    val pasted = "// pasted line ".repeat(8)
    val edited = code.substring(0, mid) + pasted + code.substring(mid)
    return measureSecondUpdate("$label / paste at middle", code, edited)
  }

  private fun measureThemeOnly(label: String, code: String): BenchmarkConfig.BenchmarkResult = measureSecondUpdate("$label / theme only", code, code, themeA, themeB)

  private fun measureSameTextRepeated(label: String, code: String): BenchmarkConfig.BenchmarkResult {
    val engine = IncrementalHighlighter(Languages.Kotlin)
    try {
      engine.update(code, themeA)
      repeat(BenchmarkConfig.JVM_WARMUP_ITERATIONS) { engine.update(code, themeA) }
      val times = List(BenchmarkConfig.JVM_MEASURE_ITERATIONS) {
        measureNanoTime { repeat(5) { engine.update(code, themeA) } }
      }
      return BenchmarkConfig.reportStatistics("$label / same text x5", times)
    } finally {
      engine.close()
    }
  }

  /**
   * Measures only the second [update] call (the incremental / theme-only path).
   * Engine construction and the first [update] are performed outside the timed
   * block: each iteration alternates between [initial]/[initialTheme] and
   * [edited]/[finalTheme] so that every measured call exercises the incremental
   * path against a genuinely different prior state.
   */
  private fun measureSecondUpdate(
    name: String,
    initial: String,
    edited: String,
    initialTheme: SyntaxTheme = themeA,
    finalTheme: SyntaxTheme = themeA,
  ): BenchmarkConfig.BenchmarkResult {
    val engine = IncrementalHighlighter(Languages.Kotlin)
    try {
      repeat(BenchmarkConfig.JVM_WARMUP_ITERATIONS) {
        engine.update(initial, initialTheme)
        engine.update(edited, finalTheme)
      }
      val times = List(BenchmarkConfig.JVM_MEASURE_ITERATIONS) {
        engine.update(initial, initialTheme)
        measureNanoTime { engine.update(edited, finalTheme) }
      }
      return BenchmarkConfig.reportStatistics(name, times)
    } finally {
      engine.close()
    }
  }

  private fun run(name: String, block: () -> Unit): BenchmarkConfig.BenchmarkResult {
    repeat(BenchmarkConfig.JVM_WARMUP_ITERATIONS) { block() }
    val times = List(BenchmarkConfig.JVM_MEASURE_ITERATIONS) {
      measureNanoTime { block() }
    }
    return BenchmarkConfig.reportStatistics(name, times)
  }

  private fun assertGates(
    label: String,
    baseline: BenchmarkConfig.BenchmarkResult,
    results: List<BenchmarkConfig.BenchmarkResult>,
  ) {
    val byName = results.associateBy { it.name }
    val firstCall = byName.getValue("$label / first call")
    val insertNearEnd = byName.getValue("$label / insert near end")
    val insertAtMid = byName.getValue("$label / insert at middle")
    val pasteAtMid = byName.getValue("$label / paste at middle")
    val themeOnly = byName.getValue("$label / theme only")

    val (incrementalGate, themeGate, firstCallTolerance, smallTolerance) = when (label) {
      "5k lines" -> Quad(3.0, 5.0, 0.20, 0.0)
      "1k lines" -> Quad(2.0, 3.0, 0.20, 0.0)
      "100 lines" -> Quad(0.0, 0.0, 0.20, 0.10)
      else -> error("unknown size $label")
    }

    fun ratio(b: Long, a: Long) = b.toDouble() / a.toDouble()

    if (incrementalGate > 0.0) {
      val r1 = ratio(baseline.medianNs, insertNearEnd.medianNs)
      val r2 = ratio(baseline.medianNs, insertAtMid.medianNs)
      val r3 = ratio(baseline.medianNs, pasteAtMid.medianNs)
      assertTrue(r1 >= incrementalGate, "$label / insert near end: $r1× < gate $incrementalGate×")
      assertTrue(r2 >= incrementalGate, "$label / insert at middle: $r2× < gate $incrementalGate×")
      assertTrue(r3 >= incrementalGate, "$label / paste at middle: $r3× < gate $incrementalGate×")
      val rt = ratio(baseline.medianNs, themeOnly.medianNs)
      assertTrue(rt >= themeGate, "$label / theme only: $rt× < gate $themeGate×")
    } else {
      val tolerance = smallTolerance
      val maxAllowed = (baseline.medianNs * (1.0 + tolerance)).toLong()
      assertTrue(insertNearEnd.medianNs <= maxAllowed, "$label / insert near end regressed: ${insertNearEnd.medianNs}ns > $maxAllowed")
      assertTrue(insertAtMid.medianNs <= maxAllowed, "$label / insert at middle regressed")
      assertTrue(pasteAtMid.medianNs <= maxAllowed, "$label / paste at middle regressed")
      assertTrue(themeOnly.medianNs <= maxAllowed, "$label / theme only regressed")
    }

    val firstCallMin = (baseline.medianNs * (1.0 - firstCallTolerance)).toLong()
    val firstCallMax = (baseline.medianNs * (1.0 + firstCallTolerance)).toLong()
    assertTrue(
      firstCall.medianNs in firstCallMin..firstCallMax,
      "$label / first call drifted from baseline: ${firstCall.medianNs}ns vs baseline ${baseline.medianNs}ns (±${(firstCallTolerance * 100).toInt()}%)",
    )
  }

  private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
