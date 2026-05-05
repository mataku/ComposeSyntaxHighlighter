# ComposeSyntaxHighlighter — Agent Guide

## Overview

ComposeSyntaxHighlighter is a **Kotlin Multiplatform (KMP)** library that provides syntax-highlighted `Text` composables for Jetpack Compose / Compose Multiplatform.

- **Rendering**: Produces an `AnnotatedString` from `tree-sitter` query captures and renders it with Compose `Text`.
- **Targets**: Android (published AAR) and JVM (published JAR for Compose Desktop interop).
- **Languages**: Kotlin and Swift (more can be added as separate modules).
- **UI target**: Material3 only (`minSdk = 26`).
- **Browser**: out of scope — use a JS-side highlighter (highlight.js, Shiki) for web apps.

## Module Structure

```
root
├── build-logic/                    # Custom Gradle plugin for language modules
├── composeApp/                     # Demo app (Android only)
├── core/                           # Core highlighting engine & public API
├── languages/
│   ├── kotlin/                     # Kotlin language support
│   └── swift/                      # Swift language support
└── gradle/libs.versions.toml       # Version catalog
```

### `:core`

Maven coordinates: `io.github.mataku:compose-highlight-core:<version>`.

Public API in `commonMain` (no expect/actual — every target is JVM-based):
- `SyntaxHighlightedText(code, language, theme, style, modifier)` — main composable.
- `SyntaxTheme(baseStyle, styles)` — maps tree-sitter capture names (e.g. `keyword`, `string.escape`) to `SpanStyle`. Provides `SyntaxTheme.darkDefault()` / `SyntaxTheme.lightDefault()`.
- `LocalSyntaxTheme` — composition local that defaults to `SyntaxTheme.darkDefault()`.
- `Language` — concrete class wrapping a KTreeSitter `Language` + a highlights query.
- `kTreeSitterLanguage(parser, highlightsQuery)` — factory used by language modules.
- `highlight(code, language, theme)` / `rememberHighlightedString(...)` — the underlying functions.

Implementation files live under `core/src/commonMain/kotlin/io/github/mataku/compose/highlight/core/`.

### `:languages:<name>`

Maven coordinates: `io.github.mataku:compose-highlight-<name>:<version>`. Each language module:

- Applies the custom `compose-highlight-language` convention plugin.
- Vendors a `tree-sitter-<lang>` grammar submodule.
- Exposes a single `val <Lang>Language: Language` from `commonMain`.

## Build System

### `build-logic` — `compose-highlight-language` plugin

Located at `build-logic/src/main/kotlin/compose-highlight-language.gradle.kts`.

Per-language configuration DSL (`ComposeHighlightLanguageExtension`):

```kotlin
composeHighlightLanguage {
    languageName.set("kotlin")
    grammarSubmodulePath.set("tree-sitter-kotlin")
    parserClassName.set("TreeSitterKotlin")
    sources.set(listOf("src/scanner.c", "src/parser.c"))
    queries.set(listOf("queries/highlights.scm"))
    licenseSpdx.set("MIT")
    licenseSource.set("fwcd/tree-sitter-kotlin (MIT)")
}
```

What the plugin does automatically:
1. **`generateParserSource`**: runs `tree-sitter generate --abi=<treesitterAbi>` inside the grammar submodule (skipped if `parser.c` is already up-to-date).
2. **`generateGrammarFiles`** (from the ktreesitter plugin): generates the JNI binding object (`TreeSitter<Lang>`).
3. **`generateHighlightsQuery`**: embeds `queries/highlights.scm` as `internal const val HIGHLIGHTS_QUERY`.
4. **`generateNotice`**: writes `META-INF/NOTICE` from `NOTICE.tpl` plus a per-grammar entry, wired into both KMP `commonMain.resources` and AGP `main.resources`.
5. **CMake configuration**: writes `CMakeLists.txt` (Android NDK) and `host-cmake/CMakeLists.txt` (host JVM tests).
6. **`buildHostCMake`**: builds the parser `.dylib`/`.so`/`.dll` consumed by `jvmTest`.
7. **vanniktech maven-publish**: sets POM coordinates `compose-highlight-<name>` and POM name/description.
8. **AGP packaging**: drops `META-INF/NOTICE` from the default excludes and pickFirsts it so the AAR's classes.jar retains the file.

### Source set hierarchy

Both `:core` and language modules use the default KMP hierarchy with no custom intermediate sets:

```
commonMain  (all production code)
├── androidMain  (no extra source)
└── jvmMain      (no extra source)
```

`commonTest` runs across `androidUnitTest` and `jvmTest`. Parser-using golden tests live in `<module>/src/jvmTest/` so they can run on host JVM with `java.library.path` pointing at the host CMake output.

## Adding a new language

1. **Create module**: `languages/<lang>/`.
2. **Add submodule**: `git submodule add <tree-sitter-grammar-repo> languages/<lang>/tree-sitter-<lang>`.
3. **Create `build.gradle.kts`**:
   ```kotlin
   plugins { id("compose-highlight-language") }

   composeHighlightLanguage {
       languageName.set("<lang>")
       grammarSubmodulePath.set("tree-sitter-<lang>")
       parserClassName.set("TreeSitter<Lang>")
       sources.set(listOf("src/parser.c", "src/scanner.c"))
       queries.set(listOf("queries/highlights.scm"))
       licenseSpdx.set("MIT")
       licenseSource.set("<owner>/tree-sitter-<lang> (MIT)")
   }
   ```
4. **Register in `settings.gradle.kts`**:
   ```kotlin
   include(":languages:<lang>")
   ```
5. **Implement** `src/commonMain/kotlin/io/github/mataku/compose/highlight/<lang>/<Lang>Language.kt`:
   ```kotlin
   val <Lang>Language: Language by lazy {
       kTreeSitterLanguage(TsLanguage(TreeSitter<Lang>.language()), HIGHLIGHTS_QUERY)
   }
   ```
6. **Add JVM golden tests** in `src/jvmTest/kotlin/.../` (loads the host CMake `.dylib`/`.so` automatically).
7. **Run** `./gradlew :languages:<lang>:jvmTest` and `./gradlew :composeApp:assembleDebug`.

## Running tests

```bash
./gradlew :core:jvmTest :languages:kotlin:jvmTest :languages:swift:jvmTest
```

JVM tests depend on `buildHostCMake`, which compiles the parser into a host shared library and is automatically wired into `tasks.named<Test>("jvmTest")`.

`./gradlew :core:commonTest` runs the pure-Kotlin tests (theme resolution, UTF-8 indexing).

## Publishing

```bash
./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false --no-configuration-cache
bash scripts/verify-notice.sh
```

`scripts/verify-notice.sh` walks each language artifact (Android AAR + JVM JAR) and asserts that `META-INF/NOTICE` is present (descending into `classes.jar` for AARs).

CI (`.github/workflows/build.yml`) runs JVM tests, the Android demo build, and NOTICE verification on every PR / push to main / develop. `.github/workflows/publish.yml` runs `publishToMavenCentral` on every `v*` tag push using vanniktech with in-memory signing keys from repository secrets.

### Local Maven Central publishing

For ad-hoc publishes (e.g. cutting a SNAPSHOT for downstream verification) `settings.gradle.kts` loads the following keys from `local.properties` (gitignored) and exposes them as gradle properties:

- `mavenCentralUsername` / `mavenCentralPassword` — Central Portal user token
- `signingInMemoryKey` / `signingInMemoryKeyId` / `signingInMemoryKeyPassword` — GPG signing material (the key may be base64-encoded for single-line storage)

Environment variables (`ORG_GRADLE_PROJECT_*`) take precedence over `local.properties`, so CI continues to work unchanged.

### SNAPSHOT vs release

- Set `VERSION_NAME=0.1.0-SNAPSHOT` in `gradle.properties` and run `./gradlew publishToMavenCentral --no-configuration-cache` to push to Central Portal's snapshot repository (`https://central.sonatype.com/repository/maven-snapshots/`). Snapshots are auto-published with no manual gate.
- For a release, set `VERSION_NAME=0.1.0`, push a `v0.1.0` tag to fire `publish.yml`, then click **Publish** on the Central Portal Deployments page (the workflow uses `publishToMavenCentral`, which uploads to staging without auto-releasing).

## Platform notes

### Android
- Uses `externalNativeBuild.cmake` with the generated `CMakeLists.txt`.
- NDK pinned in the plugin (`ndkVersion = "26.3.11579264"`).
- ABI filters: `x86_64`, `arm64-v8a`, `armeabi-v7a`.

### JVM / Desktop
- At runtime the host OS shared library (`libktreesitter-<lang>.{dylib,so,dll}`) must be on `java.library.path`.
- `jvmTest` configures this via `systemProperty("java.library.path", ...)`.
- For Compose Desktop consumers, ship the host library alongside the JAR or document the requirement.

## Tree-sitter ABI version pinning

`libs.versions.toml` pins `treesitterAbi = "14"` because `ktreesitter 0.24.1` only supports ABI 13–14. Modern `tree-sitter-cli` (0.25+) defaults to ABI 15, so the plugin explicitly passes `--abi=14` when generating the parser. Bump in lockstep with `ktreesitter` when it adds ABI 15 support.

## Key dependencies

| Dependency           | Version         | Purpose                            |
|----------------------|-----------------|------------------------------------|
| Kotlin               | 2.3.20          | KMP compiler                       |
| Compose Multiplatform| 1.10.3          | UI framework                       |
| AGP                  | 8.13.2          | Android build                      |
| ktreesitter          | 0.24.1          | Tree-sitter Kotlin bindings (JNI)  |
| material3            | 1.10.0-alpha05  | Compose Material3                  |
| vanniktech publish   | 0.34.0          | Maven Central publishing (CENTRAL_PORTAL host, supports snapshots) |

## Public API usage example

```kotlin
import io.github.mataku.compose.highlight.core.SyntaxHighlightedText
import io.github.mataku.compose.highlight.core.SyntaxTheme
import io.github.mataku.compose.highlight.kotlin.KotlinLanguage

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

## License notes

Each language module bundles a `tree-sitter-*` grammar submodule. Ensure `licenseSpdx` and `licenseSource` in `composeHighlightLanguage` reflect the upstream grammar license. Currently all bundled grammars are MIT licensed.
