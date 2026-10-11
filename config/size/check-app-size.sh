#!/bin/bash
# Compares the release bundle's Pixel-class download size with config/size/download-size.txt.
# Run after ./gradlew :app:bundleRelease. Pass --update to record the current size as the baseline.
# Uses $BUNDLETOOL_JAR when set, otherwise `bundletool` on the PATH.
set -euo pipefail

cd "$(dirname "$0")/../.."
AAB=app/build/outputs/bundle/release/app-release.aab
SPEC=config/size/pixel.json
BASELINE=config/size/download-size.txt
TOLERANCE_PERCENT=1

if [[ -n "${BUNDLETOOL_JAR:-}" ]]; then
  bundletool() { java -jar "$BUNDLETOOL_JAR" "$@"; }
fi

WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT
bundletool build-apks --bundle="$AAB" --output="$WORK/app.apks" --mode=default >/dev/null
size=$(bundletool get-size total --apks="$WORK/app.apks" --device-spec="$SPEC" | tail -1 | cut -d, -f2 | tr -d '[:space:]')

if [[ "${1:-}" == "--update" ]]; then
  echo "$size" > "$BASELINE"
  echo "Baseline set to $size bytes."
  exit 0
fi

baseline=$(tr -d '[:space:]' < "$BASELINE")
delta=$((size - baseline))
limit=$((baseline * TOLERANCE_PERCENT / 100))
report="Download size (Pixel-class): $size bytes, baseline $baseline, delta $delta bytes (limit +$limit)."
echo "$report"
[[ -n "${GITHUB_STEP_SUMMARY:-}" ]] && echo "$report" >> "$GITHUB_STEP_SUMMARY"

if (( delta > limit )); then
  echo "The download size grew by more than $TOLERANCE_PERCENT%. If that is intended, run config/size/check-app-size.sh --update and commit the new baseline."
  exit 1
fi
