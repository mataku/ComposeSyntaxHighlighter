#!/usr/bin/env bash
# Verify META-INF/NOTICE is present in every published artifact.
#
# Usage:
#   bash scripts/verify-notice.sh
#       Check all artifacts. Each module's version is resolved from its own
#       gradle.properties (root for core stack; languages/<lang>/gradle.properties
#       for language modules).
#
#   bash scripts/verify-notice.sh :core :languages:kotlin ...
#       Check only the artifacts corresponding to the given Gradle project paths.
#       Version resolution is the same as the full-sweep mode.

set -euo pipefail

group_path="io/github/mataku"
repo="${HOME}/.m2/repository"

variants=(
  android:aar
  jvm:jar
)

# Per-module keywords that must appear in the NOTICE body. Empty = presence check only.
required_keywords_compose_syntax_highlight_core=("Solarized" "GitHub Primer" "Atom One" "Dracula")

required_keywords_for() {
  case "$1" in
    compose-syntax-highlight-core) printf '%s\n' "${required_keywords_compose_syntax_highlight_core[@]}" ;;
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

check_module() {
  local module="$1"
  local version="$2"
  for variant_ext in "${variants[@]}"; do
    local variant="${variant_ext%%:*}"
    local ext="${variant_ext##*:}"
    local artifact="${module}-${variant}-${version}.${ext}"
    local file="${repo}/${group_path}/${module}-${variant}/${version}/${artifact}"

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
}

# Map a Gradle project path to the artifact name and the version properties file.
# Sets globals: _module_name, _version_file
resolve_project() {
  local project="$1"
  case "$project" in
    :core-api)
      _module_name="compose-syntax-highlight-api"
      _version_file="gradle.properties"
      ;;
    :core)
      _module_name="compose-syntax-highlight-core"
      _version_file="gradle.properties"
      ;;
    :material3)
      _module_name="compose-syntax-highlight-material3"
      _version_file="gradle.properties"
      ;;
    :material3-text-field)
      _module_name="compose-syntax-highlight-material3-text-field"
      _version_file="gradle.properties"
      ;;
    :languages:*)
      local lang="${project#:languages:}"
      _module_name="compose-syntax-highlight-${lang}"
      _version_file="languages/${lang}/gradle.properties"
      ;;
    *)
      echo "Unknown project path: $project" >&2
      exit 1
      ;;
  esac
}

default_projects=(
  :core-api
  :core
  :material3
  :material3-text-field
  :languages:kotlin
  :languages:swift
  :languages:ruby
  :languages:rust
  :languages:python
  :languages:go
  :languages:java
  :languages:markdown
  :languages:javascript
  :languages:typescript
)

projects=("${@:-${default_projects[@]}}")

for project in "${projects[@]}"; do
  _module_name=""
  _version_file=""
  resolve_project "$project"
  version="$(grep '^VERSION_NAME=' "$_version_file" | cut -d= -f2)"
  if [[ -z "$version" ]]; then
    echo "VERSION_NAME not found in $_version_file" >&2
    exit 1
  fi
  check_module "$_module_name" "$version"
done
