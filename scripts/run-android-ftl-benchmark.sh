#!/usr/bin/env bash
# Run the Android FTL benchmark suite locally and download results.
#
# Prerequisites (one-time):
#   gcloud auth login
#   gcloud config set project <your-gcp-project-id>
#
# Outputs:
#   ./ftl-results/<device>-<api>-<locale>-<orientation>/*-benchmarkData.json
#
# After running, paste the numbers you judge representative into
# docs/large_input_profiling.md (and the small full-highlight table in
# README.md). Always include device, API level, run date, and source
# commit alongside the numbers.

set -euo pipefail

cd "$(dirname "$0")/.."

if ! command -v gcloud >/dev/null 2>&1; then
  echo "gcloud CLI not found on PATH" >&2
  exit 1
fi

project=$(gcloud config get-value project 2>/dev/null || true)
if [ -z "$project" ] || [ "$project" = "(unset)" ]; then
  echo "No gcloud project configured. Run: gcloud config set project <id>" >&2
  exit 1
fi

echo "==> Building host and androidTest APKs"
./gradlew \
  :composeApp:assembleBenchmark \
  :benchmark:assembleBenchmarkAndroidTest

app_apk=$(find composeApp/build/outputs/apk/benchmark -name '*.apk' | head -1)
test_apk=$(find benchmark/build/outputs/apk/androidTest/benchmark -name '*.apk' | head -1)

if [ -z "$app_apk" ] || [ -z "$test_apk" ]; then
  echo "Could not locate built APKs" >&2
  echo "  app_apk=$app_apk" >&2
  echo "  test_apk=$test_apk" >&2
  exit 1
fi

echo "==> Submitting to Firebase Test Lab"
echo "    host: $app_apk"
echo "    test: $test_apk"

mkdir -p ftl-results

gcloud firebase test android run \
  --type=instrumentation \
  --app="$app_apk" \
  --test="$test_apk" \
  --device=model=frankel,version=36,locale=en,orientation=portrait \
  --directories-to-pull=/sdcard/Android/media/io.github.mataku.compose.highlight.benchmark.test/additional_test_output \
  --environment-variables=additionalTestOutputDir=/sdcard/Android/media/io.github.mataku.compose.highlight.benchmark.test/additional_test_output,no-isolated-storage=true \
  --test-targets="class io.github.mataku.compose.highlight.benchmark.LargeInputAndroidBenchmark,class io.github.mataku.compose.highlight.benchmark.IncrementalHighlighterAndroidBenchmark" \
  --timeout=30m \
  --no-record-video \
  --no-performance-metrics \
  2>&1 | tee ftl-output.log

gcs_path=$(grep -oE 'storage/browser/test-lab-[a-z0-9-]+/[^/]+/' ftl-output.log \
  | head -1 \
  | sed 's|storage/browser/|gs://|')

if [ -z "$gcs_path" ]; then
  echo "Could not extract GCS results path from gcloud output" >&2
  exit 1
fi

echo "==> Results path: $gcs_path"
echo "==> Downloading results to ./ftl-results"

gcloud storage cp --recursive "${gcs_path}*" ftl-results/

echo "==> Done. JSON files:"
find ftl-results -name '*-benchmarkData.json' | sort

if ! command -v jq >/dev/null 2>&1; then
  echo
  echo "(Install jq to print a markdown summary table. Skipping summary.)"
  exit 0
fi

echo
echo "==> Summary (paste into docs/large_input_profiling.md after judging representativeness)"
for device_dir in ftl-results/*/; do
  device_label=$(basename "$device_dir")
  jsons=$(find "$device_dir" -name '*-benchmarkData.json' 2>/dev/null | sort || true)
  echo
  echo "### Device: $device_label"
  if [ -z "$jsons" ]; then
    echo "_No benchmark JSON output was found for this device._"
    continue
  fi
  echo
  echo "| Test | Min (ms) | Median (ms) | Max (ms) |"
  echo "| --- | ---: | ---: | ---: |"
  for j in $jsons; do
    jq -r '
      def round3: . * 1000 | round / 1000;
      .benchmarks[]
      | "| \(.className | split(".") | last).\(.name) | \((.metrics.timeNs.minimum / 1000000) | round3) | \((.metrics.timeNs.median / 1000000) | round3) | \((.metrics.timeNs.maximum / 1000000) | round3) |"
    ' "$j"
  done
done
