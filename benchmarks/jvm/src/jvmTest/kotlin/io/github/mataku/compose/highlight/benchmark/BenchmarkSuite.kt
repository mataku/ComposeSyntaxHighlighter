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

class BenchmarkSuite {

  @Test
  fun runAllBenchmarks() {
    BenchmarkConfig.printEnvironment()
    BenchmarkConfig.printSampleSizes()

    println("=== First use [Cold] (includes query compilation) ===")
    val coldResults = mutableListOf<Pair<String, Long>>()

    fun measureCold(name: String, block: () -> Unit): Long {
      val timeNs = measureNanoTime { block() }
      coldResults += name to timeNs
      println("[First use] $name: ${timeNs}ns (${"%.3f".format(timeNs / 1_000_000.0)}ms)")
      return timeNs
    }

    measureCold("Kotlin") {
      highlight(BenchmarkSamples.Kotlin, Languages.Kotlin, BenchmarkConfig.theme)
    }
    measureCold("Swift") {
      highlight(BenchmarkSamples.Swift, Languages.Swift, BenchmarkConfig.theme)
    }
    measureCold("Ruby") {
      highlight(BenchmarkSamples.Ruby, Languages.Ruby, BenchmarkConfig.theme)
    }
    measureCold("Rust") {
      highlight(BenchmarkSamples.Rust, Languages.Rust, BenchmarkConfig.theme)
    }
    measureCold("Python") {
      highlight(BenchmarkSamples.Python, Languages.Python, BenchmarkConfig.theme)
    }
    measureCold("Go") {
      highlight(BenchmarkSamples.Go, Languages.Go, BenchmarkConfig.theme)
    }
    measureCold("Java") {
      highlight(BenchmarkSamples.Java, Languages.Java, BenchmarkConfig.theme)
    }

    println()
    println("=== Subsequent use [Warm] (query already compiled) ===")
    val warmResults = mutableListOf<BenchmarkConfig.BenchmarkResult>()

    fun runAndRecord(name: String, block: () -> Unit) {
      repeat(BenchmarkConfig.JVM_WARMUP_ITERATIONS) { block() }
      val times = List(BenchmarkConfig.JVM_MEASURE_ITERATIONS) {
        measureNanoTime { block() }
      }
      warmResults += BenchmarkConfig.reportStatistics(name, times)
    }

    runAndRecord("Kotlin") {
      highlight(BenchmarkSamples.Kotlin, Languages.Kotlin, BenchmarkConfig.theme)
    }
    runAndRecord("Swift") {
      highlight(BenchmarkSamples.Swift, Languages.Swift, BenchmarkConfig.theme)
    }
    runAndRecord("Ruby") {
      highlight(BenchmarkSamples.Ruby, Languages.Ruby, BenchmarkConfig.theme)
    }
    runAndRecord("Rust") {
      highlight(BenchmarkSamples.Rust, Languages.Rust, BenchmarkConfig.theme)
    }
    runAndRecord("Python") {
      highlight(BenchmarkSamples.Python, Languages.Python, BenchmarkConfig.theme)
    }
    runAndRecord("Go") {
      highlight(BenchmarkSamples.Go, Languages.Go, BenchmarkConfig.theme)
    }
    runAndRecord("Java") {
      highlight(BenchmarkSamples.Java, Languages.Java, BenchmarkConfig.theme)
    }

    println()
    println("### First use Markdown table")
    println("| Language | First use [Cold] (ms) |")
    println("|----------|----------------------|")
    coldResults.forEach { (name, ns) ->
      println("| $name | ${"%.3f".format(ns / 1_000_000.0)} |")
    }

    println()
    BenchmarkConfig.printMarkdown(warmResults)
  }

  private fun runBenchmark(name: String, block: () -> Unit) {
    repeat(BenchmarkConfig.JVM_WARMUP_ITERATIONS) { block() }
    val times = List(BenchmarkConfig.JVM_MEASURE_ITERATIONS) {
      measureNanoTime { block() }
    }
    BenchmarkConfig.reportStatistics(name, times)
  }
}
