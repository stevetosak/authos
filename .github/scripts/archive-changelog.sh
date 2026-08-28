#!/usr/bin/env bash
# Roll the oldest entries out of docs/CHANGELOG.md into
# docs/archive/CHANGELOG-archive.md, keeping the newest --keep entries in place.
#
#   .github/scripts/archive-changelog.sh              # keep the newest 60
#   .github/scripts/archive-changelog.sh --keep 100
#   .github/scripts/archive-changelog.sh --dry-run
#
# Both files are append-only and marked `merge=union` in .gitattributes, so this
# roll is safe to do on its own branch as long as no open branch has *edited*
# (rather than appended to) CHANGELOG.md. Merge this branch before the others.
#
# Run it when .github/scripts/doc-size.sh reports CHANGELOG.md OVER its limit.

set -euo pipefail

keep=60
dry_run=0
while [ $# -gt 0 ]; do
  case "$1" in
    --keep) keep="$2"; shift 2 ;;
    --keep=*) keep="${1#*=}"; shift ;;
    --dry-run) dry_run=1; shift ;;
    *) echo "unknown arg: $1" >&2; exit 2 ;;
  esac
done

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_root"

src="docs/CHANGELOG.md"
archive="docs/archive/CHANGELOG-archive.md"
pointer="<!-- Older entries live in archive/CHANGELOG-archive.md -->"

[ -f "$src" ] || { echo "$src not found" >&2; exit 1; }

# An entry is a line beginning with "- ". Everything before the first entry is
# the header (title + editing rule + --- + any existing pointer comment).
first_entry_ln="$(grep -n '^- ' "$src" | head -1 | cut -d: -f1 || true)"
if [ -z "${first_entry_ln:-}" ]; then
  echo "no entries found in $src — nothing to do"; exit 0
fi

mapfile -t all < "$src"
header=("${all[@]:0:first_entry_ln-1}")
tail_lines=("${all[@]:first_entry_ln-1}")

# Split the tail into entry lines (keep their order) and drop blank lines.
entries=()
for line in "${tail_lines[@]}"; do
  [[ "$line" =~ ^-\  ]] && entries+=("$line")
done

total="${#entries[@]}"
if [ "$total" -le "$keep" ]; then
  echo "$src has $total entries, keep=$keep — nothing to archive"
  exit 0
fi

move_count=$(( total - keep ))
to_move=("${entries[@]:0:move_count}")
to_keep=("${entries[@]:move_count}")

echo "entries: $total   archiving oldest: $move_count   keeping newest: $keep"
if [ "$dry_run" -eq 1 ]; then
  echo "--- would move ---"; printf '%s\n' "${to_move[@]}"
  exit 0
fi

# Strip an existing pointer line from the header; we re-add it below.
clean_header=()
for line in "${header[@]}"; do
  [ "$line" = "$pointer" ] && continue
  clean_header+=("$line")
done
# Trim trailing blank lines off the header.
while [ "${#clean_header[@]}" -gt 0 ] && [ -z "${clean_header[-1]}" ]; do
  unset 'clean_header[-1]'
done

# Append the moved entries to the archive (create with a header if new).
if [ ! -f "$archive" ]; then
  {
    echo "# Changelog — archive"
    echo
    echo "Older entries rolled out of \`../CHANGELOG.md\` by \`.github/scripts/archive-changelog.sh\`."
    echo "Same format, same append-only / newest-at-end rule."
    echo
    echo "---"
    echo
  } > "$archive"
fi
printf '%s\n' "${to_move[@]}" >> "$archive"

# Rewrite CHANGELOG.md: header + pointer + kept entries.
{
  printf '%s\n' "${clean_header[@]}"
  echo
  echo "$pointer"
  echo
  printf '%s\n' "${to_keep[@]}"
} > "$src"

echo "moved $move_count entries -> $archive"
echo "run .github/scripts/doc-size.sh to confirm"
