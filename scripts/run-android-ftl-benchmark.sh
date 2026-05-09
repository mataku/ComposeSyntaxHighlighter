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

if ! gcloud config get-value project >/dev/null 2>&1; then
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
  --device=model=bluejay,version=32,locale=en,orientation=portrait \
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

gsutil -m cp -r "${gcs_path}*" ftl-results/

echo "==> Done. JSON files:"
find ftl-results -name '*-benchmarkData.json' | sort
