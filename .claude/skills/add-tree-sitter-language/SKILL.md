---
name: add-tree-sitter-language
description: End-to-end workflow for adding a new tree-sitter-backed language module (grammar selection with ABI accept-window check, submodule add, Gradle wiring, Language object, golden tests, demo wiring, CI updates). Run only when invoked explicitly via slash command.
disable-model-invocation: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - Bash(git submodule:*)
  - Bash(git -C *)
  - Bash(./gradlew:*)
  - Bash(bash scripts/verify-notice.sh:*)
  - Bash(grep:*)
  - Bash(ls:*)
  - Bash(unzip -p:*)
  - Skill(git-commit)
  - Skill(superpowers:brainstorming)
  - Skill(superpowers:writing-plans)
  - Skill(superpowers:executing-plans)
  - Skill(superpowers:test-driven-development)
  - Skill(superpowers:verification-before-completion)
---

# Adding a new tree-sitter language module

This skill encodes the workflow for adding a new `:languages:<lang>` module.

Canonical worked examples already on `develop`:

- **Single grammar**: commits `9bbe87d`..`c54e2b3` (Ruby + Rust).
- **Multi-grammar** (one module bundling 2+ grammars): the markdown module (commit history under `languages/markdown/`) and the typescript module (which also depends on the javascript module for shared queries).

The workflow is rigid — follow the order. Most steps are obvious, but several have non-obvious traps that the order avoids.

## 1. Pick the upstream grammar and the right pin

**Search order for grammar repos:**

1. `https://github.com/tree-sitter` — the official org maintains canonical grammars for many languages (`tree-sitter-ruby`, `tree-sitter-rust`, `tree-sitter-python`, `tree-sitter-go`, `tree-sitter-javascript`, `tree-sitter-typescript`, …). Prefer these when they exist.
2. `https://github.com/tree-sitter-grammars` — community-maintained grammars curated by the tree-sitter project. Use when (1) has nothing.
3. Well-known forks (e.g. `fwcd/tree-sitter-kotlin`, `alex-pinkus/tree-sitter-swift`) — only when neither org has a usable grammar. Document why in the commit message.

**License:** All grammars in the repo today are MIT. If you pick something non-MIT, stop and ask the user before going further — it changes how the NOTICE bundling looks and the `licenseSpdx` / `licenseSource` DSL values.

**Pin selection — keep the bundled parser.c inside the accept window:**

`gradle/libs.versions.toml` is the single source of truth: it pins `treesitterAbi` (currently `15`) and the `ktreesitter` loader, whose accept window for a parser's `LANGUAGE_VERSION` is currently `13..15` on every target (including Kotlin/Native). Re-read the catalog rather than trusting these numbers — they move when ktreesitter is bumped. `generateParserSource` (in `build-logic/src/main/kotlin/compose-syntax-highlight-language.gradle.kts`) only regenerates `src/parser.c` when it is missing — it does **not** detect ABI mismatch. An upstream parser.c whose ABI falls outside the window reaches `Language(...)` and throws `IllegalArgumentException: Incompatible language version <N>` at runtime.

Verification happens *after* `git submodule add` (step 2) but *before* you record the gitlink in a commit. Inspect the default-checked-out parser.c: if its `LANGUAGE_VERSION` is inside `13..15`, the pin is fine — prefer the newest such tag so you ride the latest grammar. If it is outside the window (e.g. a future ABI 16), walk back tags with `git -C <submodule> checkout <older-tag>` until the bundled parser.c reads a `LANGUAGE_VERSION` within the window, then commit *that* gitlink. As of this writing every released upstream grammar ships ABI ≤ 15, so default HEAD is normally fine. Some bundled modules sit at ABI 15 and others remain at ABI 14 purely because their upstream has not yet cut an ABI-15 release — ABI 14 is in-window and perfectly fine; do not downgrade a working pin to chase a particular ABI.

If the grammar ships *no* `parser.c` at all (e.g. `tree-sitter-swift` at the pinned commit), that's fine — `generateParserSource`'s `!parserC.exists()` branch regenerates it at the configured `treesitterAbi` (currently 15) on every fresh checkout.

If a grammar's only in-window tags are unacceptably old, stop and tell the user — widening the accept window (bumping `ktreesitter`, and `treesitterAbi` for regenerated grammars) is a separate, repo-wide change that has to land first.

## 2. Add the submodule, verify ABI, and create the module skeleton

```bash
git submodule add https://github.com/<owner>/tree-sitter-<lang>.git languages/<lang>/tree-sitter-<lang>
```

`git submodule add` defaults to upstream main HEAD. Inspect the bundled parser.c immediately:

```bash
grep '#define LANGUAGE_VERSION' languages/<lang>/tree-sitter-<lang>/src/parser.c
```

If it reports a value in `13..15`, you're done with verification. If it reports something outside the window (e.g. `16`), list available tags and pick the newest tag whose tree contains parser.c inside `13..15`:

```bash
git -C languages/<lang>/tree-sitter-<lang> tag --sort=-version:refname
git -C languages/<lang>/tree-sitter-<lang> show <candidate-tag>:src/parser.c | grep LANGUAGE_VERSION
git -C languages/<lang>/tree-sitter-<lang> checkout <chosen-tag>
git add languages/<lang>/tree-sitter-<lang>   # restage the gitlink at the new commit
```

If the `grep` for the existing parser.c returns no output, that means the file is missing entirely (the case for `tree-sitter-swift`). That's fine — `generateParserSource` will produce it at build time with the configured ABI.

Verify the layout. The convention plugin requires `src/parser.c` (or none, if you'll regenerate), `src/scanner.c` if the grammar uses an external scanner, and `queries/highlights.scm`:

```bash
ls languages/<lang>/tree-sitter-<lang>/grammar.js \
   languages/<lang>/tree-sitter-<lang>/queries/highlights.scm \
   languages/<lang>/tree-sitter-<lang>/src/parser.c \
   languages/<lang>/tree-sitter-<lang>/src/scanner.c
```

`ls` exits non-zero if any path is missing; that's the signal. If `src/scanner.c` is the only missing file, drop it from `sources` in the build script (most grammars need it; check the grammar's README). If `src/parser.c` is missing, that's fine — `generateParserSource` will materialize it on first build with the configured ABI.

Create `languages/<lang>/build.gradle.kts`:

```kotlin
plugins {
  id("compose-syntax-highlight-language")
}

composeSyntaxHighlightLanguage {
  languageName.set("<lang>")
  grammars {
    create("<lang>") {
      submodulePath.set("tree-sitter-<lang>")
      parserClassName.set("TreeSitter<Lang>")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("queries/highlights.scm"))
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("<owner>/tree-sitter-<lang> (MIT)")
  licenseCopyright.set("Copyright (c) <year> <holder>")
}
```

The grammar's `name` (here `"<lang>"`) **must equal** `languageName` for single-grammar modules. Both are used in the package path `io.github.mataku.compose.highlight.<name>` and in the native library / header naming, and the convention plugin only matches them up when they're identical.

`licenseCopyright` is **required** by the convention plugin — `generateNotice` fails the build if it is not set. Copy the upstream copyright line verbatim from the submodule's LICENSE file (e.g. `grep -m1 'Copyright' languages/<lang>/tree-sitter-<lang>/LICENSE`); do not paraphrase, abbreviate, or substitute a year range. The string is reproduced in the published `META-INF/NOTICE` to satisfy MIT's requirement that the original copyright notice travel with redistributed binaries.

For modules deriving from multiple upstream repos (e.g. typescript bundles tree-sitter-typescript parser code plus tree-sitter-javascript queries), semicolon-separate the copyrights with a parenthesised repo tag, mirroring the `licenseSource` convention:

```kotlin
licenseCopyright.set("Copyright (c) 2017 Max Brunsfeld (tree-sitter-typescript); Copyright (c) 2014 Max Brunsfeld (tree-sitter-javascript)")
```

Append to `settings.gradle.kts` after the existing `:languages:<...>` includes:

```kotlin
include(":languages:<lang>")
```

**Exclude the generated parser object from the public ABI.** The ktreesitter plugin generates `TreeSitter<Lang>` (exposing `fun language(): Any`) into `io.github.mataku.compose.highlight.<name>.internal`. It is `public`, carries no `@InternalSyntaxHighlightApi`, and is consumed only from within its own module — so unless it is excluded, BCV records it in the module's jvm **and** klib dumps and the 1.0 stability guarantee pins that untyped accessor forever. The generator belongs to ktreesitter, so its visibility cannot be changed here; the exclusion is a BCV setting.

Add the new package to `ignoredPackages` in the root `build.gradle.kts`:

```kotlin
apiValidation {
  ignoredPackages += listOf(
    …,
    "<name>",          // one entry per grammar key, not per module
  ).map { "io.github.mataku.compose.highlight.$it.internal" }
}
```

For multi-grammar modules add **one entry per grammar key**, not one per module — the package follows the grammar's `create("<name>")` key. That is why `typescript` contributes both `typescript` and `tsx`, and `markdown` contributes both `markdown` and `markdownInline`.

Verify after the first `apiDump` (step 7) that nothing leaked:

```bash
grep -rl 'TreeSitter' --include='*.api' --include='*.klib.api' languages/<lang>/api   # must print nothing
```

Run `./gradlew :languages:<lang>:tasks --quiet`. The plugin's `afterEvaluate` writes `languages/<lang>/CMakeLists.txt` and `languages/<lang>/host-cmake/CMakeLists.txt`. **These files are gitignored** (`languages/*/CMakeLists.txt` and `languages/*/host-cmake/`) — the plugin regenerates them on every configuration. Do **not** add them to the commit.

Commit (one atomic commit per the repo's git-commit skill):

- `.gitmodules`
- `languages/<lang>/tree-sitter-<lang>` (the gitlink)
- `languages/<lang>/build.gradle.kts`
- `settings.gradle.kts`
- `build.gradle.kts` (the `ignoredPackages` entry)

## 3. Implement `Languages.<Lang>` with golden tests (TDD)

Write the failing test first. Use the template in `references/highlight-test-template.md` and substitute language-specific keywords/comment syntax. Run `./gradlew :languages:<lang>:jvmTest` and confirm the failure is an unresolved reference on `Languages.<Lang>` (compile failure) — that proves the test exercises the right code path.

The test goes in `languages/<lang>/src/sharedTest/`, **not** `src/jvmTest/`. The convention plugin wires `sharedTest` to the `jvmTest`, `iosArm64Test`, and `iosSimulatorArm64Test` compilations (and deliberately not to `androidUnitTest`), so a single test file covers JVM and iOS. Keep it portable — `kotlin.test` + Compose Multiplatform types, no JVM-only APIs.

Then create `languages/<lang>/src/commonMain/kotlin/io/github/mataku/compose/highlight/<lang>/<Lang>Language.kt`:

```kotlin
package io.github.mataku.compose.highlight.<lang>

import io.github.mataku.compose.highlight.api.Language
import io.github.mataku.compose.highlight.api.Languages
import io.github.mataku.compose.highlight.api.kTreeSitterLanguage
import io.github.mataku.compose.highlight.<lang>.internal.TreeSitter<Lang>
import io.github.treesitter.ktreesitter.Language as TsLanguage

private val instance: Language by lazy {
  kTreeSitterLanguage(TsLanguage(TreeSitter<Lang>.language()), HIGHLIGHTS_QUERY)
}

val Languages.<Lang>: Language get() = instance
```

The public surface is the `Languages.<Lang>` extension property only. The `private val instance` is just a backing holder for the lazy parser/query construction — extension properties cannot have backing fields, so the lazy delegate has to live in a sibling private val.

Re-run `./gradlew :languages:<lang>:jvmTest`. Expect all 5 tests to pass.

**If a test fails at runtime** (not at compile time), inspect the actual spans printed in the failure message before changing anything. Common shapes:

- *Capture-name mismatch*: upstream `highlights.scm` uses a name your test theme doesn't map. Example: Rust integer literals are tagged `@constant.builtin`, not `@number`. Keep the test logic, add the upstream capture key to the test theme. `SyntaxTheme.resolve()` does dotted-prefix fallback, so `constant.builtin` will fall back to `constant` — but the most precise key is the most predictable. `resolve()` also carries a `CAPTURE_ALIASES` table for nvim-treesitter-era names that are exact synonyms of a typed field but share no dotted prefix with it (`escape`, `float`, `conditional`, `repeat`, `include`, `exception`, `parameter`, `character`). If the new grammar emits a *further* such synonym, extend that table rather than papering over it in the test theme — an unmapped top-level capture renders unstyled for every consumer, not just the test.
- *ABI mismatch*: failure says `Incompatible language version <N>. Must be between 13 and 15.` Stop. Go back to step 1 and pick a different submodule pin. Do not patch `build-logic` for this; the project policy is to fix the pin instead.
- *`QueryError$Predicate: ... must be a string literal`*: ktreesitter's predicate parser miscounts arguments for patterns containing **more than one** predicate (`#match? ... #is-not? local`). It accepts single-predicate `(#is-not? local)` (ruby uses this) but fails on multi-predicate combinations (the JS scm hits this for `@variable.builtin` / `@function.builtin`). Still reproducible on the pinned `ktreesitter` (see the version catalog). Workaround: ship a hand-curated `languages/<lang>/queries/highlights.scm` (outside the submodule) and point `queries.set(listOf("../queries/highlights.scm"))` at that — the javascript module is the precedent. Add a header comment documenting the deviation from upstream.

Commit the Language object and the test as one atomic commit.

## 4. Multi-grammar modules (one module, two or more grammars)

If a single submodule ships multiple grammars under one repo — markdown ships `markdown` + `markdown-inline`; tree-sitter-typescript ships `typescript` + `tsx` — use the multi-entry form. Otherwise skip this section.

Each `create("<name>") { … }` describes one grammar, with its own submodule subpath, parser class, sources, queries, and optionally `cSymbol` if the grammar's exported C symbol does not follow `tree_sitter_<name>`:

```kotlin
composeSyntaxHighlightLanguage {
  languageName.set("typescript")
  grammars {
    create("typescript") {
      submodulePath.set("tree-sitter-typescript/typescript")
      parserClassName.set("TreeSitterTypescript")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("../queries/highlights.scm"))
    }
    create("tsx") {
      submodulePath.set("tree-sitter-typescript/tsx")
      parserClassName.set("TreeSitterTsx")
      sources.set(listOf("src/parser.c", "src/scanner.c"))
      queries.set(listOf("../queries/highlights.scm"))
      cSymbol.set("tree_sitter_tsx")
    }
  }
  licenseSpdx.set("MIT")
  licenseSource.set("tree-sitter/tree-sitter-typescript (MIT)")
}
```

Each grammar gets its own `Languages.<Name>` extension property in `src/commonMain/.../<name>/<Name>Language.kt`, just like the single-grammar pattern. The package and Language-object naming follow `<name>` (the grammar key), not `languageName`.

**Primary-grammar trap.** The plugin treats the alphabetically-first grammar as "primary" — that grammar's binding.c and TreeSitter<Class>.kt come from the ktreesitter-plugin; the others are emitted by the convention plugin's secondary path. For the typescript module, `"tsx"` sorts before `"typescript"`, so `tsx` ends up primary. This is harmless in practice (the convention plugin aliases the native library and header names onto the primary's name so cinterop and CMake agree), but it shapes a few internals:

- The native library is `libktreesitter-<languageName>.{so,a}` even though the primary grammar may be named differently. The convention plugin overrides ktreesitter-plugin's `libraryName` default to enforce this.
- The CMake alias header is named after the primary grammar (`tree-sitter-tsx.h` in the typescript module). Secondary binding.c includes the same header. The iOS header and static lib follow the same rule.

You don't have to fight the ordering — just be aware when reading CMakeLists or grammar.def that "primary" means "alphabetically first," not the grammar you'd naively call canonical.

**Cross-module query reuse.** If one module's queries should also reach another (e.g. TypeScript's upstream highlights only cover TS-specific captures; JavaScript queries supply the keywords/strings/numbers/comments fall-through), point `queries.set(listOf(...))` at the sibling module's curated scm via a relative path. The convention plugin reads every entry in the `queries` list and concatenates them with a blank-line separator before embedding into `HIGHLIGHTS_QUERY`, so you can mix files from different submodules and from `languages/<lang>/queries/` (the curated-scm workaround) in one list. The typescript module reads `"../../../javascript/queries/highlights.scm"` followed by `"../queries/highlights.scm"`.

## 5. Wire into the demo (`composeApp`)

`samples/composeApp/build.gradle.kts` — extend the existing `commonMain.dependencies` block, after the last `projects.languages.*` line:

```kotlin
implementation(projects.languages.<lang>)
```

`samples/composeApp/src/commonMain/kotlin/com/mataku/composesyntaxhighlighter/SampleCode.kt` — add a `val <lang>: String` property containing a 15–20 line snippet that exercises the language's distinctive constructs (keywords, strings, comments, numbers, plus 1–2 idiomatic features so the highlight result is visibly different from base text).

`samples/composeApp/src/commonMain/kotlin/com/mataku/composesyntaxhighlighter/HighlighterDemo.kt`:

- Add the import: `import io.github.mataku.compose.highlight.<lang>.<Lang>` (the `Languages.<Lang>` extension property).
- Extend the `DemoLanguage` enum with a new entry.
- Extend the `when (selectedLang)` branch to map the new enum entry to `SampleCode.<lang> to Languages.<Lang>`.

Run `./gradlew :samples:androidApp:assembleDebug` to verify the build (`:samples:composeApp` is the shared KMP library demo; `:samples:androidApp` is the launchable APK consuming it). Commit composeApp changes as one atomic commit.

## 6. Update the NOTICE module list and README license list

The CI workflow (`.github/workflows/build.yml`) runs project-wide `./gradlew jvmTest -x :benchmarks:jvm:jvmTest` and `./gradlew apiCheck`, so there is no hardcoded module list to update there.

**`scripts/verify-notice.sh`** does still have a hardcoded `modules=( … )` array; append the new artifact name:

```bash
modules=(
  …
  compose-syntax-highlight-<lang>
)
```

**`README.md`** — append the new submodule to the bundled-grammar list inside the `## License` section, matching the existing `<owner>/tree-sitter-<repo>` bullet format. For multi-grammar modules (e.g. typescript ships `typescript` + `tsx`, markdown ships `markdown` + `markdown-inline`), one bullet per upstream submodule is enough — they share the same repo. This list is documentation hygiene only; it is not enforced by `verify-notice.sh`, so an omission here will not fail CI but will silently drift from the published NOTICE.

Commit as `ci(verify-notice): include <lang> module` (fold the README edit into the same commit, or split as `docs(readme): list <lang> grammar in license section` — match the closest precedent).

## 7. End-to-end local verification

Run the full pipeline before declaring done:

```bash
./gradlew jvmTest -x :benchmarks:jvm:jvmTest
./gradlew apiCheck
./gradlew :samples:androidApp:assembleDebug
./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false --no-configuration-cache
bash scripts/verify-notice.sh
```

If `apiCheck` fails for the new module with a "missing api file" error, run `./gradlew :languages:<lang>:apiDump` to record the baseline. Two files are produced, and both must be committed:

- `languages/<lang>/api/jvm/<lang>.api`
- `languages/<lang>/api/<lang>.klib.api` — the iOS ABI (`iosArm64` + `iosSimulatorArm64`)

There is no `api/android/` dump: the migration to `com.android.kotlin.multiplatform.library` dropped Android from BCV, and the orphaned android dumps were deleted repo-wide. `apiCheck` runs `jvmApiCheck` + `klibApiCheck` only.

**klib dumps can only be produced on macOS** (Apple targets do not build elsewhere), which is why CI's `api-check` job runs on `macos-latest`. Running `apiDump` on Linux will not generate or update the `.klib.api` file.

Before committing the dumps, confirm the generated parser object was excluded (step 2):

```bash
grep -rl 'TreeSitter' --include='*.api' --include='*.klib.api' languages/<lang>/api   # must print nothing
```

If it prints a file, the `ignoredPackages` entry is missing or misspelled — fix that rather than committing the leak.

The verify-notice run must print `OK: ... contains META-INF/NOTICE` for both the new `<lang>-android` AAR and `<lang>-jvm` JAR. Spot-check the NOTICE content:

```bash
version="$(grep '^VERSION_NAME=' gradle.properties | cut -d= -f2)"
unzip -p "$HOME/.m2/repository/io/github/mataku/compose-syntax-highlight-<lang>-jvm/${version}/compose-syntax-highlight-<lang>-jvm-${version}.jar" META-INF/NOTICE
```

It must end with a `Bundled grammar:` block naming the grammar and license source. Multi-grammar modules show one `Bundled grammar:` block per grammar.

## 8. Note on submodule "dirty" state

If your grammar pin is missing `parser.c` (case from step 1), `generateParserSource` regenerates it locally on every fresh checkout, leaving `git status` showing the submodule as `-dirty`. The parent gitlink SHA is unchanged, so this is cosmetic. Don't `git add` the submodule entry to record the regenerated parser.c — the regeneration is supposed to be a build-time, per-checkout step.

## Reference

- `references/highlight-test-template.md` — full golden-test source you can copy-paste and adjust.
- `CLAUDE.md` — repo-wide guide; the "Adding a new language" section cross-references this skill.
- Single-grammar precedent: commits `9bbe87d`..`c54e2b3` on `develop` (Ruby + Rust).
- Multi-grammar precedent: the `languages/markdown/` module and the `languages/javascript/` + `languages/typescript/` pair (the latter demonstrates cross-module query reuse and the multi-predicate workaround).
