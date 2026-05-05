#!/usr/bin/env bash
# Verify META-INF/NOTICE is present in every published language artifact.
# Pass the version as $1 (defaults to VERSION_NAME from gradle.properties).

set -euo pipefail

version="${1:-$(grep '^VERSION_NAME=' gradle.properties | cut -d= -f2)}"
group_path="io/github/mataku"
repo="${HOME}/.m2/repository"

modules=(
  compose-highlight-core
  compose-highlight-kotlin
  compose-highlight-swift
  compose-highlight-ruby
  compose-highlight-rust
  compose-highlight-python
  compose-highlight-go
  compose-highlight-java
)

variants=(
  android:aar
  jvm:jar
)

# Per-module keywords that must appear in the NOTICE body. Empty = presence check only.
required_keywords_compose_highlight_core=("Solarized" "GitHub Primer" "Atom One" "Dracula")

required_keywords_for() {
  case "$1" in
    compose-highlight-core) printf '%s\n' "${required_keywords_compose_highlight_core[@]}" ;;
    *) ;;
  esac
}

assert_keywords() {
  local module="$1"
  local source_label="$2"
  local notice_content="$3"
  local missing=()
  while IFS= read -r kw; do
    [[ -z "${kw}" ]] && continue
    if ! grep -qF -- "${kw}" <<<"${notice_content}"; then
      missing+=("${kw}")
    fi
  done < <(required_keywords_for "${module}")
  if (( ${#missing[@]} > 0 )); then
    echo "FAIL: ${source_label} missing required attributions: ${missing[*]}" >&2
    exit 1
  fi
}

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

    if notice_content="$(unzip -p "${file}" 'META-INF/NOTICE' 2>/dev/null)" && [[ -n "${notice_content}" ]]; then
      assert_keywords "${module}" "${artifact}" "${notice_content}"
      echo "OK: ${artifact} contains META-INF/NOTICE"
      continue
    fi

    if [[ "${ext}" == "aar" ]]; then
      tmp="$(mktemp -d)"
      trap 'rm -rf "${tmp}"' EXIT
      unzip -q "${file}" -d "${tmp}"
      if notice_content="$(unzip -p "${tmp}/classes.jar" 'META-INF/NOTICE' 2>/dev/null)" && [[ -n "${notice_content}" ]]; then
        assert_keywords "${module}" "${artifact}/classes.jar" "${notice_content}"
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
