#!/usr/bin/env bash
# Verify the iOS grammar static libraries built by the language modules: every object in
# languages/<name>/build/libs/ios_arm64/*.a is built for iOS devices and every object in
# languages/<name>/build/libs/ios_simulator_arm64/*.a is built for the iOS simulator.
# These libraries are embedded in the published cinterop klibs, where a wrong platform only
# surfaces as a link error in the consuming app.
#
# Usage:
#   bash scripts/verify-ios-static-libs.sh
#       Run from the repository root after building both iOS targets.
#
# Requires otool (Xcode command line tools).

set -euo pipefail

# LC_BUILD_VERSION platform numbers.
platform_ios=2
platform_ios_simulator=7

checked=0
failed=0

check_dir() {
  local target="$1" want="$2" lib platforms
  for lib in languages/*/build/libs/"$target"/*.a; do
    [[ -f "$lib" ]] || continue
    checked=$((checked + 1))
    platforms="$(otool -l "$lib" | awk '/LC_BUILD_VERSION/ { in_cmd = 1 } in_cmd && $1 == "platform" { print $2; in_cmd = 0 }' | sort -u | tr '\n' ' ')"
    if [[ "$platforms" != "$want " ]]; then
      failed=$((failed + 1))
      echo "FAIL $lib: objects built for platform(s) [${platforms% }], expected $want" >&2
    fi
  done
}

check_dir ios_arm64 "$platform_ios"
check_dir ios_simulator_arm64 "$platform_ios_simulator"

if (( checked == 0 )); then
  echo "No iOS static libraries found under languages/*/build/libs; build the iOS targets first." >&2
  exit 1
fi

echo "Checked $checked iOS static libraries, $failed with objects for the wrong platform."
(( failed == 0 ))
