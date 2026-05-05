#!/usr/bin/env bash
# Verify META-INF/NOTICE is present in every published language artifact.
# Pass the version as $1 (defaults to VERSION_NAME from gradle.properties).

set -euo pipefail

version="${1:-$(grep '^VERSION_NAME=' gradle.properties | cut -d= -f2)}"
group_path="io/github/mataku"
repo="${HOME}/.m2/repository"

modules=(
  compose-highlight-kotlin
  compose-highlight-swift
)

variants=(
  android:aar
  jvm:jar
)

for module in "${modules[@]}"; do
  for variant_ext in "${variants[@]}"; do
    variant="${variant_ext%%:*}"
    ext="${variant_ext##*:}"
    artifact="${module}-${variant}-${version}.${ext}"
    file="${repo}/${group_path}/${module}-${variant}/${version}/${artifact}"

    if [[ ! -f "${file}" ]]; then
      echo "FAIL: artifact missing: ${file}" >&2
      exit 1
    fi

    if unzip -p "${file}" 'META-INF/NOTICE' >/dev/null 2>&1; then
      echo "OK: ${artifact} contains META-INF/NOTICE"
      continue
    fi

    if [[ "${ext}" == "aar" ]]; then
      tmp="$(mktemp -d)"
      trap 'rm -rf "${tmp}"' EXIT
      unzip -q "${file}" -d "${tmp}"
      if unzip -p "${tmp}/classes.jar" 'META-INF/NOTICE' >/dev/null 2>&1; then
        echo "OK: ${artifact}/classes.jar contains META-INF/NOTICE"
        rm -rf "${tmp}"
        trap - EXIT
        continue
      fi
      rm -rf "${tmp}"
      trap - EXIT
    fi

    echo "FAIL: META-INF/NOTICE missing in ${artifact}" >&2
    exit 1
  done
done
