package io.github.mataku.compose.highlight.benchmark

import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.go.GoLanguage
import io.github.mataku.compose.highlight.java.JavaLanguage
import io.github.mataku.compose.highlight.kotlin.KotlinLanguage
import io.github.mataku.compose.highlight.python.PythonLanguage
import io.github.mataku.compose.highlight.ruby.RubyLanguage
import io.github.mataku.compose.highlight.rust.RustLanguage
import io.github.mataku.compose.highlight.swift.SwiftLanguage
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
      highlight(BenchmarkSamples.Kotlin, KotlinLanguage, BenchmarkConfig.theme)
    }
    measureCold("Swift") {
      highlight(BenchmarkSamples.Swift, SwiftLanguage, BenchmarkConfig.theme)
    }
    measureCold("Ruby") {
      highlight(BenchmarkSamples.Ruby, RubyLanguage, BenchmarkConfig.theme)
    }
    measureCold("Rust") {
      highlight(BenchmarkSamples.Rust, RustLanguage, BenchmarkConfig.theme)
    }
    measureCold("Python") {
      highlight(BenchmarkSamples.Python, PythonLanguage, BenchmarkConfig.theme)
    }
    measureCold("Go") {
      highlight(BenchmarkSamples.Go, GoLanguage, BenchmarkConfig.theme)
    }
    measureCold("Java") {
      highlight(BenchmarkSamples.Java, JavaLanguage, BenchmarkConfig.theme)
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
      highlight(BenchmarkSamples.Kotlin, KotlinLanguage, BenchmarkConfig.theme)
    }
    runAndRecord("Swift") {
      highlight(BenchmarkSamples.Swift, SwiftLanguage, BenchmarkConfig.theme)
    }
    runAndRecord("Ruby") {
      highlight(BenchmarkSamples.Ruby, RubyLanguage, BenchmarkConfig.theme)
    }
    runAndRecord("Rust") {
      highlight(BenchmarkSamples.Rust, RustLanguage, BenchmarkConfig.theme)
    }
    runAndRecord("Python") {
      highlight(BenchmarkSamples.Python, PythonLanguage, BenchmarkConfig.theme)
    }
    runAndRecord("Go") {
      highlight(BenchmarkSamples.Go, GoLanguage, BenchmarkConfig.theme)
    }
    runAndRecord("Java") {
      highlight(BenchmarkSamples.Java, JavaLanguage, BenchmarkConfig.theme)
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
