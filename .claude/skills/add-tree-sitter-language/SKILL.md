---
name: add-tree-sitter-language
description: End-to-end workflow for adding a new tree-sitter-backed language module (grammar selection with ABI-14 pin check, submodule add, Gradle wiring, Language object, golden tests, demo wiring, CI updates). Run only when invoked explicitly via slash command.
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

This skill encodes the workflow for adding a new `:languages:<lang>` module. Commits `9bbe87d`..`c54e2b3` on `develop` are the canonical worked example (Ruby + Rust). Read `references/highlight-test-template.md` when you need the full golden-test source.

The workflow is rigid — follow the order. Most steps are obvious, but several have non-obvious traps that the order avoids.

## 1. Pick the upstream grammar and the right pin

**Search order for grammar repos:**

1. `https://github.com/tree-sitter` — the official org maintains canonical grammars for many languages (`tree-sitter-ruby`, `tree-sitter-rust`, `tree-sitter-python`, `tree-sitter-go`, …). Prefer these when they exist.
2. `https://github.com/tree-sitter-grammars` — community-maintained grammars curated by the tree-sitter project. Use when (1) has nothing.
3. Well-known forks (e.g. `fwcd/tree-sitter-kotlin`, `alex-pinkus/tree-sitter-swift`) — only when neither org has a usable grammar. Document why in the commit message.

**License:** All grammars in the repo today are MIT. If you pick something non-MIT, stop and ask the user before going further — it changes how the NOTICE bundling looks and the `licenseSpdx` / `licenseSource` DSL values.

**Pin selection — this is the gotcha that bit Rust:**

`generateParserSource` (in `build-logic/src/main/kotlin/compose-highlight-language.gradle.kts`) only re-runs `tree-sitter generate --abi=14` when (a) `src/parser.c` is missing or (b) `grammar.js` is newer than `src/parser.c`. **It does NOT detect ABI mismatch.** ktreesitter 0.24.1 only loads ABI 13–14, so an upstream parser.c at ABI 15 reaches `Language(...)` and throws `IllegalArgumentException: Incompatible language version 15` at runtime.

Verification happens *after* `git submodule add` (in step 2) but *before* you record the gitlink in a commit. If the default-checked-out commit ships parser.c at ABI 15, you walk back tags via `git -C <submodule> checkout <older-tag>` until the bundled parser.c reads `#define LANGUAGE_VERSION 14`, then commit *that* gitlink. Concrete precedent: `tree-sitter-rust` upstream main bundles ABI 15; `v0.23.3` is the last tag at ABI 14, and that's what we pin.

If the grammar ships *no* `parser.c` at all (e.g. `tree-sitter-swift` at the pinned commit), that's fine — the existing `!parserC.exists()` branch in `onlyIf` triggers regeneration with the configured ABI on every fresh checkout.

If no tag is at ABI 14, stop and tell the user — bumping ktreesitter and `treesitterAbi = "15"` is a separate, repo-wide change that has to land first.

## 2. Add the submodule, verify ABI, and create the module skeleton

```bash
git submodule add https://github.com/<owner>/tree-sitter-<lang>.git languages/<lang>/tree-sitter-<lang>
```

`git submodule add` defaults to upstream main HEAD. Inspect the bundled parser.c immediately:

```bash
grep '#define LANGUAGE_VERSION' languages/<lang>/tree-sitter-<lang>/src/parser.c
```

If it reports `14`, you're done with verification. If it reports `15` (or anything other than 14), list available tags and pick the newest tag whose tree contains parser.c at ABI 14:

```bash
git -C languages/<lang>/tree-sitter-<lang> tag --sort=-version:refname
git -C languages/<lang>/tree-sitter-<lang> show <candidate-tag>:src/parser.c | grep LANGUAGE_VERSION
git -C languages/<lang>/tree-sitter-<lang> checkout <chosen-tag>
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
    id("compose-highlight-language")
}

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

Append to `settings.gradle.kts` after the existing `:languages:<...>` includes:

```kotlin
include(":languages:<lang>")
```

Run `./gradlew :languages:<lang>:tasks --quiet`. The plugin's `afterEvaluate` writes `languages/<lang>/CMakeLists.txt` and `languages/<lang>/host-cmake/CMakeLists.txt`. **These generated files must be committed** — the existing kotlin/swift modules track theirs.

Commit (one atomic commit per the repo's git-commit skill):

- `.gitmodules`
- `languages/<lang>/tree-sitter-<lang>` (the gitlink)
- `languages/<lang>/build.gradle.kts`
- `languages/<lang>/CMakeLists.txt`
- `languages/<lang>/host-cmake/CMakeLists.txt`
- `settings.gradle.kts`

## 3. Implement `<Lang>Language` with golden tests (TDD)

Write the failing test first. Use the template in `references/highlight-test-template.md` and substitute language-specific keywords/comment syntax. Run `./gradlew :languages:<lang>:jvmTest` and confirm the failure is `Unresolved reference '<Lang>Language'` (compile failure) — that proves the test exercises the right code path.

Then create `languages/<lang>/src/commonMain/kotlin/io/github/mataku/compose/highlight/<lang>/<Lang>Language.kt`:

```kotlin
package io.github.mataku.compose.highlight.<lang>

import io.github.mataku.compose.highlight.core.Language
import io.github.mataku.compose.highlight.core.kTreeSitterLanguage
import io.github.mataku.compose.highlight.<lang>.internal.TreeSitter<Lang>
import io.github.treesitter.ktreesitter.Language as TsLanguage

val <Lang>Language: Language by lazy {
    kTreeSitterLanguage(TsLanguage(TreeSitter<Lang>.language()), HIGHLIGHTS_QUERY)
}
```

Re-run `./gradlew :languages:<lang>:jvmTest`. Expect all 5 tests to pass.

**If a test fails at runtime** (not at compile time), inspect the actual spans printed in the failure message before changing anything. The two common shapes:

- *Capture-name mismatch*: upstream `highlights.scm` uses a name your test theme doesn't map. Example: Rust integer literals are tagged `@constant.builtin`, not `@number`. Keep the test logic, add the upstream capture key to the test theme. `SyntaxTheme.resolve()` does dotted-prefix fallback, so `constant.builtin` will fall back to `constant` — but the most precise key is the most predictable.
- *ABI mismatch*: failure says `Incompatible language version <N>. Must be between 13 and 14.` Stop. Go back to step 1 and pick a different submodule pin. Do not patch `build-logic` for this; the project policy is to fix the pin instead.

Commit the Language object and the test as one atomic commit.

## 4. Wire into the demo (`composeApp`)

`composeApp/build.gradle.kts` — extend the existing `commonMain.dependencies` block, after the `swift` line:

```kotlin
implementation(projects.languages.<lang>)
```

`composeApp/src/commonMain/kotlin/com/mataku/composesyntaxhighlighter/SampleCode.kt` — add a `val <lang>: String` property containing a 15–20 line snippet that exercises the language's distinctive constructs (keywords, strings, comments, numbers, plus 1–2 idiomatic features so the highlight result is visibly different from base text).

`composeApp/src/commonMain/kotlin/com/mataku/composesyntaxhighlighter/HighlighterDemo.kt`:

- Add the import: `import io.github.mataku.compose.highlight.<lang>.<Lang>Language`
- Extend the `DemoLanguage` enum with a new entry.
- Extend the `when (selectedLang)` branch to map the new enum entry to `SampleCode.<lang> to <Lang>Language`.

Run `./gradlew :composeApp:assembleDebug` to verify the build. Commit composeApp changes as one atomic commit.

## 5. Update the two hardcoded files

These files have hardcoded module lists that must be edited every time:

**`.github/workflows/build.yml`** — the `jvm-tests` job's `run:` line:

```yaml
- run: ./gradlew :core:jvmTest :languages:kotlin:jvmTest :languages:swift:jvmTest :languages:<lang>:jvmTest
```

**`scripts/verify-notice.sh`** — the `modules` array:

```bash
modules=(
  compose-highlight-kotlin
  compose-highlight-swift
  compose-highlight-<lang>
)
```

These are two unrelated changes — commit them separately (`ci(build): …` and `ci(verify-notice): …`), matching the kotlin/swift precedent.

## 6. End-to-end local verification

Run the full pipeline before declaring done:

```bash
./gradlew :core:jvmTest :languages:kotlin:jvmTest :languages:swift:jvmTest :languages:<lang>:jvmTest
./gradlew :composeApp:assembleDebug
./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false --no-configuration-cache
bash scripts/verify-notice.sh
```

The verify-notice run must print `OK: ... contains META-INF/NOTICE` for both the new `<lang>-android` AAR and `<lang>-jvm` JAR. Spot-check the NOTICE content:

```bash
version="$(grep '^VERSION_NAME=' gradle.properties | cut -d= -f2)"
unzip -p "$HOME/.m2/repository/io/github/mataku/compose-highlight-<lang>-jvm/${version}/compose-highlight-<lang>-jvm-${version}.jar" META-INF/NOTICE
```

It must end with a `Bundled grammar:` block naming the grammar and license source.

## 7. Note on submodule "dirty" state

If your grammar pin is missing `parser.c` (case from step 1), `generateParserSource` regenerates it locally on every fresh checkout, leaving `git status` showing the submodule as `-dirty`. The parent gitlink SHA is unchanged, so this is cosmetic. Don't `git add` the submodule entry to record the regenerated parser.c — the regeneration is supposed to be a build-time, per-checkout step.

## Reference

- `references/highlight-test-template.md` — full golden-test source you can copy-paste and adjust.
- `AGENTS.md` (`CLAUDE.md` symlink) — repo-wide guide; the "Adding a new language" and "Tree-sitter ABI version pinning" sections cross-reference this skill.
- Commits `9bbe87d`..`c54e2b3` — the Ruby/Rust addition is the canonical worked example.
