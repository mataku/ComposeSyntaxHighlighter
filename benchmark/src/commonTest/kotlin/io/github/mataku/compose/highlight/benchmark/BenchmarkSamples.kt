package io.github.mataku.compose.highlight.benchmark

object BenchmarkSamples {
    val Kotlin = """
        package com.example

        import kotlin.math.PI

        class Calculator {
            fun add(a: Int, b: Int): Int = a + b
            fun subtract(a: Int, b: Int): Int = a - b
        }

        fun main() {
            val calc = Calculator()
            println(calc.add(1, 2))
            println("PI = ${'$'}PI")
            // comment
        }
    """.trimIndent()

    val Swift = """
        import Foundation

        class Calculator {
            func add(a: Int, b: Int) -> Int {
                return a + b
            }
        }

        let calc = Calculator()
        print(calc.add(a: 1, b: 2))
        // comment
    """.trimIndent()

    val Ruby = """
        require 'json'

        class Calculator
          def add(a, b)
            a + b
          end
        end

        calc = Calculator.new
        puts calc.add(1, 2)
        # comment
    """.trimIndent()

    val Rust = """
        use std::fmt;

        struct Calculator;

        impl Calculator {
            fn add(a: i32, b: i32) -> i32 {
                a + b
            }
        }

        fn main() {
            let calc = Calculator;
            println!("{}", calc.add(1, 2));
            // comment
        }
    """.trimIndent()

    val Python = """
        import json

        class Calculator:
            def add(self, a, b):
                return a + b

        calc = Calculator()
        print(calc.add(1, 2))
        # comment
    """.trimIndent()

    val Go = """
        package main

        import "fmt"

        type Calculator struct{}

        func (c Calculator) Add(a, b int) int {
            return a + b
        }

        func main() {
            calc := Calculator{}
            fmt.Println(calc.Add(1, 2))
        }
    """.trimIndent()

    val Java = """
        package com.example;

        public class Calculator {
            public int add(int a, int b) {
                return a + b;
            }

            public static void main(String[] args) {
                Calculator calc = new Calculator();
                System.out.println(calc.add(1, 2));
                // comment
            }
        }
    """.trimIndent()
}
