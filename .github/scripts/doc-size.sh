#!/usr/bin/env bash
# Guardrail: keep the docs/ tracking files from ballooning.
#
# Prints the line count of every governed doc. Exits non-zero if any file is
# over its limit, with a pointer to the archiving procedure. CI runs this on
# every PR that touches docs/ (.github/workflows/docs.yaml); run it by hand any
# time with `.github/scripts/doc-size.sh`.
#
# The fix when a file is OVER is never "raise the limit" — it is to archive the
# stale part per docs/README.md (§ Archiving). For CHANGELOG.md that is
# scripted: .github/scripts/archive-changelog.sh

set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

# file : soft limit (lines). Approaching = within 10% under.
limits=(
  "docs/roadmap.md:400"
  "docs/duster-v1-tasks.md:500"
  "docs/duster-v1-design.md:500"
  "docs/CHANGELOG.md:500"
  "docs/automation-tests-plan.md:400"
)

status=0
printf '%-34s %7s %7s\n' "FILE" "LINES" "LIMIT"
printf -- '-%.0s' {1..58}; printf '\n'
for entry in "${limits[@]}"; do
  file="${entry%%:*}"
  limit="${entry##*:}"
  if [ ! -f "$file" ]; then
    printf '%-34s %7s %7s  (missing)\n' "$file" "-" "$limit"
    continue
  fi
  lines="$(wc -l < "$file" | tr -d '[:space:]')"
  flag=""
  if [ "$lines" -gt "$limit" ]; then
    flag="  <-- OVER: archive per docs/README.md"
    status=1
  elif [ "$lines" -gt $(( limit * 9 / 10 )) ]; then
    flag="  (approaching limit)"
  fi
  printf '%-34s %7s %7s%s\n' "$file" "$lines" "$limit" "$flag"
done

if [ "$status" -ne 0 ]; then
  echo
  echo "One or more docs are over their line limit. Archive the stale section"
  echo "(see docs/README.md § Archiving) rather than raising the limit here."
fi

exit "$status"
