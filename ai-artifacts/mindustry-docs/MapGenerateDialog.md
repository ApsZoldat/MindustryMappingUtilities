<!-- verified: v9 @ 2026-10-09, sources: editor/MapGenerateDialog.java, maps/filters/GenerateFilter.java, maps/filters/FilterOption.java, editor/MapInfoDialog.java -->

# MapGenerateDialog

Live-preview filter list for terrain generation. `extends BaseDialog`. Two
long-lived instances exist:

- `applied = true` - the MapEditorDialog toolbar "generate": applies filters to the
  editor's tiles directly.
- `applied = false` - inside MapInfoDialog: the result is serialized into the map's
  `tags["genfilters"]` so the map regenerates on load.

Shown via `show(Seq<GenerateFilter>, Cons<Seq<GenerateFilter>>)` (sets `filters` +
a caller-supplied `applier`); `hidden()` runs `apply()` -> `applier.get(filters)`.
The `applier` is the injection point: replace it to redirect where results go.

## Preview

`update()` copies the filter list, submits to `mainExecutor`, ping-pongs two
`long[]` buffers through the filters (`buffer1` -> `buffer2` -> ...), paints a
`Pixmap`, and `Core.app.post`s a texture draw. Sets the global
`state.generating` true/false while running - this is what makes
`EditorTile.skip()` suppress op recording during preview reads. `generating` /
`updateQueued` coalesce re-entry; `result` is joined in `apply()` before cleanup.

## Applied mode

`applyToEditor(filters)`: for each filter, packs tiles into write buffers, runs
`filter.apply` and writes back inside `editor.load(...)` (skipping synthetic
tiles), then `editor.renderer.recache()` + `editor.clearOp()` - **applying wipes
undo**. The "Add filter" menu hides `isPost()` filters in this mode.

UI: per-filter rows rebuilt by `rebuildFilters()`; each `FilterOption`'s `changed`
callback is rewired to `this::update`. Mirror axis handles drag
`MirrorFilter.axisX/axisY` directly. Clipboard = `JsonIO.write/read(filters)`;
reset = `maps.readFilters("")`.

## GenerateFilter / FilterOption (maps/filters/)

- `GenerateFilter`: abstract, `public int seed`; `apply(GenerateInput)` per tile;
  `options()` -> `FilterOption[]`; `randomize()`; `copy()`.
- `isBuffered()` filters read all tiles into a buffer first (`apply(World,
  GenerateInput)`) - they see pre-filter state; plain filters see the running
  result. **Order matters**: the list is sequential, each output feeds the next
  (up/down buttons `swap(...)` change results).
- `isPost()` = generation-only (terrain post-pass), hidden in applied mode.
- `FilterOption`: one UI row per parameter (`build(Table)` + a `changed`
  `Runnable`); subclasses `SliderOption`, `BlockOption`, `ToggleOption`.
- Registry: `Maps.allFilterTypes` drives the Add menu; persistence is the
  `genfilters` tag, and filters with `seed = 0` are effectively marked unset
  (MapInfoDialog zeroes seeds before storing).
