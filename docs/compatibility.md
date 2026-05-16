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

The release-management rules — when each version bumps in response to a change
event — live in
[docs/specs/2026-05-16-independent-language-versioning-design.md](specs/2026-05-16-independent-language-versioning-design.md).
That spec is the authoritative source for "what kind of bump does this change
warrant". One-sentence summary: we follow ktreesitter's range semantics — when
ktreesitter narrows its accept window we major-bump `:core-api` and re-publish
every language module so each artefact carries the new strict range.

## Compatibility table

| Module | Version | Parser ABI | ktreesitter (tested) | core-api (tested) | Targets |
|---|---|---|---|---|---|

<!-- Append one row per published artefact. Rows are append-only; do not remove
     entries for older versions. -->
