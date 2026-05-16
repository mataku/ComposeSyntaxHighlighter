# Publishing

## Local publish + NOTICE verification

```bash
./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false --no-configuration-cache
bash scripts/verify-notice.sh
```

`scripts/verify-notice.sh` walks each language artifact (Android AAR + JVM JAR) and asserts that `META-INF/NOTICE` is present (descending into `classes.jar` for AARs).

## CI

`.github/workflows/build.yml` runs JVM tests, the Android demo build, and NOTICE verification on every PR / push to main / develop. `.github/workflows/publish.yml` runs `publishToMavenCentral` on manual dispatch from the GitHub Actions UI, scoped to a single module per run, using vanniktech with in-memory signing keys from repository secrets.

## Local Maven Central publishing

For ad-hoc publishes (e.g. cutting a SNAPSHOT for downstream verification) `settings.gradle.kts` loads the following keys from `local.properties` (gitignored) and exposes them as gradle properties:

- `mavenCentralUsername` / `mavenCentralPassword` — Central Portal user token
- `signingInMemoryKey` / `signingInMemoryKeyId` / `signingInMemoryKeyPassword` — GPG signing material (the key may be base64-encoded for single-line storage)

Environment variables (`ORG_GRADLE_PROJECT_*`) take precedence over `local.properties`, so CI continues to work unchanged.

## SNAPSHOT vs release

- Set `VERSION_NAME=0.1.0-SNAPSHOT` in `gradle.properties` and run `./gradlew publishToMavenCentral --no-configuration-cache` to push to Central Portal's snapshot repository (`https://central.sonatype.com/repository/maven-snapshots/`). Snapshots are auto-published with no manual gate.
- For a release, set `VERSION_NAME=0.1.0`, run the Publish workflow manually from GitHub Actions (see "Publishing a single module" below), then click **Publish** on the Central Portal Deployments page (the workflow uses `publishToMavenCentral`, which uploads to staging without auto-releasing).

## Publishing a single module

The publish workflow at `.github/workflows/publish.yml` accepts a `module` input
and publishes only the chosen scope.

### Core stack

1. Update the root `CHANGELOG.md`.
2. Bump `VERSION_NAME` in the root `gradle.properties`.
3. Open and merge the PR.
4. From the GitHub Actions UI, run **Publish** with `module=core-stack`.
5. The workflow tags the commit `core-stack-v<version>` on success.

### A single language

1. Update `languages/<lang>/CHANGELOG.md`.
2. Bump `VERSION_NAME` in `languages/<lang>/gradle.properties`.
3. Add a new row to `docs/compatibility.md`.
4. Open and merge the PR.
5. From the GitHub Actions UI, run **Publish** with `module=languages/<lang>`.
6. The workflow tags the commit `<lang>-v<version>` on success.

The `dry-run` input runs every step except the Maven Central upload and the
tag push, which is useful for verifying configuration changes.

## Runbook: ktreesitter accept window narrows

When the catalog-pinned `ktreesitter` bumps to a version that drops support for
an old ABI, we major-bump `:core-api` and re-publish every language module so
that each artefact carries the new strict range. We follow ktreesitter's own
range semantics: a narrowing event on their side is treated as a major event on
ours.

There are two reasons every language re-publishes, not only the ones that fall
outside the new window:

- **Languages whose parser ABI is outside the new window** need a submodule pin
  update before they can be re-published. Their parser would otherwise fail
  ktreesitter's runtime ABI assertion.
- **Languages whose parser ABI is still inside the new window** are functionally
  unchanged, but the strict range baked into their Gradle Module Metadata still
  points at the old `coreApiCompatibleRange`. A consumer that upgrades the core
  stack to the new major sees a `strictly` conflict on
  `compose-syntax-highlight-api` until those languages are also re-published
  against the new range.

Steps:

- [ ] Update `gradle/libs.versions.toml`: bump `ktreesitter` to the version
      with the new window. Bump `coreApiCompatibleRange` to the next major
      (e.g. `"[1.0, 2.0)"` → `"[2.0, 3.0)"`).
- [ ] Inspect each language's parser ABI to identify the out-of-window ones,
      for example:

      ```bash
      grep -H 'LANGUAGE_VERSION' languages/*/tree-sitter-*/src/parser.c
      ```

      Compare each result against the new ktreesitter window.
- [ ] For each out-of-window language: update the grammar submodule pin to a
      commit whose `parser.c` is inside the new window. Update the language's
      `CHANGELOG.md` with the ABI change.
- [ ] For each in-window language: add an entry to its `CHANGELOG.md` noting
      "compat re-publish for `:core-api` major X".
- [ ] Bump the root `VERSION_NAME` to the new major and update root
      `CHANGELOG.md`.
- [ ] Publish the core stack (`module=core-stack`).
- [ ] For every language module, bump its `gradle.properties` `VERSION_NAME`
      (typically a minor bump — coordinated, not driven by per-language
      changes) and publish (`module=languages/<lang>`).
- [ ] Append rows to `docs/compatibility.md` for every artefact published.

Until every language module has been re-published, consumers upgrading the core
stack across this major will see a Gradle `strictly` conflict on
`compose-syntax-highlight-api`. The error message references
`docs/compatibility.md`, which lists the language module versions compatible
with each `:core-api` range.
