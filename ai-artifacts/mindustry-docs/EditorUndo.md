<!-- verified: v9 @ 2026-10-09, sources: editor/DrawOperation.java, editor/OperationStack.java, editor/EditorTile.java, editor/MapEditor.java -->

# editor-undo

How the map editor records and reverses edits. Three pieces: `EditorTile`
(records), `DrawOperation` (one stroke), `OperationStack` (history).

## Recording: EditorTile

`EditorTile extends Tile` replaces every `Tile` in the editor world
(`MapEditor.updateRenderer()` builds them). It overrides
`setFloor/setBlock/setTeam/setOverlay`: each calls `op(type, value)` **before**
mutating, which packs the old value into a `TileOp` (`@Struct`
annotation-processor generated accessors `TileOp.x/y/type/value`, `TileOp.get(...)`)
and passes it to `editor.addTileOp(...)`.

`skip()` gates recording: no ops while `state.isGame()`, `state.generating`, or
`editor.isLoading()`. World rewrites wrapped in `editor.load(Runnable)` are
therefore not undoable - that is also how undo/redo themselves apply changes.

## One stroke: DrawOperation

A `LongSeq` of packed ops with type bytes `opFloor/opBlock/opRotation/opTeam/
opOverlay/opData/opDataExtra`. `undo()` replays backwards, `redo()` forwards, but
`updateTile` **swaps the stored value with the tile's current value in place** -
undo is symmetric mutation, not a snapshot, so redo works by the same swap.
Applying goes through `setTile`, which wraps the mutation in `editor.load(...)`
(no re-recording) and refreshes the renderer before and after.

## History: OperationStack

`Seq<DrawOperation>` with `maxSize = 30` and an `index` redo cursor (0 = newest;
negative after undo). `add` truncates the redo tail and drops the oldest entry
past the cap. `undo()/redo()/canUndo()/canRedo()/clear()` are invoked via
`MapEditor` (`editor.undo()` etc., bound to toolbar buttons and ctrl+z/y).

The bridge: `MapEditor.currentOp` accumulates tile ops; `MapView` touch-up calls
`editor.flushOp()` which pushes it onto the stack - **one stroke = one undo
step**. Applying a generate filter (`MapGenerateDialog.applyToEditor`) or
resizing wipes the stack (`clearOp`).
