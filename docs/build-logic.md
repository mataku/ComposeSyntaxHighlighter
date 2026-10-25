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
1. **`generateParserSource`**: runs `tree-sitter generate --abi=<treesitterAbi>` inside the grammar submodule (skipped if `parser.c` already exists).
2. **`generateGrammarFiles`** (from the ktreesitter plugin): generates the JNI binding object (`TreeSitter<Lang>`).
3. **`generateHighlightsQuery`**: embeds `queries/highlights.scm` as `internal const val HIGHLIGHTS_QUERY`.
4. **`generateNotice`**: writes `META-INF/NOTICE` from `NOTICE.tpl` plus a per-grammar entry, wired into both KMP `commonMain.resources` and AGP `main.resources`.
5. **CMake configuration**: writes `CMakeLists.txt` (Android NDK) and `host-cmake/CMakeLists.txt` (host JVM build).
6. **`buildHostCMake`**: builds the parser `.dylib`/`.so`/`.dll` as `Release`; on macOS as an `arm64;x86_64` universal binary with deployment target `26.0`.
7. **`stageJvmNativeLibrary`**: thins the universal binary into `lib/macos/aarch64/` and `lib/macos/x64/` and ad-hoc signs each slice (other hosts copy the library to `lib/<os>/<arch>/`) under `build/generated/jvmNatives/`, which is registered as `jvmMain` resources so `jvmJar`, `publishToMavenLocal` and the `jvmTest` runtime classpath carry it. With `-PprebuiltJvmNativesDir=<dir>` it also copies this module's libraries from `<dir>/lib/<os>/<arch>/` (built on other hosts; relative paths resolve against the repository root), so one macOS build can ship the Linux libraries too.
8. **vanniktech maven-publish**: sets POM coordinates `compose-syntax-highlight-<name>` and POM name/description.
9. **AGP packaging**: drops `META-INF/NOTICE` from the default excludes and pickFirsts it so the AAR's classes.jar retains the file.

## Source set hierarchy

Both `:core` and language modules use the default KMP hierarchy with no custom intermediate sets:

```
commonMain  (all production code)
├── androidMain  (no extra source)
└── jvmMain      (no extra source)
```

`commonTest` runs across `androidUnitTest` and `jvmTest`. Parser-using golden tests live in `<module>/src/jvmTest/` so they can run on host JVM, loading the grammar library from the staged `jvmMain` resources.

## Platform notes

### Android
- Uses `externalNativeBuild.cmake` with the generated `CMakeLists.txt`.
- NDK pinned in the plugin (`ndkVersion = "26.3.11579264"`).
- ABI filters: `x86_64`, `arm64-v8a`, `armeabi-v7a`.

### JVM / Desktop
- The generated loader tries `System.loadLibrary("ktreesitter-<lang>")` first, then falls back to the classpath resource `lib/<os>/<arch>/<prefix>ktreesitter-<lang>.<ext>`, extracted to a temp file and loaded with `System.load`.
- Published JVM jars include the macOS and Linux `aarch64` and `x64` libraries; `jvmTest` and Compose Desktop apps on those hosts load them with no `java.library.path` configuration.
- The Linux libraries are built by `.github/workflows/linux-jvm-natives.yml` on pinned `ubuntu-24.04` / `ubuntu-24.04-arm` runners and passed to the macOS publish job via `-PprebuiltJvmNativesDir`. `scripts/verify-linux-natives.sh` fails if one needs a glibc newer than 2.17, the floor already set by ktreesitter-jvm's own `libktreesitter.so`.
- Windows consumers supply `ktreesitter-<lang>.dll` on `java.library.path`.

## Tree-sitter ABI

Three related concepts that should not be conflated:

- **Parser ABI** — the ABI baked into each `parser.c`. Recorded per
  language module as the POM property `tree-sitter-abi` by the convention
  plugin (and `tree-sitter-abi-<grammarName>` for multi-grammar modules).
- **Catalog `treesitterAbi`** — the value passed as `--abi=N` to
  `tree-sitter generate`. Affects only grammars whose upstream gitignores
  `parser.c` (currently `tree-sitter-swift`); grammars that commit a generated
  `parser.c` use the ABI of that committed file regardless of this value.
- **Accept window** — the `[MIN_COMPATIBLE_LANGUAGE_VERSION ..
  LANGUAGE_VERSION]` range that the currently-pinned `ktreesitter` accepts at
  runtime. We do not duplicate this window in our catalog; the consumer-side
  build-time enforcement is delegated to the strict version mechanism on
  `:core-api` (see below).

When picking a submodule pin for a new or updated language module, ensure the
bundled `parser.c` falls inside the current accept window of the catalog-pinned
ktreesitter. `generateParserSource` skips regeneration whenever `parser.c`
already exists; the build trusts the checked-in upstream artefact. If a grammar
gitignores `parser.c` upstream, the plugin generates it locally at the
configured `treesitterAbi`.

## Strict version constraint on `compose-syntax-highlight-api`

The convention plugin injects a `strictly` constraint on each language module's
dependency on `:core-api`, derived from `coreApiCompatibleRange` in the catalog.
The same constraint is set in `core/build.gradle.kts`. Together they cause
Gradle to fail at consumer dependency resolution if a too-new core stack and an
older language module land on the same classpath.

Local development continues to use `project(":core-api")` thanks to a
`dependencySubstitution` rule in `settings.gradle.kts` that rewrites
`io.github.mataku:compose-syntax-highlight-api` back to the project. The
substitution affects resolution but not publication, so published metadata
records the coordinate and the strict range as designed.

The strict range moves whenever ktreesitter narrows its accept window — we
follow ktreesitter's range semantics and major-bump `:core-api` in lockstep.
