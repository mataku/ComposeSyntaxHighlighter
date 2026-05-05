package io.github.mataku.compose.highlight.benchmark

import io.github.mataku.compose.highlight.core.SyntaxTheme

object BenchmarkConfig {
    val theme: SyntaxTheme = SyntaxTheme.DarkDefault
    const val JVM_WARMUP_ITERATIONS = 10
    const val JVM_MEASURE_ITERATIONS = 50
}
