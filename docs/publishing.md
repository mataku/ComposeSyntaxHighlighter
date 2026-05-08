# Publishing

## Local publish + NOTICE verification

```bash
./gradlew publishToMavenLocal -PRELEASE_SIGNING_ENABLED=false --no-configuration-cache
bash scripts/verify-notice.sh
```

`scripts/verify-notice.sh` walks each language artifact (Android AAR + JVM JAR) and asserts that `META-INF/NOTICE` is present (descending into `classes.jar` for AARs).

## CI

`.github/workflows/build.yml` runs JVM tests, the Android demo build, and NOTICE verification on every PR / push to main / develop. `.github/workflows/publish.yml` runs `publishToMavenCentral` on every `v*` tag push using vanniktech with in-memory signing keys from repository secrets.

## Local Maven Central publishing

For ad-hoc publishes (e.g. cutting a SNAPSHOT for downstream verification) `settings.gradle.kts` loads the following keys from `local.properties` (gitignored) and exposes them as gradle properties:

- `mavenCentralUsername` / `mavenCentralPassword` — Central Portal user token
- `signingInMemoryKey` / `signingInMemoryKeyId` / `signingInMemoryKeyPassword` — GPG signing material (the key may be base64-encoded for single-line storage)

Environment variables (`ORG_GRADLE_PROJECT_*`) take precedence over `local.properties`, so CI continues to work unchanged.

## SNAPSHOT vs release

- Set `VERSION_NAME=0.1.0-SNAPSHOT` in `gradle.properties` and run `./gradlew publishToMavenCentral --no-configuration-cache` to push to Central Portal's snapshot repository (`https://central.sonatype.com/repository/maven-snapshots/`). Snapshots are auto-published with no manual gate.
- For a release, set `VERSION_NAME=0.1.0`, push a `v0.1.0` tag to fire `publish.yml`, then click **Publish** on the Central Portal Deployments page (the workflow uses `publishToMavenCentral`, which uploads to staging without auto-releasing).
