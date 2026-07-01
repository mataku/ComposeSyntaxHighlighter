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

JVM tests depend on `buildHostCMake`, which compiles the parser into a host shared library and is automatically wired into `tasks.named<Test>("jvmTest")`. Benchmarks (`:benchmarks:jvm`) are intentionally excluded from this default and should be run separately when needed.

After running tests, run `./gradlew spotlessApply` to keep the working tree formatted (Spotless ktlint is wired via the `compose-syntax-highlight-spotless` build-logic plugin) and `./gradlew apiCheck` for binary-compatibility validation.

## `:languages:swift` regenerates its own `parser.c`

`tree-sitter-swift` upstream gitignores `src/parser.c` (`/src/*` except `scanner.c` and `*.json`). Swift is the only language module in this repo that has this property today; every other grammar commits a checked-in `parser.c`. For swift, the convention plugin runs `tree-sitter generate --abi=<treesitterAbi>` on demand to produce the file locally.

The `:languages:swift:generateParserSource` task is gated by `onlyIf { !parserC.exists() }`, so a `parser.c` that already exists locally is never regenerated — even after `treesitterAbi` changes in the catalog, or after the previous file was produced under a different ABI. The local file sticks.

Symptom: the published swift POM property `tree-sitter-abi`, or a local `grep '#define LANGUAGE_VERSION' languages/swift/tree-sitter-swift/src/parser.c`, reports an ABI that does not match `gradle/libs.versions.toml`'s `treesitterAbi`. The repository is fine; the local file is stale. CI starts from a fresh checkout where the file does not exist, so CI builds always emit the catalog's ABI.

Fix: delete the file and let the plugin regenerate it on the next build.

```bash
rm languages/swift/tree-sitter-swift/src/parser.c
./gradlew :languages:swift:generateParserSource
```

When verifying ABI compliance from a local checkout, do not trust `ls`/`grep` of swift's `parser.c` — the source of truth is `gradle/libs.versions.toml`'s `treesitterAbi`. (Other language modules ship `parser.c` from upstream, so for those the on-disk file is authoritative.)

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
