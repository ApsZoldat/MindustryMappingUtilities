# Mindustry Editor — Principles

Conceptual notes on `Mindustry/core/src/mindustry/editor/` (v9 branch).
Contains *why* and *invariants* only. Class/field/method lists: grep the source.
Names below are search anchors; behavior was summarized from a dump and not re-verified line by line.

## 1. Roles

| Class | Role |
|---|---|
| `MapEditor` | Model: editor state, tile mutation entry points, undo stack |
| `MapEditorDialog` | Controller/UI: toolbar, palette, menus, save/load |
| `MapView` | Input + canvas: screen→tile mapping, tool dispatch, clipboard paste |
| `EditorRenderer` | Chunked rendering of terrain/buildings |
| `EditorTool` | Enum of tools; each constant overrides behavior |
| `EditorTile` | `Tile` subclass that records undo ops on every mutation |

Flow: input → `MapView` → `EditorTool.touched` → `MapEditor.drawBlocks` → `EditorTile` mutation records ops into the current `DrawOperation` → on mouse-up `editor.flushOp()` pushes it to `OperationStack` → renderer recaches affected chunks.

## 2. Core invariants

- **One tile class, two contexts.** `EditorTile.skip()` (true when `state.isGame() || editor.isLoading() || state.generating`) gates undo recording. While loading/generating/playing, mutations are silent; while editing, every mutation is recorded. Do not bypass `EditorTile` setters when you need undo.
- **Tile events are suppressed in editor mode.** `fireChanged`, `firePreChanged`, `recache`, `changed` are no-ops outside game mode. Do not rely on tile-change events firing during editing.
- **`loading` flag** suppresses undo while a map is loaded, so thousands of tile writes do not flood the stack.
- **One stroke = one `DrawOperation`.** A drag is accumulated and flushed once on mouse-up. Undo = one stroke.
- **No-op draws must not enter undo.** Data ops are recorded speculatively before mutation and rolled back (`removeLastOps`) if nothing changed.
- **Undo is symmetric.** `DrawOperation.updateTile` swaps current state into the op array and applies the old value, so undo and redo share the same code.
- **Limits:** undo depth 30; op coordinates are 14-bit (maps up to 16384×16384).

## 3. `rules.editor` flag

- `state.rules.editor` (read via `state.isEditor()`) gates behavior globally: tile update skipping, placement checks, logic-block editing, any-block placement, editor HUD.
- **Temporary-toggle pattern:** code sets `rules.editor = true`, performs an action normally blocked in editor mode, then restores it (seen in resize, clipboard paste, logic blocks). Always restore in `finally`-equivalent flow.
- **Save path** sets `rules.editor = false` before serializing rules, and strips runtime state (objective flags, stats, player unit) first.

## 4. Editor modes

- **Standalone:** dialog shown directly; `logic.reset()` + `editor.beginEdit(...)`.
- **In-game editing:** dialog hidden, editor rules applied, player unit spawned, game runs in `State.playing`; `resumeEditing()` returns to the dialog.
- **Playtest:** saves the map, then `control.playMap(...)`; pause menu offers "Resume editing".

## 5. Drawing and tools

- Tools are enum constants with alt modes; right-click temporarily switches to eraser, middle-click to zoom, Shift/Alt to pick; previous tool is restored on release.
- Dragging interpolates with Bresenham between last and current tile to avoid gaps.
- Draw pipeline preserves per-tile `data`, `floorData`, `overlayData`, `extraData` across block changes.
- `MapView.project` subtracts 0.5 for even-sized draw blocks so they center on the cursor.
- Flood fill is scanline-based; "replace" mode iterates the whole map instead of flooding; OOM is caught and retried (pragmatic hack).

## 6. Clipboard

- Capture rectangle **grows** to fully contain partially selected multiblocks.
- Rotate/flip remaps directional data (cliff bitmasks, char-overlay rotation) and point-based building configs.
- Preview floor variants are chosen by **source** world position, not clipboard-local position.

## 7. Resize

- Building `config()` is read **before** tile coordinates are reassigned, because configs may be relative to `tileX/tileY`.
- Point-based configs are shifted by the resize offset (`BuildPlan.pointConfig`); `rules.editor` is set temporarily.

## 8. Rendering

- World split into 60×60 chunks, each baked into one mesh; only dirty chunks are recached, queued in an `IntSet` and drained at half frame rate.
- **Recache the tile and its 4 neighbors**: floor/wall visuals cross tile boundaries.
- Terrain (floors/walls) uses the game's own floor renderer with surface animation off; buildings are drawn from chunk meshes. This is what allows independent show/hide toggles.
- Synthetic (team) blocks are sorted last within a chunk for correct overlap.
- Editor shaders must replicate the `255.0/254.0` alpha scaling used by Arc's default shader.

## 9. Save / load / preview

- Map data is run-length encoded; multiblock entities are written only for the center tile.
- Block renames are handled by `SaveFileReader.fallback` (+ mod name map); mods can add `CustomChunk`s.
- Preview generation uses `CachedTile`, which never creates buildings and touches no global state; safe off the main thread.
- Image import creates environment tiles only (no buildings; empty floor defaults to stone). Image export disables synthetic blocks.

## 10. Extension points

- **Objectives UI is reflection-driven:** `MapObjectivesDialog` builds edit UI from field types and annotations (`@Multiline`, `@LogicCode`, `@Second`, `@TilePos`, `@Researchable`, `@Synthetic`, `@Immutable`, `@Unordered`). Mods register custom field UI via `MapObjectivesDialog.setInterpreter` / `setProvider`.
- Generation filters (`MapGenerateDialog`) compute on a background executor with double buffering; preview is applied to the editor via `editor.load()`.

## 11. Hidden / easy to miss

- `MapEditorDialog` "Simulate" button is gated by the property `mindustry.editor.simulate.button`.