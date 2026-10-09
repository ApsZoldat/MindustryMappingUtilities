<!-- verified: v9 @ 2026-10-09, sources: editor/MapEditorDialog.java, editor/MapInfoDialog.java, ui/dialogs/MapPlayDialog.java -->

# MapEditorDialog

The whole editor screen. `extends Dialog implements Disposable`; global `ui.editor`.
Owns the `MapView` and every sub-dialog as long-lived fields.

## Structure

- The ctor calls `clearChildren()` - all UI comes from `build()`, which runs on
  *every* `show()`. Anything a patch adds inside `build()` must expect full
  rebuilds.
- `build()` lays out three columns: left toolbar table, center `t.add(view).grow()`,
  right block selection (`addBlockSelection`). Toolbar: tool button group
  (`ButtonGroup<ImageButton>` over `EditorTool.all`, icons via `ui.getIcon`),
  undo/redo, rotate, team buttons over `Team.baseTeams`, brush slider over
  `MapEditor.brushSizes`, show-buildings/terrain/floor checkboxes, menu button.
- Block selection: search field named `"editor/search"`, block buttons set
  `editor.drawBlock`, a collapser shows `block.buildEditorConfig(table)` when
  `editorConfigurable`; sorted cores -> synthetic -> ores -> id.

## Sub-dialog fields (constructed once in the ctor)

`infoDialog` (MapInfoDialog), `loadDialog` (MapLoadDialog), `resizeDialog`
(MapResizeDialog), `generateDialog` (`new MapGenerateDialog(true)` - applied mode),
`sectorGenDialog` (SectorGenerateDialog), `playtestDialog` (MapPlayDialog).

Not here: WaveInfoDialog, objectives and rules dialogs live inside MapInfoDialog.
The `Simulate` dialog is a method-local, gated behind the system property
`mindustry.editor.simulate.button`.

## Lifecycle

- `shown`: clears the op stack, sets scroll focus to `view`, and if opened without a
  map (`!shownWithMap`) runs `logic.reset()` + `editor.beginEdit(200, 200)`.
- `hidden`: clears ops.
- `doInput()` runs every frame while the dialog has keyboard focus: tool keys from
  `EditorTool.key`, `ctrl+1..9` sets `tool.mode`, `r`/`e` rotate, `ctrl+z/y`
  undo/redo, `ctrl+s` save, `ctrl+g` grid, escape opens the menu.
- `save()`: puts `JsonIO.write(state.rules)` into `editor.tags["rules"]`, strips
  `width`/`height` tags, clears objectives/player, temporarily clears
  `state.rules.editor`, then `maps.saveMap(editor.tags, false)`.
- `editInGame()` plays with `Gamemode.editor.apply(...)` rules; `resumeEditing()`
  restores `lastSavedRules` and calls `renderer.recache()`; `playtest()` /
  `resumeAfterPlaytest` round-trip a map. `handleSaveBuiltin(Fi)` is overridable
  (built-in maps are blocked from being overwritten).
- `beginEditMap(Fi)` loads a file into the editor; `dispose()` ->
  `renderer.dispose()`.
