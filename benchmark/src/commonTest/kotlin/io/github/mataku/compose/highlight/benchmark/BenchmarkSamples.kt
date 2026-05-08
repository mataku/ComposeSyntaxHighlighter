package io.github.mataku.compose.highlight.benchmark

object BenchmarkSamples {
  val Kotlin = readSample("Kotlin.kt")
  val Swift = readSample("Swift.swift")
  val Ruby = readSample("Ruby.rb")
  val Rust = readSample("Rust.rs")
  val Python = readSample("Python.py")
  val Go = readSample("Go.go")
  val Java = readSample("Java.java")

  val KotlinLarge1k: String by lazy { synthesizeKotlin(repeats = 10) }
  val KotlinLarge5k: String by lazy { synthesizeKotlin(repeats = 50) }

  private fun readSample(filename: String): String = javaClass.classLoader
    ?.getResourceAsStream("samples/$filename")
    ?.bufferedReader()
    ?.use { it.readText() }
    ?: error("Sample not found: $filename")

  private fun synthesizeKotlin(repeats: Int): String {
    val source = Kotlin.lines()
    val header = source.takeWhile { it.startsWith("package ") || it.startsWith("import ") || it.isBlank() }
    val body = source.drop(header.size)
    return buildString {
      header.forEach { appendLine(it) }
      repeat(repeats) {
        body.forEach { appendLine(it) }
      }
    }
  }
}
