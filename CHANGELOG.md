# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Built-in third-party syntax themes on `SyntaxTheme`: `SolarizedDark`,
  `SolarizedLight`, `GitHubDark`, `GitHubLight`, `OneDark`, `OneLight`, `Dracula`.
  All four upstream palettes (Solarized, GitHub Primer, Atom One, Dracula) are
  MIT-licensed and attributed in `META-INF/NOTICE` of the `:core` artifact.
- `SyntaxTheme.background: Color?` — optional canonical backdrop color carried
  by each built-in theme. User-constructed `SyntaxTheme(...)` defaults to
  `null`, preserving prior behavior.
- Cross-module `object Languages` catalog. Each `:languages:<name>` artifact
  contributes a `val Languages.<name>: Language` extension forwarder pointing
  at the existing top-level `<Lang>Language` val. Closed-enum semantics are
  intentionally not provided (cross-module enum extension is impossible in
  Kotlin).
- `compose-highlight-java` language module (tree-sitter-java, MIT).
- `compose-highlight-ruby`, `compose-highlight-rust`, `compose-highlight-python`,
  `compose-highlight-go` language modules (all upstream MIT).
- `META-INF/NOTICE` in the `:core` artifact, listing third-party theme
  attributions; `scripts/verify-notice.sh` enforces presence and required body
  keywords as a publish hard gate.

### Changed
- **BREAKING:** `SyntaxTheme.darkDefault()` / `SyntaxTheme.lightDefault()`
  (companion `fun`) are renamed to `SyntaxTheme.DarkDefault` /
  `SyntaxTheme.LightDefault` (companion `val by lazy`). Migrate call sites by
  dropping the parentheses.
- Demo (`composeApp`) theme dropdown now lists all nine built-in themes and
  sources the surface backdrop from `theme.background` rather than hardcoding
  it. Demo language selection now uses `Languages.<name>` instead of importing
  each per-module `<Lang>Language` val.

### Documentation
- AGENTS.md "Adding a new language" recipe moved to
  `.claude/skills/add-tree-sitter-language/SKILL.md`. AGENTS.md now
  cross-references the skill instead of duplicating the steps.
- README adds the `compose-highlight-java` row to the language artifact table.

[Unreleased]: https://github.com/mataku/ComposeSyntaxHighlighter/compare/main...HEAD
