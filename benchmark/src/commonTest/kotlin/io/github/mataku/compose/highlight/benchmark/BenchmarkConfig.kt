package io.github.mataku.compose.highlight.benchmark

import io.github.mataku.compose.highlight.core.SyntaxTheme

object BenchmarkConfig {
  val theme: SyntaxTheme = SyntaxTheme.DarkDefault
  const val JVM_WARMUP_ITERATIONS = 10
  const val JVM_MEASURE_ITERATIONS = 50

  data class BenchmarkResult(
    val name: String,
    val minNs: Long,
    val medianNs: Long,
    val maxNs: Long,
  )

  fun printEnvironment() {
    val rt = Runtime.getRuntime()
    println("Benchmark Environment")
    println("-".repeat(40))
    println("OS: ${System.getProperty("os.name")} (${System.getProperty("os.version")})")
    println("Arch: ${System.getProperty("os.arch")}")
    println("JVM: ${System.getProperty("java.vm.name")} ${System.getProperty("java.version")}")
    println("Processors: ${rt.availableProcessors()}")
    println("Max heap: ${rt.maxMemory() / 1024 / 1024} MB")
    println("Total heap: ${rt.totalMemory() / 1024 / 1024} MB")
    println("JVM args: ${java.lang.management.ManagementFactory.getRuntimeMXBean().inputArguments}")
    println("Warmup iterations: $JVM_WARMUP_ITERATIONS")
    println("Measure iterations: $JVM_MEASURE_ITERATIONS")
    println("-".repeat(40))
  }

  fun printSampleSizes() {
    println("Sample code sizes (characters / lines)")
    println("-".repeat(40))
    listOf(
      "Kotlin" to BenchmarkSamples.Kotlin,
      "Swift" to BenchmarkSamples.Swift,
      "Ruby" to BenchmarkSamples.Ruby,
      "Rust" to BenchmarkSamples.Rust,
      "Python" to BenchmarkSamples.Python,
      "Go" to BenchmarkSamples.Go,
      "Java" to BenchmarkSamples.Java,
    ).forEach { (name, code) ->
      println("$name: ${code.length} chars / ${code.lines().size} lines")
    }
    println("-".repeat(40))
  }

  fun reportStatistics(name: String, times: List<Long>): BenchmarkResult {
    val sorted = times.sorted()
    val median = sorted[sorted.size / 2]
    val min = sorted.first()
    val max = sorted.last()
    println("[Benchmark] $name: min=${min}ns, median=${median}ns, max=${max}ns")
    return BenchmarkResult(name, min, median, max)
  }

  fun printMarkdown(results: List<BenchmarkResult>) {
    println()
    println("### Markdown table")
    println("| Stage | Min (ms) | Median (ms) | Max (ms) |")
    println("|-------|---------:|------------:|---------:|")
    results.forEach { r ->
      fun ms(ns: Long) = "%.3f".format(ns / 1_000_000.0)
      println("| ${r.name} | ${ms(r.minNs)} | ${ms(r.medianNs)} | ${ms(r.maxNs)} |")
    }
    println()
  }
}
