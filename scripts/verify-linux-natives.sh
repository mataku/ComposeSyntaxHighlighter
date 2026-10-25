#!/usr/bin/env bash
# Verify the Linux grammar libraries staged for bundling into the JVM jars: every language
# module has a library per architecture, built for that architecture, and requiring no glibc
# symbol newer than ktreesitter-jvm's own libktreesitter.so does (GLIBC_2.17).
#
# Usage:
#   bash scripts/verify-linux-natives.sh <dir> <x64|aarch64>
#       <dir> holds lib/linux/<arch>/libktreesitter-<name>.so for every `:languages:<name>`
#       module included in settings.gradle.kts.
#
#   bash scripts/verify-linux-natives.sh <dir> <x64|aarch64> kotlin typescript ...
#       Check only the given language directories under languages/.
#
# Requires readelf (binutils).

set -euo pipefail

max_glibc="2.17"

if (( $# < 2 )); then
  echo "usage: $0 <dir> <x64|aarch64> [name ...]" >&2
  exit 1
fi

root="$1"
arch="$2"
shift 2

elf_machine() {
  case "$1" in
    x64) echo "Advanced Micro Devices X86-64" ;;
    aarch64) echo "AArch64" ;;
    *)
      echo "FAIL: unsupported architecture '$1'" >&2
      exit 1
      ;;
  esac
}

check_library() {
  local lib="$1"
  local arch="$2"

  if [[ ! -s "${lib}" ]]; then
    echo "FAIL: missing ${lib}" >&2
    exit 1
  fi

  local machine expected
  machine="$(readelf -h "${lib}" | sed -n 's/^ *Machine: *//p')"
  expected="$(elf_machine "${arch}")"
  if [[ "${machine}" != "${expected}" ]]; then
    echo "FAIL: ${lib} is built for '${machine}', expected '${expected}'" >&2
    exit 1
  fi

  local newest
  newest="$(readelf -W --version-info "${lib}" | { grep -o 'GLIBC_[0-9.]*' || true; } | sed 's/^GLIBC_//' | sort -uV | tail -1)"
  if [[ -n "${newest}" ]] && [[ "$(printf '%s\n%s\n' "${newest}" "${max_glibc}" | sort -V | tail -1)" != "${max_glibc}" ]]; then
    echo "FAIL: ${lib} requires GLIBC_${newest}, newer than GLIBC_${max_glibc}" >&2
    exit 1
  fi

  echo "OK: ${lib} (${arch}, newest GLIBC_${newest:-none})"
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
  check_library "${root}/lib/linux/${arch}/libktreesitter-${lang}.so" "${arch}"
done
