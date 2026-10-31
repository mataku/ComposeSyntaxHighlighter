#!/usr/bin/env bash
# Load every language module's grammar from its JVM jar alone: java.library.path points at
# an empty directory, so the generated loader must fall back to the library bundled in the jar.
#
# Usage:
#   bash scripts/verify-jvm-natives-load.sh
#       Load every `:languages:<name>` module included in settings.gradle.kts.
#       Expects languages/<name>/build/libs/<name>-jvm.jar to exist.
#
#   bash scripts/verify-jvm-natives-load.sh kotlin typescript ...
#       Load only the given language directories under languages/.
#
# Downloads kotlin-stdlib and ktreesitter-jvm from Maven Central at the versions pinned in
# gradle/libs.versions.toml.

set -euo pipefail

tmp="$(mktemp -d)"
trap 'rm -rf "${tmp}"' EXIT

catalog_version() {
  local value
  value="$(sed -n "s/^$1 = \"\([^\"]*\)\"$/\1/p" gradle/libs.versions.toml)"
  if [[ -z "${value}" ]]; then
    echo "FAIL: version '$1' not found in gradle/libs.versions.toml" >&2
    exit 1
  fi
  echo "${value}"
}

fetch() {
  local group_path="$1"
  local artifact="$2"
  local version="$3"
  curl -sSfL --retry 5 -o "${tmp}/${artifact}.jar" \
    "https://repo1.maven.org/maven2/${group_path}/${artifact}/${version}/${artifact}-${version}.jar"
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

jars=()
for lang in "${langs[@]}"; do
  jar="languages/${lang}/build/libs/${lang}-jvm.jar"
  if [[ ! -f "${jar}" ]]; then
    echo "FAIL: artifact missing: ${jar}" >&2
    exit 1
  fi
  jars+=("${jar}")
done

kotlin_version="$(catalog_version kotlin)"
ktreesitter_version="$(catalog_version ktreesitter)"
fetch org/jetbrains/kotlin kotlin-stdlib "${kotlin_version}"
fetch io/github/tree-sitter ktreesitter-jvm "${ktreesitter_version}"
classpath="$(IFS=:; echo "${tmp}/kotlin-stdlib.jar:${tmp}/ktreesitter-jvm.jar:${jars[*]}")"

mkdir "${tmp}/empty-library-path"
java -Djava.library.path="${tmp}/empty-library-path" -cp "${classpath}" \
  scripts/LoadGrammars.java "${jars[@]}"
