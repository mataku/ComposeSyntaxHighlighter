package io.github.mataku.compose.highlight.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.java.JavaLanguage
import io.github.mataku.compose.highlight.go.GoLanguage
import io.github.mataku.compose.highlight.kotlin.KotlinLanguage
import io.github.mataku.compose.highlight.python.PythonLanguage
import io.github.mataku.compose.highlight.ruby.RubyLanguage
import io.github.mataku.compose.highlight.rust.RustLanguage
import io.github.mataku.compose.highlight.swift.SwiftLanguage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidHighlightBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun kotlinHighlight() {
        benchmarkRule.measureRepeated {
            highlight(BenchmarkSamples.Kotlin, KotlinLanguage, BenchmarkConfig.theme)
        }
    }

    @Test
    fun swiftHighlight() {
        benchmarkRule.measureRepeated {
            highlight(BenchmarkSamples.Swift, SwiftLanguage, BenchmarkConfig.theme)
        }
    }

    @Test
    fun rubyHighlight() {
        benchmarkRule.measureRepeated {
            highlight(BenchmarkSamples.Ruby, RubyLanguage, BenchmarkConfig.theme)
        }
    }

    @Test
    fun rustHighlight() {
        benchmarkRule.measureRepeated {
            highlight(BenchmarkSamples.Rust, RustLanguage, BenchmarkConfig.theme)
        }
    }

    @Test
    fun pythonHighlight() {
        benchmarkRule.measureRepeated {
            highlight(BenchmarkSamples.Python, PythonLanguage, BenchmarkConfig.theme)
        }
    }

    @Test
    fun goHighlight() {
        benchmarkRule.measureRepeated {
            highlight(BenchmarkSamples.Go, GoLanguage, BenchmarkConfig.theme)
        }
    }

    @Test
    fun javaHighlight() {
        benchmarkRule.measureRepeated {
            highlight(BenchmarkSamples.Java, JavaLanguage, BenchmarkConfig.theme)
        }
    }
}
