# Releasing

Cross-module versioning rules that govern what ships together. For the
underlying Maven Central / signing / tag-push mechanics, see the Publishing
section of `CLAUDE.md`.

## Versioning policy

The default rule is simple:

- **`:core` + `:material3`** ship in lockstep, always at the same version.
  `:material3` consumes `:core` beyond the public SPI, so the two are not
  meaningfully separable.
- **`:languages:<name>`** version independently. Each language module is cut
  on its own cadence so a grammar update or `highlights.scm` tweak does not
  drag a `:core` release with it.

Downstream users can bump `compose-syntax-highlight-core` (e.g. for new
themes) without updating language artifacts, and vice versa.

## Changes that require lockstep across all modules

The default rule above covers most releases. The exceptions below should be
recognized at PR time and cut as a synchronized release across `:core` +
`:material3` and every `:languages:*` artifact.

1. **`SyntaxTheme` typed-field changes (add / rename / remove).** A `:core`
   public API change. Older `:languages:*` artifacts continue to work in the
   additive case, but updating language modules to emit the matching capture
   name in the same release is the cleanest user-facing story. Renames and
   removals require a `:core` major bump.
2. **Changing the internal capture-name to typed-field mapping inside
   `:core`.** Verify every `:languages:*:jvmTest` golden against the modified
   `:core` before tagging.
3. **Bumping `treesitterAbi` in `libs.versions.toml`.** Every language
   module needs its parser regenerated; re-publish them all in lockstep.

## Changes that do NOT require coordination

- Built-in theme palettes added or edited in `:core` — `:core` + `:material3`
  only.
- A `:languages:<name>` grammar submodule update or `highlights.scm` tweak
  that uses **existing typed capture names** — that language module only.
- A `highlights.scm` change that introduces a new capture flowing through
  `SyntaxTheme.extras` — that language module only. `extras` is a
  `Map<String, SpanStyle>` and is best-effort by design; older `:core`
  versions ignore unknown keys, which is documented behavior, not a
  regression.

## Why there is no cross-version compatibility test in CI

A `:core` v1.0 + `:languages:kotlin` v1.1 (or the reverse) smoke test was
considered and intentionally not implemented. The drift surface it would
protect is structurally narrow:

- `:core-api` is the only inter-module binary contract and is frozen by
  `binary-compatibility-validator`.
- The typed `SyntaxTheme` fields are `:core` public API and locked by the
  same validator, so adding, renaming, or removing them is a tracked release
  event covered by the lockstep rules above.
- The internal capture-name to typed-field mapping is exercised by every
  `:languages:*:jvmTest` golden against monorepo HEAD; any regression fails
  on the PR that introduces it.
- `SyntaxTheme.extras` carries no contract, so divergence there is not a
  regression by design.

The only unprotected scenario is a maintainer who changes both a
capture-name mapping in `:core` and a language module's emitted captures in
the same commit, then publishes only the language module while leaving
`:core` at the prior version. The lockstep rules above prevent that.
Mechanical CI enforcement is not warranted at this scale.

## Pre-release checklist

- [ ] `./gradlew apiCheck` clean
- [ ] `./gradlew :core:jvmTest :material3:jvmTest` plus every
      `:languages:*:jvmTest` green
- [ ] `./gradlew spotlessCheck` clean
- [ ] `./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false --no-configuration-cache`
- [ ] `bash scripts/verify-notice.sh` passes against the
      `~/.m2`-published artifacts
- [ ] `gradle.properties` `VERSION_NAME` matches the tag about to be pushed
- [ ] If a lockstep event applies (see above), every affected module is
      re-tagged at the same version
