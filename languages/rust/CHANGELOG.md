# Changelog — compose-syntax-highlight-rust

## Unreleased

- Nothing yet.

## v0.7.0

- The JVM jar bundles the macOS grammar library for `aarch64` and `x64` at `lib/macos/<arch>/`, loaded automatically when `java.library.path` has no copy; requires macOS 26.0 or newer.
- Compatibility re-publish against the core stack 0.7.0; the `compose-syntax-highlight-api` range stays `[0.6, 1.0)`.

## v0.6.0

- Add iOS targets (iosArm64, iosSimulatorArm64).
- Bump tree-sitter-rust to v0.24.2 (tree-sitter ABI 15).
