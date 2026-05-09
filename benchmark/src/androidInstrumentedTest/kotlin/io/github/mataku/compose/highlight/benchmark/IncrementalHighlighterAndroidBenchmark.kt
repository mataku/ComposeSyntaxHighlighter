package io.github.mataku.compose.highlight.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.IncrementalHighlighter
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.kotlin.Kotlin
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IncrementalHighlighterAndroidBenchmark {

  @get:Rule
  val benchmarkRule = BenchmarkRule()

  private val themeA: SyntaxTheme = BenchmarkConfig.theme
  private val themeB: SyntaxTheme = SyntaxTheme(
    baseStyle = SpanStyle(color = Color(0xFF202020)),
    keyword = SpanStyle(color = Color(0xFF8800FF)),
    string = SpanStyle(color = Color(0xFF008888)),
    comment = SpanStyle(color = Color(0xFF888888)),
  )

  // --- 100 lines ---

  @Test fun fullHighlightBaseline_100lines() = fullHighlightBaseline(BenchmarkSamples.Kotlin)

  @Test fun firstCall_100lines() = firstCall(BenchmarkSamples.Kotlin)

  @Test fun insertNearEnd_100lines() = insertNearEnd(BenchmarkSamples.Kotlin)

  @Test fun insertAtMiddle_100lines() = insertAtMiddle(BenchmarkSamples.Kotlin)

  @Test fun pasteAtMiddle_100lines() = pasteAtMiddle(BenchmarkSamples.Kotlin)

  @Test fun themeOnly_100lines() = themeOnly(BenchmarkSamples.Kotlin)

  @Test fun sameTextX5_100lines() = sameTextX5(BenchmarkSamples.Kotlin)

  // --- 1k lines ---

  @Test fun fullHighlightBaseline_1kLines() = fullHighlightBaseline(BenchmarkSamples.KotlinLarge1k)

  @Test fun firstCall_1kLines() = firstCall(BenchmarkSamples.KotlinLarge1k)

  @Test fun insertNearEnd_1kLines() = insertNearEnd(BenchmarkSamples.KotlinLarge1k)

  @Test fun insertAtMiddle_1kLines() = insertAtMiddle(BenchmarkSamples.KotlinLarge1k)

  @Test fun pasteAtMiddle_1kLines() = pasteAtMiddle(BenchmarkSamples.KotlinLarge1k)

  @Test fun themeOnly_1kLines() = themeOnly(BenchmarkSamples.KotlinLarge1k)

  @Test fun sameTextX5_1kLines() = sameTextX5(BenchmarkSamples.KotlinLarge1k)

  // --- 5k lines ---

  @Test fun fullHighlightBaseline_5kLines() = fullHighlightBaseline(BenchmarkSamples.KotlinLarge5k)

  @Test fun firstCall_5kLines() = firstCall(BenchmarkSamples.KotlinLarge5k)

  @Test fun insertNearEnd_5kLines() = insertNearEnd(BenchmarkSamples.KotlinLarge5k)

  @Test fun insertAtMiddle_5kLines() = insertAtMiddle(BenchmarkSamples.KotlinLarge5k)

  @Test fun pasteAtMiddle_5kLines() = pasteAtMiddle(BenchmarkSamples.KotlinLarge5k)

  @Test fun themeOnly_5kLines() = themeOnly(BenchmarkSamples.KotlinLarge5k)

  @Test fun sameTextX5_5kLines() = sameTextX5(BenchmarkSamples.KotlinLarge5k)

  // --- pattern helpers ---

  private fun fullHighlightBaseline(code: String) {
    benchmarkRule.measureRepeated {
      highlight(code, Languages.Kotlin, themeA)
    }
  }

  private fun firstCall(code: String) {
    val shuffledCode = code.lines().reversed().joinToString("\n")
    val engine = IncrementalHighlighter(Languages.Kotlin)
    try {
      benchmarkRule.measureRepeated {
        runWithTimingDisabled {
          engine.update(shuffledCode, themeA)
        }
        engine.update(code, themeA)
      }
    } finally {
      engine.close()
    }
  }

  private fun insertNearEnd(code: String) {
    val cut = (code.length - 5).coerceAtLeast(code.length / 2)
    val edited = code.substring(0, cut) + " " + code.substring(cut)
    measureSecondUpdate(initial = code, edited = edited)
  }

  private fun insertAtMiddle(code: String) {
    val mid = code.length / 2
    val edited = code.substring(0, mid) + " " + code.substring(mid)
    measureSecondUpdate(initial = code, edited = edited)
  }

  private fun pasteAtMiddle(code: String) {
    val mid = code.length / 2
    val pasted = "// pasted line ".repeat(8)
    val edited = code.substring(0, mid) + pasted + code.substring(mid)
    measureSecondUpdate(initial = code, edited = edited)
  }

  private fun themeOnly(code: String) {
    measureSecondUpdate(
      initial = code,
      edited = code,
      initialTheme = themeA,
      finalTheme = themeB,
    )
  }

  private fun sameTextX5(code: String) {
    val engine = IncrementalHighlighter(Languages.Kotlin)
    try {
      engine.update(code, themeA)
      benchmarkRule.measureRepeated {
        repeat(5) { engine.update(code, themeA) }
      }
    } finally {
      engine.close()
    }
  }

  private fun measureSecondUpdate(
    initial: String,
    edited: String,
    initialTheme: SyntaxTheme = themeA,
    finalTheme: SyntaxTheme = themeA,
  ) {
    val engine = IncrementalHighlighter(Languages.Kotlin)
    try {
      benchmarkRule.measureRepeated {
        runWithTimingDisabled {
          engine.update(initial, initialTheme)
        }
        engine.update(edited, finalTheme)
      }
    } finally {
      engine.close()
    }
  }
}
