<!-- verified: v9 @ 2026-10-09, sources: game/Rules.java, ui/dialogs/CustomRulesDialog.java, logic/instructions/SetRuleI.java, core/Logic.java, assets/bundles/bundle.properties -->

# Rules survey

Field-by-field coverage audit of the rules dialog: which public `Rules` /
`Rules.TeamRule` fields v9 `CustomRulesDialog.setupMain` already renders, which
are committed to the `rules-dialog-module` port, which are candidates for a
future module, and which are excluded for good reason. Candidates only - nothing
here becomes a task without the user's go-ahead.

Method: exact field names matched against `setupMain` with manual confirmation
(short names like `fire`/`cheat` also appear in comments/strings); a hit only
counts when the code binds that field. The label filter inside the
`check`/`number`/`numberi`/`text`/`team` builders applies to every row and is
not counted as a gate; explicit `if(...contains(ruleSearch))` panel/button gates
are marked "search-gated". Counts from source: **118 `Rules` + 24 `TeamRule` =
142** public instance fields (the task's ~125 estimate was low).

| disposition | Rules | TeamRule | total |
|---|--:|--:|--:|
| rendered (v9 `setupMain`) | 75 | 21 | 96 |
| ported (`rules-dialog-module`, pending) | 16 | 2 | 18 |
| candidate (survey output) | 15 | 0 | 15 |
| excluded (reasons below) | 12 | 1 | 13 |
| **total** | **118** | **24** | **142** |

## Rendered by v9 setupMain

### Rules (75)

| field | gate / note |
|---|---|
| `waves` | - |
| `waveSending` | cond `rules.waves` |
| `waveTimer` | cond `rules.waves` |
| `waitEnemies` | cond `rules.waves && rules.waveTimer` |
| `randomWaveAI` | cond `rules.waves` |
| `wavesSpawnAtCores` | cond `rules.waves` |
| `airUseSpawns` | cond `rules.waves` |
| `winWave` | cond `rules.waves` |
| `waveSpacing` | cond `rules.waves && rules.waveTimer` |
| `initialWaveSpacing` | cond `rules.waves && rules.waveTimer` |
| `dropZoneRadius` | cond `rules.waves` |
| `allowEditWorldProcessors` | - |
| `infiniteResources` | - |
| `coreBuildAndConfig` | - |
| `onlyDepositCore` | - |
| `allowCoreUnloaders` | - |
| `derelictRepair` | - |
| `reactorExplosions` | - |
| `schematicsAllowed` | - |
| `coreIncinerates` | - |
| `cleanupDeadTeams` | cond `rules.pvp` |
| `disableWorldProcessors` | - |
| `buildCostMultiplier` | cond `!rules.infiniteResources` |
| `buildSpeedMultiplier` | - |
| `deconstructRefundMultiplier` | cond `!rules.infiniteResources` |
| `blockHealthMultiplier` | - |
| `blockDamageMultiplier` | - |
| `loadout` | search-gated `configure` button -> loadout dialog |
| `bannedBlocks` | search-gated button -> BannedContentDialog |
| `hideBannedBlocks` | - |
| `blockWhitelist` | - |
| `unitCapVariable` | - |
| `unitPayloadsExplode` | - |
| `unitCap` | - |
| `unitFactoryActivationDelay` | - |
| `unitDamageMultiplier` | - |
| `unitCrashDamageMultiplier` | - |
| `unitMineSpeedMultiplier` | - |
| `unitBuildSpeedMultiplier` | - |
| `unitCostMultiplier` | - |
| `logicUnitControl` | - |
| `logicUnitBuild` | cond `rules.logicUnitControl` |
| `logicUnitDeconstruct` | cond `rules.logicUnitControl` |
| `bannedUnits` | search-gated button -> BannedContentDialog |
| `unitWhitelist` | - |
| `attackMode` | - |
| `coreCapture` | - |
| `placeRangeCheck` | - |
| `polygonCoreProtection` | - |
| `enemyCoreBuildRadius` | cond `!rules.polygonCoreProtection` |
| `pauseDisabled` | - |
| `damageExplosions` | - |
| `fire` | - |
| `fog` | - |
| `lighting` | - |
| `limitMapArea` | cond `!state.isGame()` |
| `limitX` / `limitY` / `limitWidth` / `limitHeight` | cond `rules.limitMapArea && !state.isGame()` |
| `solarMultiplier` | - |
| `weather` | search-gated button -> weather dialog |
| `ambientLight` | search-gated color button -> `ui.picker` |
| `unitLight` | - |
| `alwaysPlayMusic` | cond `!rules.disableMusic` |
| `disableMusic` | - |
| `ambientMusic` | cond `!rules.disableMusic`, text row |
| `darkMusic` | cond `!rules.disableMusic`, text row |
| `planet` | search-gated `rules.title.planet`, presets via `Planet.applyRules` |
| `env` | indirect only: planet presets / "any" button (`rules.env = Vars.defaultEnv`); per-flag editor is `rules-dialog-module` scope |
| `attributes` | indirect only: planet presets; "any" button clears it |
| `allowEditRules` | cond `showRuleEditRule` (dialog ctor flag) |
| `defaultTeam` / `waveTeam` | team rows |
| `teams` | per-team collapser editors over `Team.baseTeams` |

The four `limit*` fields are one table row: they are four separate fields
declared on one source line (`Rules.limitX, limitY, limitWidth = 1,
limitHeight = 1`), likewise `backgroundOffsetX`/`backgroundOffsetY`.

### TeamRule (21)

All rows live inside the per-team collapser in the `Team.baseTeams` loop.

| field | gate / note |
|---|---|
| `blockHealthMultiplier`, `blockDamageMultiplier` | - |
| `rtsAi` | cond `team != rules.defaultTeam` |
| `rtsMinSquad`, `rtsMaxSquad`, `rtsMinWeight` | cond `rtsAi` |
| `buildAi` | cond `team != defaultTeam && env != Erekir default && !rules.pvp` |
| `buildAiTier` | cond `buildAi &&` same env/pvp gate |
| `protectCores` | - |
| `extraCoreBuildRadius` | cond `!rules.polygonCoreProtection && protectCores` |
| `checkPlacement` | - |
| `infiniteResources`, `fillItems`, `buildSpeedMultiplier` | - |
| `unitFactoryActivationDelay`, `unitDamageMultiplier`, `unitCrashDamageMultiplier`, `unitMineSpeedMultiplier`, `unitBuildSpeedMultiplier`, `unitCostMultiplier`, `unitHealthMultiplier` | - |

## Ported by rules-dialog-module (18)

Committed scope of [`rules-dialog-module`](../tasks/rules-dialog-module/task.md)
(still `ready-to-implement` as of this doc - "ported" means decided, not yet in
code). Do not re-propose these; the module re-creates `CustomRulesDialog`
instances' missing rows.

- **Rules (16):** `hideSpawns`, `ghostBlocks`, `possessionAllowed`,
  `unitPayloadUpdate`, `pvpAutoPause`, `coreDestroyClear`, `borderDarkness`,
  `disableOutsideArea`, `staticFog`, `dragMultiplier`, `staticColor`,
  `dynamicColor`, `cloudColor`, `canGameOver`, `modeName`, `mission`.
- **TeamRule (2):** `cheat`, `aiCoreSpawn` (per-team rows in the team collapser).
- Also in that port but not separate fields: the per-flag environment dialog
  (editor of `rules.env`, which the table above counts as rendered-indirect).

## Candidates (15)

Everything below is **not** rendered by v9 `setupMain` and **not** in the ported
list. UI types: `check`/`number`/`text` builders from `CustomRulesDialog`,
`color` = button + `ui.picker.show` (same pattern as `ambientLight`),
`special` = custom sub-dialog. Bundle key: which English label already exists -
vanilla `bundle.properties`, old-src mod bundle, or a new key to write.

| field | type | proposed UI | bundle key | why a mapper wants it |
|---|---|---|---|---|
| `unitHealthMultiplier` | float | number (min ~0.001) | vanilla `rules.unithealthmultiplier` exists | the only unit multiplier without a global row; `Rules.unitHealth(team)` = global x team, so the global half is unreachable from the dialog |
| `disableUnitCap` | boolean | check next to `unitCap` | new `rules.disableunitcap` | `Units` checks this flag before any cap math; without a row, disabling the cap means a magic negative `unitCap` |
| `itemDepositCooldown` | float (sec) | number | new `rules.itemdepositcooldown` | player->core deposit cooldown (`InputHandler`); economy maps tune it, only per-block `depositCooldown` is settable today |
| `objectiveTimerMultiplier` | float | number | new `rules.objectivetimemultiplier` | `TimerObjective` counts against it; objective maps get scaled timings for free |
| `allowLogicData` | boolean | check | new `rules.allowlogicdata` | gates the logic `data` instruction (`ClientDataStatement`); competitive maps can stop unit/building state exfiltration |
| `showOtherTeamPings` | boolean | check | new `rules.showotherteampings` | hides enemy-team pings (`PlayerComp`); multiplayer-only nicety, default false |
| `worldProcessorPlayerLink` | boolean | check | new `rules.worldprocessorplayerlink` | controls world-processor -> player-structure links (issue #12091); note `Logic` force-false on **campaign** world load, so it only matters off-campaign |
| `musicVolume` | float 0..1 | number | new `rules.musicvolume` | per-map loudness multiplier (`SoundControl`); logic `SetRule` can change it at runtime, so the row only sets the default |
| `revealedBlocks` | ObjectSet\<Block\> | special: second BannedContentDialog-style picker | old-src `rules.revealedblocks` + `.info` (copy) | reveals build-visibility-hidden blocks; deliberately deferred by `rules-dialog-module` as out-of-scope, not rejected |
| `blockLimits` | ObjectIntMap\<Block\> | special: bespoke per-block number table | new `rules.blocklimits` | placement limits are read by `Block`/`PlacementFragment` but **no UI anywhere vanilla writes them**; heavy - needs a custom dialog |
| `backgroundTexture` | String | text | new `rules.backgroundtexture` | ship a moving background without a mod; group all five background rows or none (see below) |
| `backgroundSpeed` | float | number | new `rules.backgroundspeed` | parallax move speed; 0 disables |
| `backgroundScl` | float | number | new `rules.backgroundscl` | parallax scale |
| `backgroundOffsetX` | float | number | new `rules.backgroundoffsetx` | UV offset pair with `backgroundOffsetY` |
| `backgroundOffsetY` | float | number | new `rules.backgroundoffsety` | UV offset pair with `backgroundOffsetX` |

### Obvious (5)

Plain bool/number, unambiguous mapper use, existing or trivially written label:

1. **`unitHealthMultiplier`** - an omission, not a niche: every sibling
   multiplier has a global row, and the vanilla label already exists (currently
   reused by the per-team row).
2. **`disableUnitCap`** - completes the existing `unitCap` row; the flag is
   checked first in `Units`, so it is the honest way to express "no cap".
3. **`itemDepositCooldown`** - one number, one clear economy lever.
4. **`objectiveTimerMultiplier`** - one number, directly scales `TimerObjective`
   durations.
5. **`allowLogicData`** - one bool with a concrete competitive-map use (blocks
   the `data` instruction).

### Debatable (10)

Real uses, but niche, multi-widget, or partially redundant:

- **`showOtherTeamPings`**, **`worldProcessorPlayerLink`** - multiplayer/campaign
  edge behavior; the link flag is overridden at campaign load, which will
  confuse anyone who edits it for a campaign map.
- **`musicVolume`** - already settable at runtime by logic `SetRule`, so the row
  only fixes the pre-logic default; also blurs "map rule" vs "audio setting".
- **`revealedBlocks`** - good candidate, but needs a second content-picker
  instance and whitelist interplay; was explicitly deferred once already.
- **`blockLimits`** - no existing UI pattern at all (per-block numbers); biggest
  UI cost of the list for a rarely used feature.
- **background set (`backgroundTexture`/`Speed`/`Scl`/`OffsetX`/`OffsetY`)** -
  take as one group: a texture path without offsets/speed is half a feature,
  and a wrong path fails silently (invisible background). Five new keys.

### Reject / excluded - do not re-propose (13)

| field | why |
|---|---|
| `pvp` | gamemode-derived: `Gamemode.pvp.apply` sets it and validity is computed from map teams (`Map`/`Maps`); a raw check would fight the play-dialog mode picker |
| `editor` | editor-internal marker, stripped from the map on save (see [MapFiles.md](MapFiles.md)) |
| `allowEnvironmentDeconstruct` | javadoc: only meant for internal sandbox/test maps |
| `instantBuild` | javadoc: highly experimental, may cause lag; sandbox-only semantics |
| `sector` | campaign/save runtime handle, not a map-authored value |
| `spawns` | spawn data belongs to wave tooling; `Rules.retainContentFields` force-replaces it from the loaded map (patch-content safety), so a dialog value would be ignored |
| `researched` | campaign unlock state, not map authoring |
| `objectives` | has its own editor (`MapObjectivesDialog`) |
| `objectiveFlags` | runtime state written by objectives / world processors |
| `tags` | opaque `StringMap`; no reader in v9 game code, serialization only - nothing to show |
| `customBackgroundCallback` | mod render hook: a string naming a callback that only exists if a mod registers it; useless without that mod |
| `planetBackground` | javadoc: cannot be changed once a map is loaded (load-time only) - also why old-src's `rules.planetbackground` key is dead weight |
| `TeamRule.prebuildAi` | javadoc: "EXPERIMENTAL, DO NOT USE" |

## Footnotes

- **Logic overlap:** `SetRule` (`SetRuleI`/`LogicRule`) changes a subset of
  rules at runtime (waves, `unitCap`, `lighting`, `canGameOver`, `musicVolume`,
  `ambientLight`, `solarMultiplier`, `dragMultiplier`, map area, per-team
  multipliers, ...). A candidate also on that list still deserves a static row -
  a runtime override is not a saved default - but `musicVolume`'s ranking leans
  on it.
- **Dead old-src keys:** `rules.numberedteam`, `rules.unitammo`,
  `rules.infiniteammo` have **no backing field in v9 `Rules`** (no matching
  symbol anywhere in `core/src/mindustry`). The `rules-numbered-teams` inbox
  stub needs re-scoping before it can be specified.
- Everything round-trips through the map file (rules serialize as one JSON blob
  in `tags["rules"]`, see [MapFiles.md](MapFiles.md)); nothing is excluded for
  being "serialization-only" - exclusions above are semantic.
