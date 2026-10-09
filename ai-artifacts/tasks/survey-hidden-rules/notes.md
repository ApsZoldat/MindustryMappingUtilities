# survey-hidden-rules - notes

## What was done

Research/doc-only task, no code. Two read-only `explore` subagents mapped (a)
`Rules`/`TeamRule` fields vs `CustomRulesDialog.setupMain` and (b) vanilla +
old-src bundle `rules.*` keys; every claim was then verified in-session against
v9 source (`Rules.java` read fully, `setupMain` and the builder section of
`CustomRulesDialog.java` read fully, `SetRuleI.java` + `Logic.java` spot-read,
bundle keys spot-grepped, "who else writes this field" greps for `pvp`,
`blockLimits`, `showOtherTeamPings`, etc.). Counts come from a script over
`Rules.java` source lines, not the task's ~125 estimate.

## Findings worth remembering

- Real field count: **118 `Rules` + 24 `TeamRule` = 142** public instance fields
  (task estimate ~125 was low). Disposition split: rendered 96 / ported 18 /
  candidate 15 / excluded 13.
- Disposition split used a **4th "ported" section** (user decision) so the
  `rules-dialog-module` list is classified without duplicating it; that task is
  still `ready-to-implement`, so the doc says "ported = decided, not in code".
  `task.md` acceptance criteria were amended to match (one bullet).
- 15 candidates, ranked 5 obvious / 10 debatable / 13 rejected-with-reason.
  Strongest candidate: `unitHealthMultiplier` - the only unit multiplier missing
  a global row, and vanilla already has the label.
- **`blockLimits` has no writer UI anywhere in vanilla** (only `Block`/
  `PlacementFragment` readers) - a bespoke per-block dialog would be needed.
- **Dead old-src keys:** `rules.numberedteam`, `rules.unitammo`,
  `rules.infiniteammo` have no v9 `Rules` field at all - flagged in the doc; the
  `rules-numbered-teams` inbox stub must be re-scoped before `create-tasks`.
- `worldProcessorPlayerLink` is force-set false on **campaign** world load
  (`Logic`), so a static row only matters off-campaign; `musicVolume` is already
  logic-`SetRule`-settable, which is why it ranks debatable.
- `Rules.tags` has no reader in v9 game code; `planetBackground` is load-time
  only (javadoc), the same reason old-src's `rules.planetbackground` key is dead.

## Changed files

- `ai-artifacts/mindustry-docs/RulesSurvey.md` (new - the deliverable)
- `AGENTS.md` - file-map line for `RulesSurvey.md` (mindustry-docs section)
- `ai-artifacts/mindustry-docs/MindustryIndex.md` - cross-link on the
  `game/Rules` entry
- `ai-artifacts/tasks/survey-hidden-rules/task.md` - acceptance bullet updated:
  real count (142) + explicit "ported" category
- `ai-artifacts/tasks/TaskStates.md` - row -> in-progress -> needs-review

## Human test checklist (no code - review focus instead)

- [ ] Every one of the 142 fields appears exactly once across the four sections
      (spot-check: `grep -c` per field name in `RulesSurvey.md`).
- [ ] Rendered table gates match `setupMain` (e.g. `waitEnemies` needs
      `waves && waveTimer`, `cleanupDeadTeams` needs `pvp`).
- [ ] Bundle-key claims: `rules.unithealthmultiplier` in vanilla,
      `rules.revealedblocks` only in old-src, none of the "new" keys exist yet.
- [ ] `numberedteam`/`unitammo`/`infiniteammo` dead-key claim vs v9 source (it
      drives a decision on the `rules-numbered-teams` inbox stub).
- [ ] Ranked sections each have reasoning, not just lists (acceptance).

## Judge calls a reviewer should challenge

- `env`/`attributes` counted as **rendered (indirect)** because only planet
  preset buttons mutate them - could defensibly be "conditional" or "excluded".
- `pvp` rejected as gamemode-derived rather than proposed as a check row.
- Background five-field set ranked debatable as a group (silent-failure risk of
  a bad texture path) instead of splitting off the easy numbers.
