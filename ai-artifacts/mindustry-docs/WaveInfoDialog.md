<!-- verified: v9 @ 2026-10-09, sources: editor/WaveInfoDialog.java, editor/WaveGraph.java, editor/MapInfoDialog.java -->

# WaveInfoDialog

Wave list editor (`extends BaseDialog`, opened from MapInfoDialog). Edits
`state.rules.spawns` - a `Seq<SpawnGroup>`.

## Staged commit

`setup()` works on `groups = JsonIO.copy(state.rules.spawns.isEmpty() ?
waves.defaults() : state.rules.spawns)` - a deep copy. All row editing mutates
the copy; `hidden()` commits with `state.rules.spawns = groups`. Closing is the
only commit point.

## Editing

`buildGroups()` sorts `groups` in place (enum `Sort`, plus reverse flags) and
rebuilds the table; expanding a row edits `SpawnGroup` fields inline
(begin/spacing/unitAmount/unitScaling/max/shields/effect/team/spawn), each change
calling `updateWaves()` -> `graph.groups = groups; graph.rebuild()`.

- Randomize: `groups = Waves.generate(1f/10f)`. Clipboard: `maps.writeWaves` /
  `readWaves`. Reset: `JsonIO.copy(waves.defaults())`.
- The spawn picker calls `Vars.state.spawner.init()` once per open (guarded by
  `checkedSpawns`) - mutates global spawner state.
- `effect == StatusEffects.none` is normalized to null when building rows.
- Team select reuses `MapObjectivesDialog.showTeamSelect(true, ...)`.

## WaveGraph

`extends Table` (not a dialog - embedded by WaveInfoDialog via
`cont.add(graph).grow()`). `public Seq<SpawnGroup> groups` is assigned externally.
A pan/zoomable line graph; enum `Mode` = counts/totals/health; a per-unit legend
with an `ObjectSet<UnitType> hidden` that only affects drawing. `rebuild()`
reallocates the `values` array for the visible `from..to` range and is called both
externally and from the draw lambda when pan/zoom changes the range - do not cache
`values`.
