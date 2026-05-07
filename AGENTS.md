# ComposeSyntaxHighlighter — Agent Guide

## Overview

ComposeSyntaxHighlighter is a **Kotlin Multiplatform (KMP)** library that provides syntax-highlighted `Text` composables for Jetpack Compose / Compose Multiplatform.

- **Rendering**: Produces an `AnnotatedString` from `tree-sitter` query captures and renders it with Compose `Text`.
- **Targets**: Android (published AAR) and JVM (published JAR for Compose Desktop interop).
- **Languages**: Kotlin, Swift, Ruby, Rust, Python, Go, Java (more can be added as separate modules).
- **UI target**: Material3 only (`minSdk = 26`).
- **Browser**: out of scope — use a JS-side highlighter (highlight.js, Shiki) for web apps.

## Module Structure

```
root
├── build-logic/                    # Custom Gradle plugin for language modules
├── composeApp/                     # Demo app (Android only)
├── core-api/                       # Cross-module SPI (Language, Languages)
├── core/                           # Highlighter engine, SyntaxTheme, builtin themes
├── material3/                      # Material3 binding (SyntaxHighlightedText)
├── languages/
│   ├── kotlin/                     # Kotlin language support
│   ├── swift/                      # Swift language support
│   ├── ruby/                       # Ruby language support
│   ├── rust/                       # Rust language support
│   ├── python/                     # Python language support
│   ├── go/                         # Go language support
│   └── java/                       # Java language support
└── gradle/libs.versions.toml       # Version catalog
```

### `:core`

Maven coordinates: `io.github.mataku:compose-syntax-highlight-core:<version>`.

Public API in `commonMain` (no expect/actual — every target is JVM-based):
- `SyntaxTheme(baseStyle, background, keyword, function, ..., extras)` — typed `SpanStyle?` fields per tree-sitter capture (the 14 used by every built-in theme), plus `extras: Map<String, SpanStyle>` for grammar-specific captures. Provides `SyntaxTheme.DarkDefault` / `SyntaxTheme.LightDefault`.
- `LocalSyntaxTheme` — composition local that defaults to `SyntaxTheme.DarkDefault`.
- `Language` — concrete class wrapping a KTreeSitter `Language` + a highlights query (re-exported from `:core-api`).
- `kTreeSitterLanguage(parser, highlightsQuery)` — factory used by language modules (re-exported from `:core-api`).
- `highlight(code, language, theme)` / `rememberHighlightedString(...)` — the underlying functions.

Implementation files live under `core/src/commonMain/kotlin/io/github/mataku/compose/highlight/core/`.

### `:material3`

Maven coordinates: `io.github.mataku:compose-syntax-highlight-material3:<version>`.

Public API in `commonMain`:
- `SyntaxHighlightedText(code, language, theme, style, modifier)` — composable that renders the highlighted code via `androidx.compose.material3.Text`, pulling the default `style` from `LocalTextStyle` (Material3) with `FontFamily.Monospace`.

Implementation file: `material3/src/commonMain/kotlin/io/github/mataku/compose/highlight/material3/SyntaxHighlightedText.kt`.

Depends on `:core` for `rememberHighlightedString`, `SyntaxTheme`, and `LocalSyntaxTheme`. Future Material releases (e.g. M4) ship as sibling modules without touching `:core`.

### `:languages:<name>`

Maven coordinates: `io.github.mataku:compose-syntax-highlight-<name>:<version>`. Each language module:

- Applies the custom `compose-syntax-highlight-language` convention plugin.
- Vendors a `tree-sitter-<lang>` grammar submodule.
- Exposes a single `val <Lang>Language: Language` from `commonMain`.

## Build System

### `build-logic` — `compose-syntax-highlight-language` plugin

Located at `build-logic/src/main/kotlin/compose-syntax-highlight-language.gradle.kts`.

Per-language configuration DSL (`ComposeSyntaxHighlightLanguageExtension`):

```kotlin
composeSyntaxHighlightLanguage {
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
7. **vanniktech maven-publish**: sets POM coordinates `compose-syntax-highlight-<name>` and POM name/description.
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

End-to-end workflow lives in `.claude/skills/add-tree-sitter-language/SKILL.md`. The skill covers grammar selection (with the ABI-14 pin trap), submodule add, Gradle wiring, the `Language` object, the `Languages.<name>` extension forwarder, golden tests, demo wiring, and CI updates. Invoke it explicitly via slash command rather than reproducing the steps here.

## Running tests

```bash
./gradlew :core:jvmTest :languages:kotlin:jvmTest :languages:swift:jvmTest
```

JVM tests depend on `buildHostCMake`, which compiles the parser into a host shared library and is automatically wired into `tasks.named<Test>("jvmTest")`.

`./gradlew :core:commonTest` runs the pure-Kotlin tests (theme resolution, UTF-8 indexing).

After running tests, run `./gradlew spotlessApply` to keep the working tree formatted (Spotless ktlint is wired via the `compose-syntax-highlight-spotless` build-logic plugin).

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

When picking a submodule pin for a new (or updated) language module, the bundled `src/parser.c` MUST already be at ABI 14 — `generateParserSource` only regenerates when `parser.c` is missing or `grammar.js` mtime is newer, so an ABI 15 parser.c bundled upstream will reach `Language(...)` and throw `IllegalArgumentException: Incompatible language version 15`. Concrete examples: `tree-sitter/tree-sitter-rust` main currently bundles ABI 15 — pin to `v0.23.3` instead, which ships ABI 14.

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
        keyword = SpanStyle(color = Color.Cyan),
        string = SpanStyle(color = Color.Green),
        comment = SpanStyle(color = Color.Gray),
    )
    SyntaxHighlightedText(
        code = code,
        language = KotlinLanguage,
        theme = theme,
    )
}
```

## Documentation paths

- Design specs: `docs/specs/YYYY-MM-DD-<topic>-design.md`
- Implementation plans: `docs/plans/YYYY-MM-DD-<topic>-plan.md`

Use these paths instead of any tool/skill default (e.g. `docs/superpowers/specs/`).

## License notes

Each language module bundles a `tree-sitter-*` grammar submodule. Ensure `licenseSpdx` and `licenseSource` in `composeSyntaxHighlightLanguage` reflect the upstream grammar license. Currently all bundled grammars are MIT licensed.
