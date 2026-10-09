# Task states

Single source of truth for every task's state. Read this header before touching any task.

**Pipeline:** `sync-upstream` (docs + inbox stubs) -> `create-tasks` (grill + spec
review) -> `ready-to-implement` -> `implement` -> `needs-review` -> `review` ->
`needs-testing` -> `test` -> `done`.

**Rules:**

- Main agent only - never subagents touch this file.
- One whole-table rewrite per skill run; keep the `Updated` date current (YYYY-MM-DD).
- Before acting, re-check `blocked` rows: when every entry in `Depends on` is `done`
  (or `cancelled`, which counts as resolved - note it), flip the row to
  `ready-to-implement`.
- `Depends on` is written at creation and stays permanently; `blocked` is where it
  becomes actionable.

**States:** inbox (stub, unspecified) - needs-info (waiting on the user) - blocked
(waiting on deps) - ready-to-implement - in-progress - needs-review (code written,
second opinion pending; user may jump in here too) - needs-testing - done (user-verified)
- cancelled (abandoned, kept for the record).

| Owner | Transitions |
|---|---|
| sync-upstream | creates -> inbox; flags open tasks stale |
| create-tasks | inbox -> ready-to-implement / needs-info / blocked |
| implement | ready-to-implement -> in-progress -> needs-review (stall -> needs-info / blocked) |
| review | needs-review -> needs-testing / in-progress / needs-info |
| test | needs-testing -> done / ready-to-implement (+failures appended) |
| any skill | auto-unblock blocked -> ready; any -> cancelled (user decides) |

<!-- Task rows below. Task = folder name under ai-artifacts/tasks/ (<name>/task.md). -->

| Task | State | Depends on | Blocker / next | Updated |
|---|---|---|---|---|
| mu-module-framework | done | - | verified: rename, init loop, failure notice | 2026-10-09 |
| mu-settings-category | ready-to-implement | mu-module-framework | next: implement (needs MU.modules registry) | 2026-10-09 |
| rules-dialog-module | ready-to-implement | mu-module-framework | next: implement (needs MUModule + init loop) | 2026-10-09 |
| survey-hidden-rules | ready-to-implement | - | next: implement (research only) | 2026-10-09 |
| rules-numbered-teams | inbox | - | stub, deferred from rules-dialog-module; needs create-tasks pass | 2026-10-09 |
