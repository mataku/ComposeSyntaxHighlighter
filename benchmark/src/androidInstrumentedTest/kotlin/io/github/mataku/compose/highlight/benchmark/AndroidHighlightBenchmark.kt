package io.github.mataku.compose.highlight.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.core.highlight
import io.github.mataku.compose.highlight.go.Go
import io.github.mataku.compose.highlight.java.Java
import io.github.mataku.compose.highlight.kotlin.Kotlin
import io.github.mataku.compose.highlight.python.Python
import io.github.mataku.compose.highlight.ruby.Ruby
import io.github.mataku.compose.highlight.rust.Rust
import io.github.mataku.compose.highlight.swift.Swift
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
      highlight(BenchmarkSamples.Kotlin, Languages.Kotlin, BenchmarkConfig.theme)
    }
  }

  @Test
  fun swiftHighlight() {
    benchmarkRule.measureRepeated {
      highlight(BenchmarkSamples.Swift, Languages.Swift, BenchmarkConfig.theme)
    }
  }

  @Test
  fun rubyHighlight() {
    benchmarkRule.measureRepeated {
      highlight(BenchmarkSamples.Ruby, Languages.Ruby, BenchmarkConfig.theme)
    }
  }

  @Test
  fun rustHighlight() {
    benchmarkRule.measureRepeated {
      highlight(BenchmarkSamples.Rust, Languages.Rust, BenchmarkConfig.theme)
    }
  }

  @Test
  fun pythonHighlight() {
    benchmarkRule.measureRepeated {
      highlight(BenchmarkSamples.Python, Languages.Python, BenchmarkConfig.theme)
    }
  }

  @Test
  fun goHighlight() {
    benchmarkRule.measureRepeated {
      highlight(BenchmarkSamples.Go, Languages.Go, BenchmarkConfig.theme)
    }
  }

  @Test
  fun javaHighlight() {
    benchmarkRule.measureRepeated {
      highlight(BenchmarkSamples.Java, Languages.Java, BenchmarkConfig.theme)
    }
  }
}
