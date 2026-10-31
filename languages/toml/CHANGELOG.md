# Changelog — compose-syntax-highlight-toml

## Unreleased

- Nothing yet.

## v0.10.0

- The JVM jar bundles the Linux grammar library for `x64` and `aarch64` at `lib/linux/<arch>/`, loaded automatically on Linux with glibc 2.17 or newer.
- Compatibility re-publish against the core stack 0.10.0; the `compose-syntax-highlight-api` range stays `[0.6, 1.0)`.

## v0.9.0

- Initial release. Bundles tree-sitter-toml v0.7.0.
