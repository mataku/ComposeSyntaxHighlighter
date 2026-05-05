package io.github.mataku.compose.highlight.benchmark

object BenchmarkSamples {
    val Kotlin = readSample("Kotlin.kt")
    val Swift = readSample("Swift.swift")
    val Ruby = readSample("Ruby.rb")
    val Rust = readSample("Rust.rs")
    val Python = readSample("Python.py")
    val Go = readSample("Go.go")
    val Java = readSample("Java.java")

    private fun readSample(filename: String): String {
        return javaClass.classLoader
            ?.getResourceAsStream("samples/$filename")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("Sample not found: $filename")
    }
}
