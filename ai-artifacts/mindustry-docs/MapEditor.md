<!-- verified: v9 @ 2026-10-09, sources: editor/MapEditor.java, editor/EditorTile.java, editor/MapEditorDialog.java -->

# MapEditor

The editor's model object: holds all editing state and is the only thing that mutates
tiles while editing. Global instance is `Vars.editor` (referenced as bare `editor`
throughout `mindustry`). Plain class, no superclass - it is *not* a scene element;
the UI (`MapView`, `MapEditorDialog`) drives it.

There is no dedicated editor controller class in v9 - this object plus `MapView`
cover everything that older mods may expect from an "editor input handler".

## State

- `brushSizes` - `static float[] {1, 1.5, 2, 3, 4, 5, 9, 15, 20}`; the brush slider
  indexes into it and `brushSize` holds the current value.
- `tags` - `StringMap`, the map's metadata. Persists into the saved map file; the
  editor stores rules here as JSON (`tags["rules"]`) - see [MapFiles.md](MapFiles.md).
- `renderer` - public `EditorRenderer`, owned directly (see
  [EditorRenderer.md](EditorRenderer.md)).
- Public mutable fields the toolbar writes: `brushSize`, `rotation`, `drawBlock`,
  `drawTeam`, `showTerrain`, `showFloor`, `showBuildings`.
- Private: `context` (save/load context), `stack` (`OperationStack`, see
  [EditorUndo.md](EditorUndo.md)), `currentOp` (stroke in progress), `loading`
  (suppresses op recording).

## Key methods

- `beginEdit(int, int)` / `beginEdit(Map)` / `beginEdit(Pixmap)` - entry points:
  blank map, existing map (copies `map.tags`, loads via `MapIO.loadMap`), or image
  import.
- `updateRenderer()` - rebuilds the world as `EditorTile`s, keeping existing
  buildings.
- `load(Runnable)` - runs the block with `loading = true` so `EditorTile` stops
  recording undo ops (used by undo/redo and by dialogs that rewrite the world).
- `drawBlocks(int, int)` + overloads, `drawCircle`, `drawSquare` - brush
  application. Multiblock placement clamps coordinates and checks `hasOverlap`.
- `resize(int, int, int, int)` - clears ops, replaces `state.world` with a fresh
  `World`, copies/offsets tiles, shifts building configs and links.
- `undo/redo/canUndo/canRedo/clearOp`, `flushOp` (commits `currentOp` onto the
  stack - called by `MapView` on touch-up), `addTileOp(long)` (no-ops while
  `loading`).

## Gotchas

- `reset()` wipes `brushSize`/`drawBlock`/`tags` but *not* `drawTeam` or `rotation`.
- `loading` is the switch that decides whether a world mutation is undoable;
  anything that rewrites tiles outside a normal brush stroke should wrap in
  `load(...)`.
- `Context` is a non-static, package-private inner class extending
  `SaveLoadContext`.
