---
name: add-syntax-theme
description: End-to-end workflow for adding a new built-in `SyntaxTheme` to `:core` (license check, companion `val by lazy`, NOTICE attribution, tests, verify-notice keyword, demo wiring, CHANGELOG). Run only when invoked explicitly via slash command.
disable-model-invocation: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - Bash(./gradlew:*)
  - Bash(bash scripts/verify-notice.sh:*)
  - Bash(grep:*)
  - Bash(ls:*)
  - Skill(git-commit)
  - Skill(superpowers:test-driven-development)
  - Skill(superpowers:verification-before-completion)
---

# Adding a new built-in syntax theme

This skill encodes the workflow for adding a new built-in `SyntaxTheme` constant to `:core`. The Solarized / GitHub / Atom One / Dracula additions on `develop` are the canonical worked example — every existing companion `val` other than `DarkDefault` / `LightDefault` followed this skill's shape.

The workflow is rigid — follow the order. Most steps are obvious, but several have non-obvious traps the order avoids.

## 1. Pick the upstream palette and verify the license

**Acceptable license:** All built-in themes today are MIT. If the upstream palette is **not** MIT (or a permissively-compatible license like Apache-2.0, BSD), stop and ask the user — the NOTICE attribution shape and `:core` license posture both change.

**Capture only the *colors and capture-key mappings*, not source code.** The bundled artifact is a `SyntaxTheme` data class with named `SpanStyle?` fields per tree-sitter capture (e.g. `keyword`, `stringEscape`) plus a `background: Color`. We are not copying any upstream source files into the repo, just hex codes.

**Naming:** Use `<Family><Variant>` PascalCase (e.g. `SolarizedDark`, `GitHubLight`, `OneDark`). Single-variant themes drop the variant suffix (e.g. `Dracula`). Match the upstream project's own canonical capitalization (`GitHub`, not `Github`).

**Required typed fields.** Every built-in theme in `core/src/commonMain/kotlin/io/github/mataku/compose/highlight/core/SyntaxTheme.kt` sets the same 14 fields. Keep this set consistent so language modules don't get holes:

```
keyword, function, type, string, stringEscape, number, boolean,
comment, constant, property, variable, namespace, operator, punctuation
```

(`stringEscape` is the field name for the `string.escape` tree-sitter capture; the resolver maps between them.)

`keyword` typically gets `fontWeight = FontWeight.Bold` to match precedent. Pick palette-appropriate hex values for the rest. Read existing entries in `SyntaxTheme.kt` for the exact shape you should mirror.

**`background: Color` is mandatory.** Built-in themes always carry a non-null `background`; only user-constructed `SyntaxTheme(...)` defaults to `null`. The demo and downstream consumers source the surface backdrop from this field.

## 2. Add the companion val to `SyntaxTheme.kt`

Edit `core/src/commonMain/kotlin/io/github/mataku/compose/highlight/core/SyntaxTheme.kt`. Inside the `companion object`, append a new `val <Name>: SyntaxTheme by lazy { ... }` block matching the shape of the existing entries.

Required header KDoc — one line, ending with `Attribution in META-INF/NOTICE.`:

```kotlin
/** <Upstream project / author tagline>. Attribution in META-INF/NOTICE. */
val <Name>: SyntaxTheme by lazy {
    SyntaxTheme(
        baseStyle = SpanStyle(color = Color(0x........)),
        background = Color(0x........),
        keyword = SpanStyle(color = Color(0x........), fontWeight = FontWeight.Bold),
        function = SpanStyle(color = Color(0x........)),
        // ... 13 more named fields in the same order as SolarizedDark
    )
}
```

Order new entries by family (Default → Solarized → GitHub → One → Dracula → your new family) so the file's grouping stays readable.

## 3. Append the NOTICE attribution

Edit `core/src/commonMain/resources/META-INF/NOTICE`. Add a 2-line block above the `=== MIT License ===` divider, after the last existing third-party block:

```
<Family> — Copyright (c) <year(s)> <author/owner>
  Source: https://github.com/<owner>/<repo>
```

The MIT license body block at the bottom is shared and unchanged. Leave it.

If the new palette is a non-MIT permissive license, append a separate license body block under a clearly-labeled divider — do not silently mix licenses.

## 4. Pin the keyword in `verify-notice.sh`

Edit `scripts/verify-notice.sh`. Append a unique substring of the family name to the `required_keywords_compose_highlight_core` array:

```bash
required_keywords_compose_highlight_core=("Solarized" "GitHub Primer" "Atom One" "Dracula" "<NewFamily>")
```

Pick a string that uniquely identifies the family in the NOTICE body (a single word usually works). This guards against accidental NOTICE truncation during a refactor — the publish gate will fail loudly.

## 5. Add tests (TDD)

Edit `core/src/commonTest/kotlin/io/github/mataku/compose/highlight/core/SyntaxThemeTest.kt`. Append one `@Test` per new variant matching the existing per-theme pattern:

```kotlin
@Test
fun <name>_provides_required_capture_keys_and_background() {
    val theme = SyntaxTheme.<Name>
    assertNotNull(theme.keyword)
    assertNotNull(theme.string)
    assertNotNull(theme.comment)
    assertEquals(theme.keyword, theme.resolve("keyword.return"))
    assertEquals(Color(0x........), theme.background)
}
```

Run `./gradlew :core:jvmTest` and confirm the new tests pass. Failure shapes:

- *Background mismatch*: the `Color(0x...)` in the test does not match the `background = ...` you set in step 2 — fix the test or the theme, whichever is wrong.
- *Missing capture field*: you skipped one of the 14 required typed fields in step 2 — go back and add it.

`:core:commonTest` runs through the JVM target, so `:core:jvmTest` is the correct task name (`:core:commonTest` does not exist as a Gradle task).

## 6. Wire into the demo (`composeApp`)

Edit `samples/composeApp/src/commonMain/kotlin/com/mataku/composesyntaxhighlighter/HighlighterDemo.kt`:

- Extend the `DemoTheme` enum with a new entry. Pick a human-friendly `label` (e.g. `"Solarized Dark"`).
- Extend the `when (selectedTheme) { ... }` branch to map the new enum entry to `SyntaxTheme.<Name>`.

No new import is needed — `SyntaxTheme` is already imported.

Run `./gradlew :samples:androidApp:assembleDebug` to verify the build (`:samples:composeApp` is the shared KMP library demo; `:samples:androidApp` is the launchable APK consuming it). The dropdown will list the new theme and the surface backdrop will pick up `theme.background` automatically.

## 7. Update CHANGELOG.md

Edit `CHANGELOG.md`. Add a bullet under `## [Unreleased]` → `### Added`:

```markdown
- `SyntaxTheme.<Name>` built-in theme (<Family>, <license>). Attribution in
  `META-INF/NOTICE` of the `:core` artifact.
```

If you are adding multiple variants of one family, list them as a single bullet:
`SyntaxTheme.<Family><DarkVariant>` and `SyntaxTheme.<Family><LightVariant>`.

## 8. End-to-end local verification

Run the full pipeline before declaring done:

```bash
./gradlew :core:jvmTest
./gradlew :samples:androidApp:assembleDebug
./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false --no-configuration-cache
bash scripts/verify-notice.sh
```

`verify-notice.sh` must print `OK: compose-highlight-core-... contains META-INF/NOTICE` for both the AAR (`classes.jar` path) and JVM JAR — and crucially, must not print `FAIL: ... missing required attributions: <NewFamily>`. If it does, your step 3 (NOTICE body) and step 4 (keyword array) disagree about the substring.

Spot-check the published NOTICE content:

```bash
version="$(grep '^VERSION_NAME=' gradle.properties | cut -d= -f2)"
unzip -p "$HOME/.m2/repository/io/github/mataku/compose-highlight-core-jvm/${version}/compose-highlight-core-jvm-${version}.jar" META-INF/NOTICE
```

It must contain the new attribution line you wrote in step 3.

## 9. Commit

Use the `git-commit` skill (per the global CLAUDE.md rule, never run `git commit` directly via Bash). Suggested splits, mirroring the precedent on `develop`:

- `feat(core): add <Family> built-in theme(s)` — `SyntaxTheme.kt`, `SyntaxThemeTest.kt`, `NOTICE`, `verify-notice.sh`
- `feat(composeApp): expose <Family> in demo theme picker` — `HighlighterDemo.kt`
- `docs(changelog): note <Family> addition` — `CHANGELOG.md`

If the change is small (single theme, no demo refactor), folding all three into one `feat(core): add <Family> built-in theme` commit is also fine — match the precedent of the closest prior addition.

## Reference

- `core/src/commonMain/kotlin/io/github/mataku/compose/highlight/core/SyntaxTheme.kt` — read existing companion `val`s to mirror the exact shape.
- `core/src/commonMain/resources/META-INF/NOTICE` — read existing attribution blocks for tone and structure.
- `AGENTS.md` (`CLAUDE.md` symlink) — repo-wide guide; the "Public API usage example" and "License notes" sections cross-reference this skill's output.
