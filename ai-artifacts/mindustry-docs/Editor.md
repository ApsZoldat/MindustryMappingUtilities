<!-- verified: v9 @ 2026-10-09, sources: editor/MapEditorDialog.java, editor/MapView.java, editor/MapEditor.java, editor/MapInfoDialog.java, ui/dialogs/EditorMapsDialog.java -->

# Guide: patching the editor

Read first: [MapEditor.md](MapEditor.md),
[MapEditorDialog.md](MapEditorDialog.md), [MapView.md](MapView.md),
[EditorUndo.md](EditorUndo.md).

## Entry points and lifecycle

- Global handles: `Vars.editor` (model), `Vars.ui.editor` (`MapEditorDialog`),
  `Vars.ui.maps` (`EditorMapsDialog`, the map manager/creator).
- Open with a map: `ui.editor.beginEditMap(Fi)` or `editor.beginEdit(Map)`;
  blank: `ui.editor.show()` (the `shown` handler runs `logic.reset()` +
  `editor.beginEdit(200, 200)`). Creating a new map from `EditorMapsDialog` fires
  `MapMakeEvent` before showing the editor.
- **`build()` reruns on every `show()`** and fully reconstructs the toolbar and
  block selection. UI added inside `build()` must be re-added by your own `shown`
  hook or built idempotently. The ctor's `clearChildren()` means there is nothing
  else to graft onto.
- `hidden()` clears the op stack, and so does `shown()` - do not stash state in
  ops across open/close.

## Adding UI

- Toolbar: patch `build()` output via the `shown` handler (it runs after
  `build()`), or hook the menu `BaseDialog` (field `menu`).
- Blocks palette: `addBlockSelection(Table)` rebuilds from `editor.drawBlock`;
  per-block config uses `block.buildEditorConfig(table)` when
  `editorConfigurable`.
- Sub-dialogs are long-lived fields (`infoDialog`, `generateDialog`, ...) on
  MapEditorDialog and MapInfoDialog - reflect/replace one instance rather than
  building a new dialog each time.
- Most editor sub-dialogs **commit on `hidden()`** (waves, objectives, generate,
  locales). If you wrap one, preserve that or edits are lost.

## Hooks into state

- Mutating tiles for undoable strokes: set `editor.drawBlock`/`drawTeam` and call
  `editor.drawBlocks(x, y)` (or per-tile `tile.setBlock` on `EditorTile`s - ops
  are recorded automatically). Wrap non-undoable bulk rewrites in
  `editor.load(Runnable)`.
- After bulk world changes: `editor.updateRenderer()` (full) or
  `editor.renderer.updateStatic(x, y)`/`updateBlock(...)` (targeted).
- Tool extensions: `EditorTool` is an enum - you cannot add constants; emulate a
  tool by handling input in a `MapView` listener or overriding `tool.touched`.
  Remember `MapView` touch-up commits one undo step via `editor.flushOp()`.
- Keybinds: `MapEditorDialog.doInput()` reads `EditorTool.key` and hardcodes
  ctrl+z/y/s/g and r/e; there is no editor input controller class in v9.

## Rules and metadata

- `editor.tags` is the map's metadata store; rules live in `tags["rules"]` as JSON
  (`JsonIO`). The dialog snapshots `lastSavedRules` and restores it in
  `resumeEditing()`.
- `state.rules.editor` marks editor mode (`Gamemode.editor`); `save()`
  temporarily strips it before serializing - custom rules patches must tolerate
  this.

## Things that will silently break

- Reflection into `MapView.zoom/offset*`, `MapEditorDialog` sub-dialog fields, or
  `MapAssetsDialog.views` - all private/package-private; verify names against v9
  source before shipping.
- Assuming `EditorTile` records ops during generation/preview - `skip()`
  suppresses recording while `state.generating` or `editor.isLoading()`.
- Expecting a rectangular selection or an `EditorController` - neither exists in
  v9.
