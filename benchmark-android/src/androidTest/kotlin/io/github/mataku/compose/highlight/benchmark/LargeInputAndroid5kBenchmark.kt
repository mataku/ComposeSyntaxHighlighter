package io.github.mataku.compose.highlight.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.kotlin.Kotlin
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Flagship-class Android `full highlight` benchmark at 5k lines.
//
// Pixel 10 (`frankel`, Tensor G5, 16 GB RAM) tolerates the 100/1k benchmark
// suite in LargeInputAndroidBenchmark. This class probes whether one 5k
// full-highlight test in isolation can also complete on the same flagship
// hardware before Scudo OOM strikes — the same structural risk documented on
// LargeInputAndroidBenchmark (BenchmarkRule.measureRepeated allocates
// ktreesitter Parser/Tree wrappers faster than the GC-driven Cleaner cleanup
// keeps up). The risk is not eliminated by isolation — it is empirically
// tested.
//
// Run only on flagship-class devices via scripts/run-android-ftl-5k-benchmark.sh.
// Lower-spec devices are expected to OOM and should not run this class.
//
// For one-shot on-device functional verification on any device class, see
// LargeInput5kSmokeTest instead.
@RunWith(AndroidJUnit4::class)
class LargeInputAndroid5kBenchmark {

  @get:Rule
  val benchmarkRule = BenchmarkRule()

  @Test fun fullHighlight_5kLines() {
    val code = BenchmarkSamples.KotlinLarge5k
    benchmarkRule.measureRepeated {
      highlight(code, Languages.Kotlin, BenchmarkConfig.theme)
    }
  }
}
