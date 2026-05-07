package io.github.mataku.compose.highlight.benchmark

import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.go.Go
import io.github.mataku.compose.highlight.java.Java
import io.github.mataku.compose.highlight.kotlin.Kotlin
import io.github.mataku.compose.highlight.python.Python
import io.github.mataku.compose.highlight.ruby.Ruby
import io.github.mataku.compose.highlight.rust.Rust
import io.github.mataku.compose.highlight.swift.Swift
import kotlin.system.measureNanoTime
import kotlin.test.Test

class HighlightBenchmark {

  @Test
  fun kotlinHighlight() = runBenchmark("kotlinHighlight") {
    highlight(BenchmarkSamples.Kotlin, Languages.Kotlin, BenchmarkConfig.theme)
  }

  @Test
  fun swiftHighlight() = runBenchmark("swiftHighlight") {
    highlight(BenchmarkSamples.Swift, Languages.Swift, BenchmarkConfig.theme)
  }

  @Test
  fun rubyHighlight() = runBenchmark("rubyHighlight") {
    highlight(BenchmarkSamples.Ruby, Languages.Ruby, BenchmarkConfig.theme)
  }

  @Test
  fun rustHighlight() = runBenchmark("rustHighlight") {
    highlight(BenchmarkSamples.Rust, Languages.Rust, BenchmarkConfig.theme)
  }

  @Test
  fun pythonHighlight() = runBenchmark("pythonHighlight") {
    highlight(BenchmarkSamples.Python, Languages.Python, BenchmarkConfig.theme)
  }

  @Test
  fun goHighlight() = runBenchmark("goHighlight") {
    highlight(BenchmarkSamples.Go, Languages.Go, BenchmarkConfig.theme)
  }

  @Test
  fun javaHighlight() = runBenchmark("javaHighlight") {
    highlight(BenchmarkSamples.Java, Languages.Java, BenchmarkConfig.theme)
  }

  private fun runBenchmark(name: String, block: () -> Unit) {
    repeat(BenchmarkConfig.JVM_WARMUP_ITERATIONS) { block() }
    val times = List(BenchmarkConfig.JVM_MEASURE_ITERATIONS) {
      measureNanoTime { block() }
    }
    BenchmarkConfig.reportStatistics(name, times)
  }
}
