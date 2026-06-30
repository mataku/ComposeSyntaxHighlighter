# Module compatibility

Each published artefact records the tree-sitter ABI baked into its parser and the
ktreesitter / core-api range it was tested against. Use this table to verify that
the modules on your classpath form a compatible set.

When Gradle reports a `strictly` conflict on `compose-syntax-highlight-api`, look
up the language modules involved and pick versions whose `core-api (tested)`
column overlaps with your core stack's version. The runbook in
[docs/publishing.md](publishing.md) describes the maintainer-side procedure for
the events that produce conflicts.

## Versioning rules

Each language module carries its own `VERSION_NAME` in
`languages/<lang>/gradle.properties` and moves independently from the core
stack (`:core-api`, `:core`, `:material3`, `:material3-text-field`), which
shares the root `gradle.properties` `VERSION_NAME`. Bump and release each axis
on its own change drivers; see [docs/publishing.md](publishing.md) for the
per-axis release flow.

We follow ktreesitter's range semantics — when ktreesitter narrows its accept
window we major-bump `:core-api` and re-publish every language module so each
artefact carries the new strict range.

## Compatibility table

| Module | Version | Parser ABI | ktreesitter (tested) | core-api (tested) | Targets |
|---|---|---|---|---|---|
| compose-syntax-highlight-go | 0.6.0 | 15 | 0.25.1 | 0.6.0 | Android, JVM, iosArm64, iosSimulatorArm64 |
| compose-syntax-highlight-java | 0.6.0 | 14 | 0.25.1 | 0.6.0 | Android, JVM, iosArm64, iosSimulatorArm64 |
| compose-syntax-highlight-javascript | 0.6.0 | 15 | 0.25.1 | 0.6.0 | Android, JVM, iosArm64, iosSimulatorArm64 |
| compose-syntax-highlight-kotlin | 0.6.0 | 14 | 0.25.1 | 0.6.0 | Android, JVM, iosArm64, iosSimulatorArm64 |
| compose-syntax-highlight-markdown | 0.6.0 | 15 | 0.25.1 | 0.6.0 | Android, JVM, iosArm64, iosSimulatorArm64 |
| compose-syntax-highlight-python | 0.6.0 | 15 | 0.25.1 | 0.6.0 | Android, JVM, iosArm64, iosSimulatorArm64 |
| compose-syntax-highlight-ruby | 0.6.0 | 14 | 0.25.1 | 0.6.0 | Android, JVM, iosArm64, iosSimulatorArm64 |
| compose-syntax-highlight-rust | 0.6.0 | 15 | 0.25.1 | 0.6.0 | Android, JVM, iosArm64, iosSimulatorArm64 |
| compose-syntax-highlight-swift | 0.6.0 | 15 | 0.25.1 | 0.6.0 | Android, JVM, iosArm64, iosSimulatorArm64 |
| compose-syntax-highlight-typescript | 0.6.0 | 14 | 0.25.1 | 0.6.0 | Android, JVM, iosArm64, iosSimulatorArm64 |

<!-- Append one row per published artefact. Rows are append-only; do not remove
     entries for older versions. -->
