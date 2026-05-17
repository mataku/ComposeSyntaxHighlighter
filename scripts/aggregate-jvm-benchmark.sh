#!/usr/bin/env bash
#
# aggregate-jvm-benchmark.sh
#
# Runs :benchmarks:jvm:jvmTest 3 times, parses the per-stage Min/Median/Max
# markdown tables emitted by BenchmarkConfig.printMarkdown, and writes an
# aggregated report:
#   - per stage group: a "main" table copied from the run whose median values
#     deviate least from the median-of-medians vector
#   - per stage group: a variance band table reporting Run 1 / Run 2 / Run 3
#     medians and the spread (max - min) / median as a percent
#
# Usage: bash scripts/aggregate-jvm-benchmark.sh [output-dir]
#   output-dir defaults to a fresh tmp dir under $TMPDIR.
# Set SKIP_RUNS=true to bypass gradle invocations and use existing
# $output-dir/run{1,2,3}.txt files (for smoke-testing the parser/aggregator).

set -euo pipefail

OUT_DIR="${1:-$(mktemp -d -t jvm-bench-XXXXXX)}"
SKIP_RUNS="${SKIP_RUNS:-false}"
mkdir -p "$OUT_DIR"

GRADLE_ARGS=(
  ":benchmarks:jvm:jvmTest"
  "--tests" "*LargeInputBenchmark"
  "--tests" "*IncrementalHighlighterBenchmark"
  "--tests" "*BenchmarkSuite"
  "--tests" "*HighlightBenchmark"
  "--rerun-tasks"
  "-i"
)

if [ "$SKIP_RUNS" = "true" ]; then
  echo "[aggregate-jvm-benchmark] SKIP_RUNS=true — using existing $OUT_DIR/run{1,2,3}.txt" >&2
  for run in 1 2 3; do
    test -f "$OUT_DIR/run${run}.txt" || { echo "missing $OUT_DIR/run${run}.txt" >&2; exit 1; }
  done
else
  for run in 1 2 3; do
    echo "[aggregate-jvm-benchmark] run $run/3 — output: $OUT_DIR/run${run}.txt" >&2
    ./gradlew "${GRADLE_ARGS[@]}" 2>&1 | tee "$OUT_DIR/run${run}.txt" >/dev/null
  done
fi

awk '
  # Sectioning: lines like "=== 100 lines (...) ===" or "=== Subsequent use [Warm] ===" mark a group.
  # Tolerate leading whitespace from gradle -i prefix.
  /^[[:space:]]*=== / {
    section = $0
    sub(/^[[:space:]]*=== */, "", section)
    sub(/ *===[[:space:]]*$/, "", section)
    in_table = 0
    next
  }
  # Markdown tables open with "| Stage" or "| Language" (also tolerant of leading whitespace).
  /^[[:space:]]*\| (Stage|Language) / {
    in_table = 1
    next
  }
  /^[[:space:]]*\|[- :|]+\|[[:space:]]*$/ {
    next   # skip header separator
  }
  in_table && /^[[:space:]]*\|/ {
    # | name | min | median | max |  → 6 cells (incl. leading/trailing empties from split)
    n = split($0, cells, /[[:space:]]*\|[[:space:]]*/)
    if (n == 6 && cells[2] != "") {
      key = run "\t" section "\t" cells[2]
      printf "%s\t%s\t%s\t%s\n", key, cells[3], cells[4], cells[5]
    }
    # Skip 4-cell rows intentionally (BenchmarkSuite Cold table is hand-rolled
    # | Language | ms |). Cold values are aggregated manually downstream.
    next
  }
  /^[[:space:]]*$/ { in_table = 0 }
' \
  run=1 "$OUT_DIR/run1.txt" \
  run=2 "$OUT_DIR/run2.txt" \
  run=3 "$OUT_DIR/run3.txt" > "$OUT_DIR/parsed.tsv"

# parsed.tsv columns: run \t section \t stage \t min \t median \t max  (all ms strings)

python3 - "$OUT_DIR" <<'PY'
import os, sys, statistics, collections

out_dir = sys.argv[1]
rows = []
with open(os.path.join(out_dir, "parsed.tsv")) as f:
    for line in f:
        parts = line.rstrip("\n").split("\t")
        if len(parts) != 6:
            continue
        run, section, stage, mn, med, mx = parts
        rows.append((int(run), section, stage, float(mn), float(med), float(mx)))

# group by (section, stage); produce {section: {stage: {run: (min, median, max)}}}
groups = collections.defaultdict(lambda: collections.defaultdict(dict))
for run, section, stage, mn, med, mx in rows:
    groups[section][stage][run] = (mn, med, mx)

print(f"# Aggregated JVM benchmark report ({out_dir})\n")

for section, stages in groups.items():
    print(f"## {section}\n")

    # variance band first
    print("### Run-to-run variance band\n")
    print("| Stage | Run 1 median (ms) | Run 2 median (ms) | Run 3 median (ms) | Spread % |")
    print("|-------|------------------:|------------------:|------------------:|---------:|")
    median_vec = {}
    for stage, runs in stages.items():
        if set(runs) != {1, 2, 3}:
            continue
        meds = [runs[r][1] for r in (1, 2, 3)]
        spread = 100 * (max(meds) - min(meds)) / statistics.median(meds) if statistics.median(meds) > 0 else 0.0
        median_vec[stage] = meds
        print(f"| {stage} | {meds[0]:.3f} | {meds[1]:.3f} | {meds[2]:.3f} | {spread:.1f} |")
    print()

    # main table: pick run minimizing sum of |median - median-of-medians|
    median_of_medians = {stage: statistics.median(meds) for stage, meds in median_vec.items()}
    def deviation(run):
        s = 0.0
        for stage, meds in median_vec.items():
            s += abs(meds[run - 1] - median_of_medians[stage])
        return s
    best = min((1, 2, 3), key=lambda r: (deviation(r), r))
    print(f"### Main table (best run: Run {best})\n")
    print("| Stage | Min (ms) | Median (ms) | Max (ms) |")
    print("|-------|---------:|------------:|---------:|")
    for stage, runs in stages.items():
        if best not in runs:
            continue
        mn, med, mx = runs[best]
        print(f"| {stage} | {mn:.3f} | {med:.3f} | {mx:.3f} |")
    print()
PY

echo "[aggregate-jvm-benchmark] done — raw runs in $OUT_DIR" >&2
