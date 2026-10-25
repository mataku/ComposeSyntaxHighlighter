#!/usr/bin/env bash
# Verify that every language module's JVM jar bundles the macOS grammar
# library for both architectures at the path the generated loader probes,
# and that the sources jar does not.
#
# Usage:
#   bash scripts/verify-jvm-natives.sh
#       Check all language modules. Expects `./gradlew jvmJar jvmSourcesJar`
#       to have run first.
#
#   bash scripts/verify-jvm-natives.sh kotlin typescript ...
#       Check only the given language directories under languages/.
#
# Requires macOS: lipo, otool and codesign come from the Xcode command line tools.

set -euo pipefail

expected_minos="26.0"

if [[ "$(uname -s)" != "Darwin" ]]; then
  echo "FAIL: this script needs macOS tooling (lipo/otool/codesign)" >&2
  exit 1
fi

tmp="$(mktemp -d)"
trap 'rm -rf "${tmp}"' EXIT

slices=(
  aarch64:arm64
  x64:x86_64
)

check_slice() {
  local lang="$1"
  local jar="$2"
  local dir="$3"
  local arch="$4"
  local entry="lib/macos/${dir}/libktreesitter-${lang}.dylib"
  local out="${tmp}/${lang}-${dir}.dylib"

  if ! unzip -p "${jar}" "${entry}" >"${out}" 2>/dev/null || [[ ! -s "${out}" ]]; then
    echo "FAIL: ${jar} is missing ${entry}" >&2
    exit 1
  fi

  local archs
  archs="$(lipo -archs "${out}")"
  if [[ "${archs}" != "${arch}" ]]; then
    echo "FAIL: ${entry} has archs '${archs}', expected '${arch}'" >&2
    exit 1
  fi

  local minos
  minos="$(otool -l "${out}" | grep -A3 'LC_BUILD_VERSION' | awk '/minos/ { print $2 }')"
  if [[ "${minos}" != "${expected_minos}" ]]; then
    echo "FAIL: ${entry} has minos '${minos}', expected '${expected_minos}'" >&2
    exit 1
  fi

  if ! codesign -dv "${out}" >/dev/null 2>&1; then
    echo "FAIL: ${entry} is not code-signed" >&2
    exit 1
  fi
}

check_module() {
  local lang="$1"
  local jar="languages/${lang}/build/libs/${lang}-jvm.jar"
  local sources="languages/${lang}/build/libs/${lang}-jvm-sources.jar"

  for f in "${jar}" "${sources}"; do
    if [[ ! -f "${f}" ]]; then
      echo "FAIL: artifact missing: ${f} (run ./gradlew jvmJar jvmSourcesJar first)" >&2
      exit 1
    fi
  done

  for slice in "${slices[@]}"; do
    check_slice "${lang}" "${jar}" "${slice%%:*}" "${slice##*:}"
  done

  if unzip -l "${sources}" | awk 'NR > 3 { print $4 }' | grep -q '^lib/'; then
    echo "FAIL: ${sources} contains native libraries under lib/" >&2
    exit 1
  fi

  echo "OK: ${lang}-jvm.jar bundles macos aarch64 + x64 slices"
}

default_langs=(
  kotlin
  swift
  ruby
  rust
  python
  go
  java
  markdown
  javascript
  typescript
)

langs=("${@:-${default_langs[@]}}")

for lang in "${langs[@]}"; do
  check_module "${lang}"
done
