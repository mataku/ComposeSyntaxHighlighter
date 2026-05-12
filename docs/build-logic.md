# Build logic

Versions for every dependency referenced below live in `gradle/libs.versions.toml` — that's the source of truth, this doc deliberately doesn't restate them.

## `compose-syntax-highlight-language` plugin

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

## Source set hierarchy

Both `:core` and language modules use the default KMP hierarchy with no custom intermediate sets:

```
commonMain  (all production code)
├── androidMain  (no extra source)
└── jvmMain      (no extra source)
```

`commonTest` runs across `androidUnitTest` and `jvmTest`. Parser-using golden tests live in `<module>/src/jvmTest/` so they can run on host JVM with `java.library.path` pointing at the host CMake output.

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

`libs.versions.toml` pins `treesitterAbi = "15"`. `ktreesitter` 0.25.0 bundles `tree-sitter` 0.25.10, whose `api.h` reports `TREE_SITTER_LANGUAGE_VERSION = 15` and `TREE_SITTER_MIN_COMPATIBLE_LANGUAGE_VERSION = 13`, so ABI 13, 14, and 15 are all loadable at runtime. The plugin only consults `treesitterAbi` when invoking `tree-sitter generate --abi=<N>`, which currently fires only for `tree-sitter-swift` (its upstream gitignores `src/parser.c`). Bump in lockstep with `ktreesitter` when its supported ABI window moves.

When picking a submodule pin for a new (or updated) language module, the bundled `src/parser.c` must fall within the supported ABI window. `generateParserSource` skips regeneration whenever `parser.c` already exists — the build trusts the checked-in upstream artifact. If a grammar gitignores `parser.c` upstream (e.g. `tree-sitter-swift`), the plugin generates it locally at the configured ABI.
