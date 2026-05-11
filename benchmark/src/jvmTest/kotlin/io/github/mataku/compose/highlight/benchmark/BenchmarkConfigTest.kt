package io.github.mataku.compose.highlight.benchmark

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BenchmarkConfigTest {

  @Test
  fun reportStatistics_emitsMinMedianMaxOnly() {
    val captured = captureStdout {
      BenchmarkConfig.reportStatistics("sample", listOf(1L, 2L, 3L, 4L, 5L))
    }
    assertTrue("min=" in captured, "should report min: $captured")
    assertTrue("median=" in captured, "should report median: $captured")
    assertTrue("max=" in captured, "should report max: $captured")
    assertFalse("mean=" in captured, "should not report mean: $captured")
    assertFalse("stddev=" in captured, "should not report stddev: $captured")
    assertFalse("p99" in captured, "should not report p99: $captured")
  }

  @Test
  fun reportStatistics_returnsMinMedianMax() {
    val result = BenchmarkConfig.reportStatistics("sample", listOf(1L, 2L, 3L, 4L, 5L))
    assertTrue(result.minNs == 1L, "min: ${result.minNs}")
    assertTrue(result.medianNs == 3L, "median: ${result.medianNs}")
    assertTrue(result.maxNs == 5L, "max: ${result.maxNs}")
  }

  @Test
  fun printMarkdown_emitsThreeColumnFormat() {
    val captured = captureStdout {
      BenchmarkConfig.printMarkdown(
        listOf(BenchmarkConfig.BenchmarkResult("a", minNs = 1_000_000L, medianNs = 2_000_000L, maxNs = 3_000_000L)),
      )
    }
    val header = captured.lines().first { it.startsWith("|") && "Min" in it }
    assertTrue("Min (ms)" in header, "header missing Min: $header")
    assertTrue("Median (ms)" in header, "header missing Median: $header")
    assertTrue("Max (ms)" in header, "header missing Max: $header")
    assertFalse("Mean" in header, "header should not have Mean: $header")
    assertFalse("StdDev" in header, "header should not have StdDev: $header")
    assertFalse("P99" in header, "header should not have P99: $header")
  }

  private fun captureStdout(block: () -> Unit): String {
    val original = System.out
    val buffer = ByteArrayOutputStream()
    System.setOut(PrintStream(buffer))
    try {
      block()
    } finally {
      System.setOut(original)
    }
    return buffer.toString()
  }
}
