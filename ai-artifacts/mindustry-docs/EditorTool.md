<!-- verified: v9 @ 2026-10-09, sources: editor/EditorTool.java, editor/MapEditorDialog.java, editor/MapView.java -->

# EditorTool

`enum EditorTool` - the seven editor tools: `zoom, pick, line, pencil, eraser,
fill, spray` (`all = values()`). Each constant implements its behavior inline via
`touched(int, int)` / `touchedLine(int, int, int, int)`.

## Mutable state on the enum (shared, static)

- `key` - `KeyCode` bind (`v, i, l, b, e, g, r` respectively); read by
  `MapEditorDialog.doInput()`.
- `mode` - int, `-1` = "standard". `altModes` holds per-tool sub-mode names shown
  as `M1..M9` in the toolbar; `ctrl+1..9` switches them. Examples: pencil modes
  normal/replace/square/drawteams/underliquid; eraser erase-block/erase-ore; fill
  normal/team/erase/cliffs/underliquid.
- `edit` - whether the tool mutates tiles; `draggable` - whether dragging repeats
  it.

Because `mode` lives on the enum constant, it is global across sessions - a patch
persisting tool state should read/write these fields directly.

## Gotchas

- `fill` has a per-constant `IntSeq stack` for its flood-fill DFS, reassigned on
  `OutOfMemoryError`.
- There is **no rectangular selection tool** in v9 - selection-style features must
  be built on top (the pre-v9 mod added its own; see `old-src/`).
- `zoom`/`pick` are non-edit tools; `pick` samples the `drawBlock` under the
  cursor.
