package io.github.mataku.compose.highlight.benchmark

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.kotlin.Kotlin
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

// Single-shot non-benchmark verification that the library can highlight a ~5k-line
// input on an Android device. Companion to LargeInputAndroidBenchmark, which omits
// 5k variants because the BenchmarkRule.measureRepeated tight loop hits Scudo OOM
// (ktreesitter has no Parser/Tree close API; native cleanup is GC-driven and does
// not keep up with per-iteration allocations under low JVM heap pressure).
//
// One-shot consumer usage (load a 5k file, call highlight() once) does not exhibit
// that problem. This test is the on-device guarantee that real-world 5k file
// loading still works; absolute parse time for 5k is not published — extrapolate
// linearly from the 1k numbers in docs/large_input_profiling.md.
@RunWith(AndroidJUnit4::class)
class LargeInput5kSmokeTest {

  @Test
  fun fullHighlight_5kLines_oneShot() {
    val code = BenchmarkSamples.KotlinLarge5k
    val result = highlight(code, Languages.Kotlin, BenchmarkConfig.theme)
    assertEquals("highlight must preserve input length at 5k", code.length, result.length)
  }
}
