# Changelog

## [Unreleased]

## v0.5.0
Rename project from "Compose Highlight" to "Compose Syntax Highlight" for clarity.

- Maven coordinates: `io.github.mataku:compose-highlight-*` → `io.github.mataku:compose-syntax-highlight-*` (`core`, `api`, `kotlin`, `swift`, `ruby`, `rust`, `python`, `go`, `java`). Earlier `v0.1.0`–`v0.3.0` releases remain published under the old coordinates.
- Internal Gradle convention plugin ids and DSL: `compose-highlight-{language,kdoc,spotless}` → `compose-syntax-highlight-{language,kdoc,spotless}`, and `composeHighlightLanguage { ... }` → `composeSyntaxHighlightLanguage { ... }`.
- POM display names, README, NOTICE headers, and demo app title updated accordingly.
- Add syntax highlighted text field: `:material3-text-field`

## v0.3.0

Replace `SyntaxTheme.styles` map with typed `SpanStyle?` fields per capture, plus `extras` for grammar-specific captures.

## v0.2.0

Enhance the documentation.

## v0.1.0

Initial release.
