package io.github.mataku.compose.highlight.benchmark

import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.java.JavaLanguage
import io.github.mataku.compose.highlight.go.GoLanguage
import io.github.mataku.compose.highlight.kotlin.KotlinLanguage
import io.github.mataku.compose.highlight.python.PythonLanguage
import io.github.mataku.compose.highlight.ruby.RubyLanguage
import io.github.mataku.compose.highlight.rust.RustLanguage
import io.github.mataku.compose.highlight.swift.SwiftLanguage
import kotlin.system.measureNanoTime
import kotlin.test.Test

class JvmHighlightBenchmark {

    @Test
    fun kotlinHighlight() = runBenchmark("kotlinHighlight") {
        highlight(BenchmarkSamples.Kotlin, KotlinLanguage, BenchmarkConfig.theme)
    }

    @Test
    fun swiftHighlight() = runBenchmark("swiftHighlight") {
        highlight(BenchmarkSamples.Swift, SwiftLanguage, BenchmarkConfig.theme)
    }

    @Test
    fun rubyHighlight() = runBenchmark("rubyHighlight") {
        highlight(BenchmarkSamples.Ruby, RubyLanguage, BenchmarkConfig.theme)
    }

    @Test
    fun rustHighlight() = runBenchmark("rustHighlight") {
        highlight(BenchmarkSamples.Rust, RustLanguage, BenchmarkConfig.theme)
    }

    @Test
    fun pythonHighlight() = runBenchmark("pythonHighlight") {
        highlight(BenchmarkSamples.Python, PythonLanguage, BenchmarkConfig.theme)
    }

    @Test
    fun goHighlight() = runBenchmark("goHighlight") {
        highlight(BenchmarkSamples.Go, GoLanguage, BenchmarkConfig.theme)
    }

    @Test
    fun javaHighlight() = runBenchmark("javaHighlight") {
        highlight(BenchmarkSamples.Java, JavaLanguage, BenchmarkConfig.theme)
    }

    private fun runBenchmark(name: String, block: () -> Unit) {
        repeat(BenchmarkConfig.JVM_WARMUP_ITERATIONS) { block() }
        val times = List(BenchmarkConfig.JVM_MEASURE_ITERATIONS) {
            measureNanoTime { block() }
        }
        reportStatistics(name, times)
    }

    private fun reportStatistics(name: String, times: List<Long>) {
        val sorted = times.sorted()
        val mean = times.average()
        val median = sorted[sorted.size / 2]
        val p99 = sorted[(sorted.size * 0.99).toInt().coerceAtMost(sorted.lastIndex)]
        println("[Benchmark] $name: mean=${mean.toLong()}ns, median=${median}ns, p99=${p99}ns")
    }
}
