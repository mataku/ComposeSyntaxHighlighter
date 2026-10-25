# ComposeSyntaxHighlighter — Agent Guide

## Overview

ComposeSyntaxHighlighter is a **Kotlin Multiplatform (KMP)** library that produces an `AnnotatedString` from `tree-sitter` query captures and renders it with Compose `Text`.

- **Targets**: Android (published AAR), JVM (published JAR for Compose Desktop interop), iosArm64, iosSimulatorArm64 (iOS framework). Production code lives in `commonMain`; the parser binding uses `expect/actual` (JVM/Android `actual` via `System.loadLibrary`; native `actual` via cinterop).
- **Languages**: Kotlin, Swift, Ruby, Rust, Python, Go, Java, JavaScript, TypeScript (with TSX), Markdown (more can be added as separate modules).
- **UI target**: Material3 only (`minSdk = 26`).
- **Browser**: out of scope — use a JS-side highlighter (highlight.js, Shiki) for web apps.

## Modules

```
root
├── build-logic/                # Custom Gradle plugin for language modules
├── samples/
│   ├── composeApp/             # Demo composables (KMP shared library: Android + iOS framework)
│   ├── androidApp/             # Android demo APK (com.android.application, consumes :samples:composeApp)
│   └── iosApp/                 # Swift Xcode project consuming the ComposeApp framework
├── core-api/                   # Cross-module SPI (Language, Languages)
├── core/                       # Highlighter engine, IncrementalHighlighter, SyntaxTheme, themes
├── material3/                  # SyntaxHighlightedText (read-only viewer)
├── material3-text-field/       # SyntaxHighlightedTextField + rememberSyntaxHighlightedString (editable)
├── languages/<name>/           # Per-language grammar + Language value
├── benchmarks/
│   ├── jvm/                    # JVM benchmarks (excluded from default jvmTest)
│   └── android/                # Android instrumented benchmarks (FTL target)
└── gradle/libs.versions.toml   # Version catalog
```

Per-module Maven coordinates and public API: see [`docs/modules.md`](docs/modules.md). Build internals (plugin DSL, NDK/CMake, ABI-14 pin trap): see [`docs/build-logic.md`](docs/build-logic.md).

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
./gradlew jvmTest -x :benchmarks:jvm:jvmTest
```

JVM tests depend on `buildHostCMake`, which compiles the parser into a host shared library, and on `stageJvmNativeLibrary`, which places it under `jvmMain` resources at `lib/<os>/<arch>/`; the generated loader picks it up from the classpath, so no `java.library.path` is set. Benchmarks (`:benchmarks:jvm`) are intentionally excluded from this default and should be run separately when needed.

After running tests, run `./gradlew spotlessApply` to keep the working tree formatted (Spotless ktlint is wired via the `compose-syntax-highlight-spotless` build-logic plugin) and `./gradlew apiCheck` for binary-compatibility validation.

## `:languages:swift` regenerates its own `parser.c`

`tree-sitter-swift` gitignores `src/parser.c` upstream, so the convention plugin generates it at `treesitterAbi` — but only when the file is missing (`onlyIf { !parserC.exists() }`); a local copy is never regenerated, even after `treesitterAbi` changes. For swift's ABI, trust `gradle/libs.versions.toml`, not the local file (other grammars ship `parser.c`, so their on-disk file is authoritative). To refresh: `rm languages/swift/tree-sitter-swift/src/parser.c && ./gradlew :languages:swift:generateParserSource`. See `docs/build-logic.md` § Tree-sitter ABI.

## Publishing

See [`docs/publishing.md`](docs/publishing.md) for Maven Central publishing, local SNAPSHOT cuts, signing-key setup, and NOTICE verification.

## Documentation paths

- Design specs: `docs/specs/YYYY-MM-DD-<topic>-design.md`
- Implementation plans: `docs/plans/YYYY-MM-DD-<topic>-plan.md`

Use these paths instead of any tool/skill default (e.g. `docs/superpowers/specs/`).

## Public API usage

See `README.md` for installation, theme overrides, and `SyntaxHighlightedText` / `SyntaxHighlightedTextField` usage examples.

## License notes

Each language module bundles a `tree-sitter-*` grammar submodule. Ensure `licenseSpdx` and `licenseSource` in `composeSyntaxHighlightLanguage` reflect the upstream grammar license. Currently all bundled grammars are MIT licensed.
