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

Build internals (plugin DSL, generated tasks, KMP source-set layout, NDK/CMake setup, ABI-14 pin trap, platform notes): see [`docs/build-logic.md`](docs/build-logic.md). Dependency versions live in `gradle/libs.versions.toml`.

## Coding principles

- **Separation of concerns** — keep distinct responsibilities in distinct units.
- **Separate state from logic** — keep pure logic free of state; isolate state in its owning boundary.
- **Readability and maintainability first** — pick the simplest design that satisfies the requirement; resist abstractions added for hypothetical reuse.

## When unclear, ask

If a task's requirements, scope, dependencies, or approach are ambiguous — or the plan as written cannot be followed without modification (a missing dependency, an API that doesn't behave as the plan assumed, an out-of-scope file that needs touching) — **stop and ask before proceeding.**

Surface what you observed, propose 1–2 options with trade-offs, and wait for direction. Silent assumptions are the most expensive failure mode: they pass review because they look like decisions, but the underlying judgement was never validated.

This applies equally to direct work, to subagents executing plan tasks, and to reports back to the controller — be honest about anything that drifted from original instructions.

## Adding a new language

End-to-end workflow lives in `.claude/skills/add-tree-sitter-language/SKILL.md`. The skill covers grammar selection (with the ABI-14 pin trap), submodule add, Gradle wiring, the `Language` object, the `Languages.<Name>` extension forwarder, golden tests, demo wiring, and CI updates. Invoke it explicitly via slash command rather than reproducing the steps here.

## Running tests

```bash
./gradlew jvmTest -x :benchmark:jvmTest
```

JVM tests depend on `buildHostCMake`, which compiles the parser into a host shared library and is automatically wired into `tasks.named<Test>("jvmTest")`. Benchmarks (`:benchmark`) are intentionally excluded from this default and should be run separately when needed.

After running tests, run `./gradlew spotlessApply` to keep the working tree formatted (Spotless ktlint is wired via the `compose-syntax-highlight-spotless` build-logic plugin) and `./gradlew apiCheck` for Binary compatibility validation.

## Publishing

See [`docs/publishing.md`](docs/publishing.md) for Maven Central publishing, local SNAPSHOT cuts, signing-key setup, and NOTICE verification.

## Documentation paths

- Design specs: `docs/specs/YYYY-MM-DD-<topic>-design.md`
- Implementation plans: `docs/plans/YYYY-MM-DD-<topic>-plan.md`

Use these paths instead of any tool/skill default (e.g. `docs/superpowers/specs/`).

## Public API usage

See `README.md` for installation, theme overrides, and `SyntaxHighlightedText` usage examples.

## License notes

Each language module bundles a `tree-sitter-*` grammar submodule. Ensure `licenseSpdx` and `licenseSource` in `composeSyntaxHighlightLanguage` reflect the upstream grammar license. Currently all bundled grammars are MIT licensed.
