---
name: add-syntax-theme
description: Workflow for adding a new built-in `SyntaxTheme` to `:core` by mirroring the most recently added theme family (license check, every touchpoint, verification). Run only when invoked explicitly via slash command.
disable-model-invocation: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - Bash(./gradlew:*)
  - Bash(bash scripts/verify-notice.sh:*)
  - Bash(git grep:*)
  - Bash(git log:*)
  - Bash(grep:*)
  - Bash(ls:*)
  - Skill(git-commit)
  - Skill(superpowers:test-driven-development)
  - Skill(superpowers:verification-before-completion)
---

# Adding a new built-in syntax theme

The existing themes are the specification. This skill intentionally does not restate code shapes, file contents, or field lists — they drift. Derive them from the latest theme family instead.

## 1. Verify the license

All built-in themes today are MIT. If the upstream palette is not MIT or a permissive equivalent (Apache-2.0, BSD), stop and ask the user before writing anything. Copy only colors, never upstream source files.

## 2. Find every touchpoint from the latest theme family

Identify the most recently added family (`git log --oneline -- core/src/commonMain/kotlin/io/github/mataku/compose/highlight/core/SyntaxTheme.kt`), then list everywhere it appears:

```bash
git grep -n -i '<LatestFamily>' -- ':!docs/plans' ':!docs/specs' ':!.claude'
```

Every hit is a place the new theme must also appear (theme definition, tests, NOTICE files, `scripts/verify-notice.sh` keyword, README tables/license list, demo, sample code, API dumps). Mirror each hit's exact shape — including trailing calls, KDoc format, and ordering.

## 3. Rules that are not visible in code

- **Naming:** `<Family><Variant>` PascalCase using the upstream's canonical capitalization (`GitHubLight`, not `GithubLight`). Single-variant themes drop the suffix.
- **Fields:** set every typed field the latest built-in theme sets, and a non-null `background`.
- **NOTICE:** the `verify-notice.sh` keyword must be a substring that appears in the new NOTICE attribution, or the publish gate fails.
- **Multi-variant families** get one README license bullet and one NOTICE block, not one per variant.

## 4. Verify

Write the test first (TDD), then:

```bash
./gradlew :core:jvmTest
./gradlew apiDump
./gradlew :samples:androidApp:assembleDebug
./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false --no-configuration-cache
bash scripts/verify-notice.sh
```

`verify-notice.sh` must not report missing attributions for the new family. Add a `CHANGELOG.md` entry under `## [Unreleased]`, then commit via the `git-commit` skill.
