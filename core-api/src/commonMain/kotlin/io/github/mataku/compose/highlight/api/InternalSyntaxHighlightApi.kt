package io.github.mataku.compose.highlight.api

@RequiresOptIn(
  message = "Internal Compose Syntax Highlight SPI. Use only when implementing a language module.",
  level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
annotation class InternalSyntaxHighlightApi
