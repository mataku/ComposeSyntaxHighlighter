#!/usr/bin/env bash
# Verify that every language module's JVM jar bundles the macOS and Linux
# grammar libraries for both architectures at the paths the generated loader
# probes, and that the sources jar does not.
#
# Usage:
#   bash scripts/verify-jvm-natives.sh
#       Check every `:languages:<name>` module included in settings.gradle.kts.
#       Expects `./gradlew jvmJar jvmSourcesJar -PprebuiltJvmNativesDir=<dir>`
#       to have run first, with <dir> holding the Linux libraries.
#
#   bash scripts/verify-jvm-natives.sh kotlin typescript ...
#       Check only the given language directories under languages/.
#
# Requires macOS: lipo, otool and codesign come from the Xcode command line tools.
# The Linux libraries' glibc floor is checked separately by verify-linux-natives.sh.

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

linux_slices=(
  "aarch64:ARM aarch64"
  "x64:x86-64"
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

check_linux_slice() {
  local lang="$1"
  local jar="$2"
  local dir="$3"
  local machine="$4"
  local entry="lib/linux/${dir}/libktreesitter-${lang}.so"
  local out="${tmp}/${lang}-linux-${dir}.so"

  if ! unzip -p "${jar}" "${entry}" >"${out}" 2>/dev/null || [[ ! -s "${out}" ]]; then
    echo "FAIL: ${jar} is missing ${entry}" >&2
    exit 1
  fi

  local description
  description="$(file -b "${out}")"
  if [[ "${description}" != "ELF 64-bit LSB shared object, ${machine},"* ]]; then
    echo "FAIL: ${entry} is '${description}', expected an ELF shared object for ${machine}" >&2
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

  for slice in "${linux_slices[@]}"; do
    check_linux_slice "${lang}" "${jar}" "${slice%%:*}" "${slice#*:}"
  done

  if unzip -l "${sources}" | awk 'NR > 3 { print $4 }' | grep -q '^lib/'; then
    echo "FAIL: ${sources} contains native libraries under lib/" >&2
    exit 1
  fi

  echo "OK: ${lang}-jvm.jar bundles macos + linux aarch64 + x64 slices"
}

default_langs=()
while IFS= read -r lang; do
  default_langs+=("${lang}")
done < <(sed -n 's/^include(":languages:\([^"]*\)")$/\1/p' settings.gradle.kts)

if (( ${#default_langs[@]} == 0 )); then
  echo "FAIL: no include(\":languages:<name>\") entries found in settings.gradle.kts" >&2
  exit 1
fi

langs=("${@:-${default_langs[@]}}")

for lang in "${langs[@]}"; do
  check_module "${lang}"
done
