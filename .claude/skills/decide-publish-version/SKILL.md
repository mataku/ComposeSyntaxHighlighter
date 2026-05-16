---
name: decide-publish-version
description: Decide which version(s) to bump and which catalog values to update before publishing. Encodes the two-axis versioning rules from docs/specs/2026-05-16-independent-language-versioning-design.md. Run only when invoked explicitly via slash command.
disable-model-invocation: true
allowed-tools:
  - Read
  - Grep
  - Glob
  - Bash(git diff:*)
  - Bash(git log:*)
  - Bash(git show:*)
  - Bash(grep:*)
  - Bash(ls:*)
  - Bash(./gradlew apiCheck:*)
  - Bash(./gradlew apiDump:*)
  - Skill(git-commit)
---

# Decide publish version

This skill walks the maintainer through the two-axis versioning rules and outputs:

- Which `VERSION_NAME` files to bump (root `gradle.properties` for the core stack, `languages/<lang>/gradle.properties` for individual languages) and by which level (major/minor/patch).
- Whether the catalog (`gradle/libs.versions.toml`) needs an update — `ktreesitter`, `treesitterAbi`, or `coreApiCompatibleRange`.
- Whether other language modules need a compat re-publish (window-narrowing case).
- The publish workflow inputs to use (`module=` and the tag the workflow will produce).

The authoritative source for the rules is `docs/specs/2026-05-16-independent-language-versioning-design.md` (the "ABI update rules" event table and the "Release operations" section). This skill is a decision aid built on top of that spec — when something feels ambiguous, re-read the spec rather than guessing.

## 1. Identify the change event

Ask the user (one question at a time) until the change matches exactly one row in the rules table below. Treat these as mutually exclusive — pick the **first** row that applies; later rows are for events that have not been described by an earlier row.

The dimensions to clarify:

1. **What surface changed?** Pick one or more:
   - A `:languages:<lang>` module (specify which one).
   - A grammar submodule pin under `languages/<lang>/tree-sitter-<grammar>/` (parser shipped by that pin).
   - The version catalog (`gradle/libs.versions.toml`), specifically `treesitterAbi` or `ktreesitter`.
   - `:core-api` Kotlin source under `core-api/`.
   - `:core` Kotlin source under `core/` (excluding `core-api/`).
   - `:material3` or `:material3-text-field` Kotlin source.
2. **Did the parser ABI change?** Only relevant for grammar submodule pin updates. Compare `LANGUAGE_VERSION` in the old vs new `parser.c`:
   ```bash
   git show HEAD~1:languages/<lang>/tree-sitter-<grammar>/src/parser.c | grep '^#define LANGUAGE_VERSION'
   grep '^#define LANGUAGE_VERSION' languages/<lang>/tree-sitter-<grammar>/src/parser.c
   ```
3. **For ktreesitter bumps: did the accept window widen or narrow?** Check `MIN_COMPATIBLE_LANGUAGE_VERSION` and `LANGUAGE_VERSION` in the new ktreesitter release (the bundled tree-sitter C headers). Narrowing = `MIN_COMPATIBLE_LANGUAGE_VERSION` increased.
4. **For `:core-api` SPI changes: backward-compatible or binary-incompatible?** Run `./gradlew apiCheck`. If it fails saying signatures changed in a breaking way, treat as binary-incompatible. Otherwise backward-compatible.

## 2. Look up the event

| # | Event | Language X | Core stack | Catalog |
|---|---|---|---|---|
| 1 | Query or minor grammar fix in language X; ABI unchanged | **patch X** | unchanged | none |
| 2 | New capture added in language X; SPI unchanged, ABI unchanged | **minor X** | unchanged | none |
| 3 | Grammar submodule pin update in language X changes parser ABI; new ABI is inside the current ktreesitter accept window | **minor X** | unchanged | none |
| 4 | Catalog `treesitterAbi` value changes (only affects grammars whose `parser.c` is gitignored upstream — today: `swift`) | **minor** for affected languages | unchanged | bump `treesitterAbi` |
| 5 | ktreesitter bumped; accept window widens (new ABI accepted, no old ABI dropped) | unchanged. Optional **minor X** per language if you want to ride the wider ABI | minor or patch reflecting the catalog change | bump `ktreesitter` |
| 6 | ktreesitter bumped; accept window narrows (an old ABI is dropped) | **minor** for **every** language module — out-of-window ones with submodule pin updates, in-window ones to refresh the strict range | **major** for the core stack (`:core-api` cutoff) | bump `ktreesitter` and `coreApiCompatibleRange` (next major, e.g. `"[0.6, 1.0)"` → `"[1.0, 2.0)"`) |
| 7 | `:core-api` Kotlin SPI change, backward-compatible | unchanged | minor | `coreApiCompatibleRange` unchanged |
| 8 | `:core-api` Kotlin SPI change, binary-incompatible | **major** for every language, requiring rebuild | **major** | bump `coreApiCompatibleRange` (next major) |
| 9 | `:core` engine refactor; `:core-api` SPI unchanged | unchanged | patch or minor | none |
| 10 | `:material3*` UI improvement | unchanged | patch or minor on the affected UI module's release line | none |

Notes:

- "minor X" means bump only `languages/<lang>/gradle.properties`. The core stack `gradle.properties` does not move.
- The core stack moves as a unit: bumping the root `VERSION_NAME` is what gets applied to all four core-stack artefacts (`:core-api`, `:core`, `:material3`, `:material3-text-field`).
- Row 6 ("window narrows") is the one event where **every** language module re-publishes. The "out-of-window" ones change real code; the "in-window" ones look unchanged but still need to re-publish so their Gradle Module Metadata advertises the new strict range. The runbook is in `docs/publishing.md` ("Runbook: ktreesitter accept window narrows").
- Row 5 ("window widens") is a no-op for languages by default. Bumping a language to ride the wider ABI (i.e. updating its grammar submodule pin to one whose parser uses the newly-accepted ABI) is a separate change classified under row 3 — handle them as separate releases.

## 3. Translate the row into concrete actions

For each affected version axis, output:

### Core stack bump (row 5, 6, 7, 8, 9, or 10)

1. Update root `CHANGELOG.md` with an entry describing the change.
2. Edit root `gradle.properties`: bump `VERSION_NAME` per the row.
3. After PR merge: trigger the publish workflow with `module=core-stack`. The workflow tags the commit `core-stack-v<new-version>`.

### Single language bump (row 1, 2, 3)

1. Update `languages/<lang>/CHANGELOG.md`.
2. Edit `languages/<lang>/gradle.properties`: bump `VERSION_NAME` per the row.
3. Add a row to `docs/compatibility.md` recording the new `(Module, Version, Parser ABI, ktreesitter tested, core-api tested, Targets)` tuple.
4. After PR merge: trigger the publish workflow with `module=languages/<lang>`. The workflow tags the commit `<lang>-v<new-version>`.

### Catalog `treesitterAbi` bump (row 4)

1. Edit `gradle/libs.versions.toml`: change `treesitterAbi = "<old>"` to the new value.
2. Identify affected languages: any module whose grammar gitignores `parser.c` upstream (today only `swift`). Confirm with:
   ```bash
   ls languages/<lang>/tree-sitter-<grammar>/src/parser.c
   ```
   Missing = affected. The plugin's `generateParserSource` will produce a new `parser.c` at the new ABI on next build.
3. Bump each affected language's `VERSION_NAME` (minor).
4. Publish the affected language modules one at a time via the workflow.
5. Add rows in `docs/compatibility.md` for every artefact re-published.

### Catalog `ktreesitter` window-widens (row 5)

1. Edit `gradle/libs.versions.toml`: bump `ktreesitter`.
2. Bump root `VERSION_NAME` (minor or patch as appropriate for the catalog change).
3. Publish the core stack only.
4. No language re-publishes are required. Individual languages may opt in to the wider ABI as separate per-language releases (handled under row 3).

### Catalog `ktreesitter` window-narrows (row 6)

This is the multi-step runbook. Follow `docs/publishing.md` § "Runbook: ktreesitter accept window narrows" exactly. Summary:

1. Bump catalog `ktreesitter` and major-bump `coreApiCompatibleRange`.
2. Inspect each language's parser ABI:
   ```bash
   grep -H 'LANGUAGE_VERSION' languages/*/tree-sitter-*/src/parser.c
   ```
   Mark each as in-window or out-of-window against the new ktreesitter accept range.
3. For out-of-window languages: update the grammar submodule pin to a commit whose `parser.c` is inside the new window. Update the language's `CHANGELOG.md`. Treat the language change as part of this coordinated release.
4. For in-window languages: add a CHANGELOG entry noting "compat re-publish for `:core-api` major X".
5. Major-bump root `VERSION_NAME`. Update root `CHANGELOG.md`.
6. Publish `module=core-stack` first.
7. Then publish every `module=languages/<lang>`, minor-bumping each language's `gradle.properties`.
8. Append rows to `docs/compatibility.md` for every artefact.

Until the language re-publishes are done, consumers upgrading the core stack across this major will hit a Gradle `strictly` conflict on `compose-syntax-highlight-api` — that is by design.

### Catalog `coreApiCompatibleRange` bump (row 8)

A binary-incompatible `:core-api` SPI change implies all consumers must rebuild against the new major. Steps mirror the window-narrowing runbook except the trigger is an SPI change rather than a ktreesitter event:

1. Major-bump `coreApiCompatibleRange` in the catalog.
2. Major-bump root `VERSION_NAME`.
3. Publish `module=core-stack`.
4. Major-bump every language `gradle.properties` and re-publish each one. The language source code itself may or may not need changes depending on what the SPI break is; the version bump and re-publish are mandatory regardless.
5. Add rows to `docs/compatibility.md`.

## 4. Sanity checks before publishing

Before recommending the maintainer trigger the workflow:

- Run `./gradlew jvmTest -x :benchmark:jvmTest` — must pass.
- Run `./gradlew apiCheck` — must pass (or the change is intentional and the user has run `apiDump` and reviewed the diff).
- Run `./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false` and inspect:
  - Each affected POM has the expected `tree-sitter-abi` property.
  - Each affected `.module` has `"strictly": "<expected coreApiCompatibleRange>"` on its `compose-syntax-highlight-api` dependency.
- Run `bash scripts/verify-notice.sh <project-paths>` for the modules to be published.
- Confirm no module being published has a `-SNAPSHOT` suffix in its `gradle.properties` — the workflow refuses SNAPSHOT publishes.

## 5. Report format

After running the skill, summarize for the user:

- **Event row matched**: number + short label (e.g. "row 3: submodule pin update, ABI inside window").
- **Files to edit**: explicit list, e.g.
  - `languages/kotlin/gradle.properties` — bump `VERSION_NAME` from `0.6.0` to `0.6.1` (patch).
  - `languages/kotlin/CHANGELOG.md` — add Unreleased entry.
  - `docs/compatibility.md` — append row.
- **Catalog updates** (if any): `gradle/libs.versions.toml` entries to change.
- **Other languages affected**: e.g. for row 6, list every language and which kind of bump it gets.
- **Publish workflow**: which `module=` value to use, and the resulting tag.
- **Pre-publish checks**: the four sanity-check commands above.

Do not perform the publish yourself — output the plan and stop. The maintainer triggers the workflow manually via GitHub Actions.
