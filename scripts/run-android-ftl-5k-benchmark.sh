#!/usr/bin/env bash
# Run the flagship-only 5k Android FTL benchmark and download results.
#
# Flagship-class only (Pixel 10 / 16 GB RAM tier). The single test in
# LargeInputAndroid5kBenchmark probes whether 5k full-highlight survives the
# same BenchmarkRule.measureRepeated tight loop that excludes 5k from the
# regular suite. Lower-spec devices are expected to OOM and are not targeted.
# Scudo OOM may still happen on Pixel 10 — accept the binary outcome.
#
# Costs 1/5 of the daily Spark-plan device-test quota. NOT run as part of
# scripts/run-android-ftl-benchmark.sh — opt-in only.
#
# Prerequisites (one-time):
#   gcloud auth login
#   gcloud config set project <your-gcp-project-id>
#
# Outputs:
#   ./ftl-results-5k/<device>-<api>-<locale>-<orientation>/*-benchmarkData.json
#
# After running, paste the median into the README.md `5k lines` Android cell
# and add a 5k subsection to docs/large_input_profiling.md (under
# "Decomposition") alongside device, API level, run date, and source commit.
# If the run OOMs, add a short note (failure mode, device, source SHA, run
# date) to LargeInputAndroid5kBenchmark.kt's class-level comment instead.

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
  :samples:androidApp:assembleBenchmark \
  :benchmarks:android:assembleBenchmarkAndroidTest

app_apk=$(find samples/androidApp/build/outputs/apk/benchmark -name '*.apk' | head -1)
test_apk=$(find benchmarks/android/build/outputs/apk/androidTest/benchmark -name '*.apk' | head -1)

if [ -z "$app_apk" ] || [ -z "$test_apk" ]; then
  echo "Could not locate built APKs" >&2
  echo "  app_apk=$app_apk" >&2
  echo "  test_apk=$test_apk" >&2
  exit 1
fi

echo "==> Submitting flagship-only 5k benchmark to Firebase Test Lab"
echo "    host: $app_apk"
echo "    test: $test_apk"

mkdir -p ftl-results-5k

gcloud firebase test android run \
  --type=instrumentation \
  --app="$app_apk" \
  --test="$test_apk" \
  --device=model=frankel,version=36,locale=en,orientation=portrait \
  --directories-to-pull=/sdcard/Android/media/io.github.mataku.compose.highlight.benchmark.test/additional_test_output \
  --environment-variables=additionalTestOutputDir=/sdcard/Android/media/io.github.mataku.compose.highlight.benchmark.test/additional_test_output,no-isolated-storage=true \
  --test-targets="class io.github.mataku.compose.highlight.benchmark.LargeInputAndroid5kBenchmark" \
  --timeout=30m \
  --no-record-video \
  --no-performance-metrics \
  2>&1 | tee ftl-output-5k.log

gcs_path=$(grep -oE 'storage/browser/test-lab-[a-z0-9-]+/[^/]+/' ftl-output-5k.log \
  | head -1 \
  | sed 's|storage/browser/|gs://|')

if [ -z "$gcs_path" ]; then
  echo "Could not extract GCS results path from gcloud output" >&2
  exit 1
fi

echo "==> Results path: $gcs_path"
echo "==> Downloading results to ./ftl-results-5k"

gcloud storage cp --recursive "${gcs_path}*" ftl-results-5k/

echo "==> Done. JSON files:"
find ftl-results-5k -name '*-benchmarkData.json' | sort

if ! command -v jq >/dev/null 2>&1; then
  echo
  echo "(Install jq to print a markdown summary table. Skipping summary.)"
  exit 0
fi

echo
echo "==> Summary (paste into README.md and docs/large_input_profiling.md)"
for device_dir in ftl-results-5k/*/; do
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
