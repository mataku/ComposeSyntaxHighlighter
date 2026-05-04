# ComposeSyntaxHighlighter — Agent Guide

## Overview

ComposeSyntaxHighlighter is a **Kotlin Multiplatform (KMP)** library that provides syntax-highlighted `Text` composables for Jetpack Compose / Compose Multiplatform.

- **Rendering**: Produces an `AnnotatedString` via `tree-sitter` query captures and renders it with Compose `Text`.
- **Platforms**: Android, iOS, JVM (Desktop), JS, and WasmJs.
- **Languages**: Kotlin and Swift (more can be added as separate modules).
- **UI target**: Material3 only (`minSdk = 26`).

## Module Structure

```
root
├── build-logic/                    # Custom Gradle plugin for language modules
├── composeApp/                     # Demo app (Android, iOS, Web)
├── core/                           # Core highlighting engine & public API
├── languages/
│   ├── kotlin/                     # Kotlin language support
│   └── swift/                      # Swift language support
└── gradle/libs.versions.toml       # Version catalog
```

### `:core`
- **Common API**:
  - `SyntaxHighlightedText(code, language, theme, ...)` — main composable.
  - `SyntaxTheme(baseStyle, styles)` — maps tree-sitter capture names (e.g. `keyword`, `string.escape`) to `SpanStyle`.
  - `Language` — expect/actual class abstracting the parser backend.
- **ktreesitterMain** (Android + JVM): Uses `io.github.tree-sitter:ktreesitter` (JNI binding).
- **wasmJsMain**: Uses `web-tree-sitter` loaded dynamically in the browser.
- **Key implementation files**:
  - `Highlighter.kt` — platform-specific highlight logic.
  - `rememberHighlightedString.kt` — `remember` (ktreesitter) or `produceState` (wasmJs).
  - `Utf8ByteIndex.kt` — maps UTF-8 byte offsets from tree-sitter to Kotlin `String` indices.

### `:languages:<name>`
Each language module applies the custom `compose-syntax-language` plugin and provides:
- A `tree-sitter-<lang>` grammar submodule.
- `commonMain`: `expect val <Lang>Language: Language`
- `ktreesitterMain`: JNI parser via `ktreesitter` plugin + `HIGHLIGHTS_QUERY` string.
- `wasmJsMain`: Loads the `.wasm` grammar via Compose Resources.
- `jvmTest`: Unit tests using host CMake-built shared library.

## Build System

### `build-logic` — `compose-syntax-language` plugin

Located at `build-logic/src/main/kotlin/compose-syntax-language.gradle.kts`.

This convention plugin is applied to every language module. It wires together:
- `org.jetbrains.kotlin.multiplatform`
- `com.android.library`
- `io.github.tree-sitter.ktreesitter-plugin`
- `org.jetbrains.compose` + Compose Compiler

Per-language configuration DSL (`ComposeSyntaxLanguageExtension`):

```kotlin
composeSyntaxLanguage {
    languageName.set("kotlin")
    grammarSubmodulePath.set("tree-sitter-kotlin")
    parserClassName.set("TreeSitterKotlin")
    sources.set(listOf("src/scanner.c", "src/parser.c"))
    queries.set(listOf("queries/highlights.scm"))
    licenseSpdx.set("MIT")
    licenseSource.set("fwcd/tree-sitter-kotlin (MIT)")
}
```

**What the plugin does automatically**:
1. **Generate parser source** (`generateParserSource`): Runs `tree-sitter generate --abi=<treesitterAbi>` inside the grammar submodule.
2. **Generate JNI binding** (`generateGrammarFiles` from ktreesitter plugin): Creates Kotlin JNI wrapper for the parser.
3. **Generate highlights query** (`generateHighlightsQuery`): Embeds `queries/highlights.scm` as a Kotlin `const val HIGHLIGHTS_QUERY`.
4. **Generate wasm stub** (`generateWasmTreeSitterStub`): Provides an unused stub object for the wasmJs target (actual grammar is loaded from `.wasm` at runtime).
5. **CMake configuration**: Writes `CMakeLists.txt` (Android) and `host-cmake/CMakeLists.txt` (host for JVM tests).
6. **Build host shared library** (`buildHostCMake`): Builds the parser `.so`/`.dylib`/`.dll` for JVM tests.
7. **Build grammar WASM** (`buildGrammarWasm`): Compiles grammar to `.wasm` for the wasmJs target (skipped if `tree-sitter` CLI, `docker`, or `emcc` is unavailable).

### Source Set Hierarchy

```
common
├── ktreesitter (androidTarget + jvm)
│   └── uses ktreesitter JNI
└── web (wasmJs)
    └── uses web-tree-sitter
```

Configured via `applyDefaultHierarchyTemplate` in both `core` and language modules.

## Adding a New Language

1. **Create module**: `languages/<lang>/`
2. **Add submodule**: `git submodule add <tree-sitter-grammar-repo> languages/<lang>/tree-sitter-<lang>`
3. **Create `build.gradle.kts`**:
   ```kotlin
   plugins { id("compose-syntax-language") }
   composeSyntaxLanguage {
       languageName.set("<lang>")
       grammarSubmodulePath.set("tree-sitter-<lang>")
       parserClassName.set("TreeSitter<Lang>")
       sources.set(listOf("src/parser.c", "src/scanner.c"))
       queries.set(listOf("queries/highlights.scm"))
       licenseSpdx.set("...")
       licenseSource.set("...")
   }
   ```
4. **Add to `settings.gradle.kts`**:
   ```kotlin
   include(":languages:<lang>")
   ```
5. **Implement source files**:
   - `src/commonMain/kotlin/.../<Lang>Language.kt`: `expect val <Lang>Language: Language`
   - `src/ktreesitterMain/kotlin/.../<Lang>Language.kt`: `actual val ...` using `kTreeSitterLanguage(..., HIGHLIGHTS_QUERY)`
   - `src/wasmJsMain/kotlin/.../<Lang>Language.kt`: `actual val ...` using `webTreeSitterLanguage(key, HIGHLIGHTS_QUERY, grammarBytesProvider)`
   - `src/jvmTest/kotlin/.../<Lang>HighlightTest.kt`: unit tests

## Running Tests

### JVM tests for a language module
```bash
./gradlew :languages:kotlin:jvmTest
./gradlew :languages:swift:jvmTest
```

These require the **host CMake shared library** to be built first (handled automatically via `buildHostCMake` task dependency). The test task sets `java.library.path` to the host CMake output directory.

### Common tests in `:core`
```bash
./gradlew :core:commonTest
```

## Platform-Specific Notes

### Android
- Uses `externalNativeBuild.cmake` with the generated `CMakeLists.txt`.
- NDK version is pinned in the plugin (`ndkVersion = "26.3.11579264"`).
- ABI filters: `x86_64`, `arm64-v8a`, `armeabi-v7a`.

### JVM / Desktop
- At runtime, the host OS shared library (built by host CMake) must be on `java.library.path`.
- `jvmTest` automatically configures this.

### WasmJs
- Requires `tree-sitter` CLI + Docker or Emscripten (`emcc`) on PATH to build the `.wasm` grammar.
- If unavailable, `buildGrammarWasm` is skipped and wasmJs targets may fail at runtime.
- The `web-tree-sitter` npm package version is declared in `libs.versions.toml` (`webTreeSitter`).

## Tree-sitter ABI Version Pinning

`libs.versions.toml` pins `treesitterAbi = "14"` because `ktreesitter 0.24.1` only supports ABI 13–14. Modern `tree-sitter-cli` (0.25+) defaults to ABI 15, so the plugin explicitly passes `--abi=14` when generating the parser.

**Bump in lockstep** with `ktreesitter` when it adds ABI 15 support.

## Key Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.3.20 | KMP compiler |
| Compose Multiplatform | 1.10.3 | UI framework |
| AGP | 8.11.2 | Android build |
| ktreesitter | 0.24.1 | Tree-sitter Kotlin bindings |
| web-tree-sitter | 0.24.7 | Browser tree-sitter runtime |
| material3 | 1.10.0-alpha05 | Compose Material3 |

## Public API Usage Example

```kotlin
import io.github.mataku.compose.syntax.core.SyntaxHighlightedText
import io.github.mataku.compose.syntax.core.SyntaxTheme
import io.github.mataku.compose.syntax.language.kotlin.KotlinLanguage

@Composable
fun CodeBlock(code: String) {
    val theme = SyntaxTheme(
        baseStyle = SpanStyle(color = Color.White),
        styles = mapOf(
            "keyword" to SpanStyle(color = Color.Cyan),
            "string" to SpanStyle(color = Color.Green),
            "comment" to SpanStyle(color = Color.Gray),
        ),
    )
    SyntaxHighlightedText(
        code = code,
        language = KotlinLanguage,
        theme = theme,
    )
}
```

## License Notes

Each language module bundles a `tree-sitter-*` grammar submodule. Ensure `licenseSpdx` and `licenseSource` in `composeSyntaxLanguage` reflect the upstream grammar license. Currently all bundled grammars are MIT licensed.
