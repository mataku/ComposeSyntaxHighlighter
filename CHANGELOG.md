# Changelog

## [Unreleased]

- JVM jars of every language module also bundle the Linux grammar library for `x64` and `aarch64` at `lib/linux/<arch>/libktreesitter-<name>.so`, so Compose Desktop apps on Linux need no native setup. The libraries require glibc 2.17 or newer, the same floor as ktreesitter-jvm. Windows still needs the library on `java.library.path`.

## v0.9.0

- New language modules: XML and TOML. XML highlights the xml grammar of tree-sitter-xml (DTD files are not covered); attribute names use the upstream `property` capture, so they follow the theme's `property` style rather than `attribute`.

## v0.8.0

- New language modules: JSON, YAML, HTML, CSS, and Bash. HTML highlights tags, attributes, attribute values and comments; `<script>` and `<style>` bodies render in the base style because injections are not run.
- Add `tag` and `attribute` typed fields to `SyntaxTheme`, resolved for the `tag` and `attribute` captures and their dotted children (e.g. `tag.error`). Every built-in theme sets both, so HTML and CSS tags and attributes, and the annotations/attributes already emitted by the Java, Kotlin, Rust and Swift grammars, are now styled out of the box. The 0.7.x `SyntaxTheme` constructor and `copy` signatures are kept as hidden overloads, so the change is binary compatible.
- Android: every language AAR's `libktreesitter-<name>.so` is linked with 16 KB LOAD-segment alignment (`arm64-v8a`, `x86_64`), so apps depending on the language modules pass the 16 KB page-size check.

## v0.7.0

- Add `highlightLines(code, language, lineRanges)` to `:core`: parses a file once and returns theme-independent `HighlightedLine` values (`lineNumber`, `text`, `spans` of `HighlightSpan(start, end, captureName)`) for the selected 1-based line ranges, plus `HighlightedLine.toAnnotatedString(theme)`. Line boundaries are `\n` only and numbering matches git, so diff viewers can slice whole-file highlights per hunk line and re-style on theme changes without re-parsing.
- JVM jars of every language module bundle the macOS grammar library for `aarch64` and `x64` at `lib/macos/<arch>/libktreesitter-<name>.dylib`; the generated loader falls back to that resource when `java.library.path` has no copy, so Compose Desktop apps (including packaged `.app` bundles) need no native setup. The bundled libraries require macOS 26.0 or newer.
- **Breaking** (`:core`): `SyntaxTheme` is no longer a `data class`. It is an `@Immutable` class with hand-written `copy`, `equals`, `hashCode` and `toString`; the `componentN` destructuring functions are removed so future capture fields can be added without breaking binary compatibility. `resolve()` now aliases legacy capture names emitted by the bundled grammars (`escape`, `float`, `conditional`, `repeat`, `include`, `exception`, `parameter`, `character`) onto the matching typed fields, and the built-in themes seed markdown's `text.*` captures in `extras`.
- **Breaking** (`:material3-text-field`): `SyntaxHighlightedTextField` renders in a single `BasicTextField` layer and takes `value: TextFieldValue` with `onValueChange` instead of `TextFieldState`, and `singleLine` / `maxLines` / `minLines` / `keyboardActions` instead of `lineLimits` / `scrollState` / `onKeyboardAction`. `rememberSyntaxHighlightVisualTransformation` is the public entry point for the highlight transformation; `ClampingSyntaxVisualTransformation` is now internal.
- The iOS klib ABI of every published module is tracked by binary-compatibility-validator alongside the JVM dumps.
- Toolchain: Kotlin 2.4.21, Compose Multiplatform 1.12.2, AGP 9.4.1.

## v0.6.0

- New language modules: JavaScript, TypeScript (with TSX), and Markdown (with inline highlighting).
- Support iOS.
- tree-sitter ABI 15: bundled go/javascript/python/rust/markdown grammars move to ABI 15; ktreesitter 0.25.1 accepts ABI 13–15 across all targets.

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
