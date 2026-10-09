# Survey hidden rules candidates

## Goal

Research only: compare every `public` field of v9 `Rules` (and `Rules.TeamRule`)
against what v9 `CustomRulesDialog.setupMain` renders, and produce a candidate
list of rule fields worth adding via a future module - the successor candidates
to the hidden-checks port. "Maybe, or maybe not" - candidates only, no code.

## Scope

- A single doc: `ai-artifacts/mindustry-docs/RulesSurvey.md` (new).
- No source changes.

## Plan steps

1. Enumerate `Rules` public fields (skip serialization-only plumbing, see
   `ai-artifacts/mindustry-docs/MapFiles.md` for what round-trips through the
   map file - skipped fields are still listed, marked "excluded" with a reason)
   and each `TeamRule` field.
2. Grep v9 `CustomRulesDialog.setupMain` for each field name (word-boundary
   match, then manual confirm - short names like `fire`/`cheat`/`pvp` match
   other text); mark rendered / not-rendered / conditionally-rendered (e.g.
   gated behind another rule).
3. For each not-rendered field: note its type, what UI it would need
   (check/number/text/color/team/special), whether a bundle label already exists
   in vanilla or old-src, and a short "why a mapper would want it" line.
4. Sanity-check a few values against in-game behavior (field javadoc in
   `Rules.java` is usually enough).
5. Rank candidates: obvious (plain bool/number with a clear use), debatable
   (niche or dangerous), reject (internal/unsafe to expose) - and say why for
   rejects so they are not re-proposed.

## Acceptance criteria

- `RulesSurvey.md` classifies 100% of `Rules` + `TeamRule` public fields
  (~125 total - count it from source, don't trust this number) exactly once
  each as rendered / candidate / excluded (with reason) - no field unaccounted
  for.
- Every candidate row states: field symbol, type, proposed UI, bundle-key
  availability, and a one-line rationale.
- The ranked sections (obvious/debatable/reject) each have reasoning, not just
  lists.
- The new `RulesSurvey.md` gets its line in the AGENTS.md file map (and
  `mindustry-docs/` stays consistent with index-docs rules).
- No code changes; doc follows index-docs rules (verify against source, cite
  path + symbol, no line numbers).

## Constraints

- Findings only - creating follow-up task stubs from "obvious" candidates is a
  separate user decision after the doc exists.
- v9 source only (`../Mindustry` branch v9); old-src is not a reference for
  which fields exist now.
- Do not duplicate the already-ported list from
  `ai-artifacts/tasks/rules-dialog-module/task.md` - those are done, the survey
  covers what that port intentionally left out plus everything new in v9.

## Refs

- v9 `/root/projects/Mindustry/core/src/mindustry/game/Rules.java` -
  `TeamRule` nested class included.
- v9 `/root/projects/Mindustry/core/src/mindustry/ui/dialogs/CustomRulesDialog.java` -
  `setupMain`.
- `ai-artifacts/mindustry-docs/Maps.md`, `MapFiles.md` - how rules reach the map
  file (serialization relevance).
- `ai-artifacts/tasks/rules-dialog-module/task.md` - the ported baseline.

## Open questions

- None (ranking is the deliverable; the user decides what becomes a task).
