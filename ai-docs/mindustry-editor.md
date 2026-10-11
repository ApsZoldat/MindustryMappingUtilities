# Mindustry Editor — Architecture & Internals

> Analysis of `../Mindustry/core/src/mindustry/editor/` and related systems (v9 branch).

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Core Classes](#core-classes)
3. [Tool System](#tool-system)
4. [Undo/Redo System](#undoredo-system)
5. [Rendering](#rendering)
6. [Input Handling](#input-handling)
7. [UI & Dialogs](#ui--dialogs)
8. [Map Objectives Editor (Node Graph)](#map-objectives-editor-node-graph)
9. [Map Serialization & IO](#map-serialization--io)
10. [Editor ↔ Game Integration](#editor--game-integration)
11. [Procedural Generation](#procedural-generation)
12. [Wave Editor](#wave-editor)
13. [Localization Editor](#localization-editor)
14. [Data Asset Management](#data-asset-management)
15. [Clever Patterns & Non-Obvious Solutions](#clever-patterns--non-obvious-solutions)
16. [Key File Reference](#key-file-reference)

---

## Architecture Overview

The editor uses a clean **model / view / controller** separation:

| Layer | Class | Role |
|-------|-------|------|
| **Model** | `MapEditor` | Editor state (tags, brush, draw block/team, undo stack). Performs tile mutations. |
| **Controller/UI** | `MapEditorDialog` | Arc `Dialog` that builds toolbar, block palette, menus, save/load. Owns `MapView`. |
| **Input/Canvas** | `MapView` | Arc `Element` + `GestureListener`. Handles mouse/gesture input, camera pan/zoom, tool dispatch, clipboard paste, draws canvas overlay. |
| **Renderer** | `EditorRenderer` | Draws terrain, buildings (via chunked sprite caches), shadows, clipboard preview. |
| **Tools** | `EditorTool` (enum) | 8 tools with alt modes, drag support, and per-tool behavior. |
| **Tile** | `EditorTile` | `Tile` subclass that intercepts all mutations to record undo ops. |
| **Undo** | `DrawOperation` / `OperationStack` | Packed-long undo system with 30-op history. |
| **Clipboard** | `EditorClipboard` / `EditorClipboardRenderer` | Copy/paste with rotate/flip transforms and GPU-accelerated preview. |

### Interaction Flow

```
MapEditorDialog → creates MapView → calls editor.beginEdit(map)
MapView → receives input → converts screen→tile via project() → calls tool.touched(x, y)
EditorTool → calls back into MapEditor.drawBlocks(...)
MapEditor → mutates EditorTile(s) → records ops into current DrawOperation
On mouse-up → MapView calls editor.flushOp() → pushes onto OperationStack
EditorRenderer → recaches affected chunks
```

---

## Core Classes

### MapEditor (`MapEditor.java`)

The central editor state manager.

**Key fields:**
- `tags` (StringMap) — map metadata (name, author, rules, etc.)
- `renderer` (EditorRenderer) — handles all editor rendering
- `context` (Context) — a `SaveLoadContext` subclass bridging editor and save/load
- `stack` (OperationStack) — undo/redo stack
- `currentOp` (DrawOperation) — active draw operation being accumulated
- `loading` (boolean) — true during `beginEdit`/`load`
- `drawBlock` / `drawTeam` / `rotation` / `brushSize` — current drawing state

**Brush sizes:** `{1, 1.5f, 2, 3, 4, 5, 9, 15, 20}`

**Inner `Context` class** (lines 409–441) — the bridge between editor and save system:
- `tile(index)` → `state.world.geti(index)` (raw array access)
- `resize()` → `state.resizeWorld()` (creates new World)
- `create()` → creates `EditorTile` and places in world
- `isGenerating()` → `state.generating`
- `begin()`/`end()` → toggle `state.generating` so tiles don't recache during load

### EditorTile (`EditorTile.java`)

Extends `Tile`. The tile type used exclusively in the editor. Intercepts every mutation to record undo ops.

**The `skip()` gate** (line 194):
```java
private boolean skip(){
    return state.isGame() || editor.isLoading() || state.generating;
}
```
When `skip()` is true, mutations happen **without recording undo ops**. This lets the editor reuse the game's real tile/world code — during loading/generating, tiles change silently; during editing, every change is recorded.

**Intercepted methods:**
- `setFloor()` → records `opFloor`
- `setBlock()` → records `opRotation`, `opTeam`, `opBlock`; handles multiblock center vs non-center logic
- `setTeam()` → records `opTeam`
- `setOverlay()` → records `opOverlay`

**Event suppression:** `fireChanged()`, `firePreChanged()`, `recache()`, `changed()` are all overridden to no-op when not in game mode — prevents the game's tile-change event system from firing during editing.

**`isEditorTile()` marker** (line 46–48): Returns true. `Tile` checks this in its rendering logic to distinguish editor tiles from game tiles without checking `state.isEditor()` everywhere.

---

## Tool System

### EditorTool (`EditorTool.java`)

An **enum-based state pattern** — each tool is an enum constant with its own instance initializer and method overrides:

```java
pencil(KeyCode.b, "replace", "square", "drawteams", "underliquid"){
    {
        edit = true;      // marks as editing tool
        draggable = true; // enables drag-to-draw
    }
    @Override
    public void touched(int x, int y){ ... }
};
```

### Tool List

| Tool | Key | Alt Modes | Notes |
|------|-----|-----------|-------|
| `zoom` | V | — | Pan/zoom (default) |
| `pick` | I | — | Eyedropper |
| `line` | L | `replace`, `orthogonal` | Bresenham line |
| `pencil` | B | `replace`, `square`, `drawteams`, `underliquid` | Draggable |
| `eraser` | E | `eraseores` | Draggable |
| `fill` | G | `replaceall`, `fillteams`, `fillerase`, `fillcliffs`, `fillunderliquid` | Flood fill |
| `spray` | R | `replace` | Random chance (0.012) |
| `copy` | — | — | Selection + clipboard |

### Tool Fields

- `altModes` — String array of mode names shown in UI
- `key` — KeyCode to activate the tool
- `mode` — Current alt mode (-1 = standard, 0..n for alt modes)
- `edit` — Whether tool modifies the canvas (marks saved state dirty)
- `draggable` — Whether tool responds to mouse drag

### Mode Selection

- **Desktop:** Right-click on tool button opens floating popup
- **Mobile:** Double-tap on tool button
- **Hotkeys:** Ctrl+1..n selects alt mode directly
- Current mode displayed as "M1", "M2", etc. next to tool button

### Drawing Pipeline (`MapEditor.drawBlocks`)

1. **Multiblock handling** — clamps position, checks overlap via `hasOverlap()`
2. **Data preservation** — saves `tile.data`, `floorData`, `overlayData`, `extraData` before changes
3. **Floor vs Block vs Overlay** — different logic paths for each type
4. **Change detection** — if data didn't actually change, removes the undo ops (prevents no-op undo entries)

### Flood Fill Algorithm (lines 236–295)

Uses **scanline flood fill** with an `IntSeq` stack:
- **Replace mode:** Iterates entire map directly (no flood fill)
- **Normal mode:** Flood fill from seed point with span tracking (`spanAbove`/`spanBelow`)
- **OOM protection:** Catches `OutOfMemoryError`, forces GC, continues

### Line Drawing (lines 28–51)

Uses `Bresenham2.line()` with two modes:
- **Mode 0 (replace):** Only replaces air
- **Mode 1 (orthogonal):** Constrains to straight horizontal/vertical line

### Drag Interpolation

`MapView.touchDragged` uses `Bresenham2.line` between the last and current tile to ensure no gaps when dragging fast.

### Right-Click / Middle-Click Shortcuts

- **Right-click** → temporarily switches to eraser
- **Middle-click** → temporarily switches to zoom
- **Shift/Alt** → temporarily switches to pick tool
- Previous tool restored on release

---

## Undo/Redo System

### OperationStack (`OperationStack.java`)

- **Max 30 operations** (`maxSize = 30`)
- **Index-based undo** — `index` is a negative offset from the end; undo decrements, redo increments
- **Truncate on new op** — `stack.truncate(stack.size + index)` clears redo history before adding

### DrawOperation (`DrawOperation.java`)

Each operation is a **packed long** encoding tile changes:

```java
@Struct
class TileOpStruct{
    @StructField(14) int x;      // 14 bits for X (up to 16384)
    @StructField(14) int y;      // 14 bits for Y (up to 16384)
    @StructField(3) int type;    // 3 bits for op type
    int value;                   // 32 bits for value
}
```

Arc's `@Struct` annotation processor generates pack/unpack methods. A whole drag-stroke of edits is one `LongSeq` of packed longs — extremely memory-efficient.

**Operation types:**

| Type | Value | Description |
|------|-------|-------------|
| `opFloor` | 0 | Floor block ID |
| `opBlock` | 1 | Wall/block ID |
| `opRotation` | 2 | Building rotation |
| `opTeam` | 3 | Team ID |
| `opOverlay` | 4 | Overlay block ID |
| `opData` | 5 | Packed data (tile+floor+overlay) |
| `opDataExtra` | 6 | Extra data |

### In-Place Undo (lines 52–56)

`DrawOperation.updateTile` is clever: it reads the current tile state, **overwrites the array entry with the current state**, then applies the old `value`. This means undo and redo use the same code — calling `updateTile` twice returns to the original state. The operation array is mutated in place, so undo and redo are symmetric.

### No-Op Draw Prevention (MapEditor lines 204–206)

When a block has `saveData`, data ops are recorded *before* mutating. After mutation, if the data didn't actually change, `removeLastOps(2)` rolls back the speculative ops. This prevents undo from filling with useless no-op entries.

---

## Rendering

### EditorRenderer (`EditorRenderer.java`)

**Chunked mesh caching:**
- World divided into **60×60 tile chunks** (`chunkSize = 60`)
- Each chunk is an `EditorSpriteCache` that builds a single mesh
- Only edited chunks are recached — `recacheChunks` is an `IntSet` drained every other frame
- **Half-rate updates:** `doUpdate = Core.graphics.getFrameId() % 2 == 0` — terrain and chunk recaching only run every other frame

**Custom shader** (lines 58–91):
- Samples from a `sampler2DArray` using a `v_depth` varying
- Each sprite's texture is bound to a depth layer
- **Alpha hack:** Multiplies alpha by `255.0/254.0` — classic libGDX/Arc workaround for alpha rounding issues

**Texture atlas approach:**
The vertex format packs position into UV coordinates via `EditorSpriteCache.pack()`:
```java
private float pack(float x, float y){
    return Pack.packUv((x + packX) / packW, (y + packY) / packH);
}
```
The world is drawn as a single textured quad with all building sprites baked into the UV space — an extremely efficient "texture atlas of the whole map" approach.

**Terrain vs buildings separation:**
- Terrain (floors/walls) drawn by game's own `renderer.blocks.floor` with `animateSurfaces` disabled
- Buildings drawn separately via chunk meshes
- Enables independent `showTerrain`/`showBuildings` toggles

**Shadow buffer reuse:**
Reuses the game's shadow framebuffer, drawn with `Shaders.darkness` shader. Carefully disables scissor test before drawing shadows to avoid UI clipping interference.

**Sorting for correct overlap:**
`recacheChunk` sorts tiles so synthetic (team-colored) blocks render last:
```java
tmpTiles.sort(Structs.comparingBool(b -> !b.block().synthetic()));
```

**Cross-shaped recache** (`updateStatic`, lines 167–173):
Recaches the tile **and its 4 neighbors** (left, up, right, down). Necessary because floor/wall rendering spans tile boundaries.

### EditorSpriteCache (`EditorSpriteCache.java`)

- `vertexSize = 4`: packed xy (1), color (1), packed uv (1), depth (1)
- `build()` creates `Mesh` with `VertexAttribute.packedPosition`, `color`, `packedTexCoords`, `depthCoords`
- `draw()` adds textured quads to vertex buffer with rotation support
- Takes external `float[] tmpVertices` buffer — no per-chunk allocation
- Shares index buffer across all caches via `SpriteIndices.get()`

### EditorClipboardRenderer (`EditorClipboardRenderer.java`)

- Renders paste preview at **0.75 alpha**
- Splits into multiple meshes when exceeding `maxSprites = 16000` (limited by shared `SpriteIndices` index buffer)
- **Variant-aware floor rendering:** Checks for `variantRegions` and uses `f.variant(x + c.srcX, y + c.srcY)` to render the correct floor variant based on **source world position**, not local clipboard position — preserves terrain pattern when pasting

---

## Input Handling

### MapView Input (`MapView.java`)

**Mouse handling:**
- **Left click:** Paste (if clipboard active) or start drawing
- **Right click:** Temporarily switches to eraser
- **Middle click:** Temporarily switches to zoom
- **Drag:** Bresenham line between last and current position for smooth drawing

**Gesture handling:**
```java
Core.input.getInputProcessors().insert(0, new GestureDetector(20, 0.5f, 2, 0.15f, this));
```
- **Pan:** Two-finger drag pans camera (only when zoom tool active)
- **Zoom:** Pinch-to-zoom (only when zoom tool active)
- **Activation:** `active()` requires zoom tool + keyboard focus on editor

**Camera controls (`act()` method):**
- WASD/Arrow keys pan camera via `Binding.moveX/moveY`
- Scroll zooms via `Binding.zoom`
- Shift/Alt temporarily switches to pick tool

**`project()` — even-size block offset (lines 387–400):**
Subtracts 0.5 from coordinates when the draw block has even size, so even-sized blocks (like 2×2 conveyors) are centered on the cursor rather than offset to the top-left.

### Keyboard Shortcuts (`MapEditorDialog.doInput()`)

- **Tool hotkeys:** B, E, G, L, I, R, V
- **Ctrl+number:** Alt mode select
- **R/E:** Rotate block
- **Ctrl+Z/Y:** Undo/redo
- **Ctrl+S:** Save
- **Ctrl+G:** Toggle grid
- **Escape:** Menu

---

## UI & Dialogs

### MapEditorDialog (`MapEditorDialog.java`)

Extends `Dialog` directly with `setFillParent(true)` and `margin(0)`. Three-column layout: tools (left), map view (center), block selection (right).

**Menu dialog** buttons: Save, Map Info, Generate, Resize, Import (map/file/image), Export (.msav/PNG), Edit In-Game, Playtest, Steam Workshop publish, Sector Generate, Simulate (hidden), Quit.

**Save logic** (lines 398–446):
- Sets `state.rules.editor = false` before saving
- Clears `objectiveFlags`, resets objectives, clears stats
- Removes player unit
- Serializes rules into `"rules"` tag via `JsonIO.write(state.rules)`
- Removes `width`/`height` tags (stored in Map object directly)
- Checks for name collision with built-in maps
- Preserves Steam ID if re-saving a workshop map

**Block selection** (lines 837–927):
- Search field with `rebuildBlockSelection()`
- Block sorting: CoreBlock first, then non-synthetic, then OverlayFloor, then by ID
- Collapsible config section for `editorConfigurable` blocks
- Grid of `ImageButton` with block `uiIcon`

**Tool palette** (lines 540–742):
- `ButtonGroup<ImageButton>` for tool buttons
- Alt-mode popup via right-click/double-tap
- Mode indicator label ("M1", "M2", etc.)
- Brush slider over `MapEditor.brushSizes` array
- Team selector: color-coded `ImageButton` grid for `Team.baseTeams`
- Visibility toggles: `showblocks`, `showterrain`, `showfloor`
- Paste panel (mobile only): floating panel with paste/cancel/flip/rotate buttons

### MapInfoDialog (`MapInfoDialog.java`)

Hub-and-spoke pattern — opens specialized editors:
- **Rules** → `CustomRulesDialog`
- **Waves** → `WaveInfoDialog`
- **Objectives** → `MapObjectivesDialog`
- **Generation** → `MapGenerateDialog` (non-applied mode)
- **Locales** → `MapLocalesDialog`
- **World Processors** → `MapProcessorsDialog`
- **Assets** → `MapAssetsDialog`

Fields: Map name (50 chars), Description (1000 chars), Author (50 chars).

### MapResizeDialog (`MapResizeDialog.java`)

Resize map dimensions + shift content.

### MapLoadDialog (`MapLoadDialog.java`)

Thumbnail grid with `ButtonGroup<Button>` for single selection. Each map shown as `BorderImage(map.safeTexture())` + name.

---

## Map Objectives Editor (Node Graph)

### MapObjectivesDialog (`MapObjectivesDialog.java`)

The most complex dialog (839 lines) — a full visual node-graph editor.

**Reflection-based field system** auto-generates UI for any `MapObjective` subclass:

- **`FieldInterpreter<T>`** — builds UI for a field type
- **`FieldProvider<T>`** — supplies default values
- **`TypeInfo`** — stores parameterized type info for `Seq<A>`, `ObjectMap<A,B>`, arrays

**Default interpreters registered in static block:**
- `String` → `TextField` (or `TextArea` if `@Multiline`, or logic editor button if `@LogicCode`)
- `boolean` → `CheckBox`
- `byte`/`int` → `TextField` with `digitsOnly` filter
- `float` → `TextField` with `floatsOnly` filter, supports `@Second` (x60) and `@TilePos` (x8) multipliers
- `UnlockableContent`/`Block`/`Item`/`UnitType` → icon button + content picker
- `Team` → color button + team picker
- `Color` → color preview + picker
- `Vec2`/`Point2` → delegates to float/int interpreters per component
- `Vertices` (float[]) → per-vertex x/y/color/u/v editing
- `Seq<T>` → full list editor with add/remove/reorder
- `Object[]` → delegates to `Seq` interpreter

**Custom annotations:**
- `@Multiline` — use `TextArea` for String fields
- `@LogicCode` — use logic editor button
- `@Second` — multiply float by 60
- `@TilePos` — multiply float by 8
- `@Researchable` — filter content selection
- `@Synthetic` — filter block selection
- `@Immutable` — hide remove button and prevent add
- `@Unordered` — hide reorder buttons
- `@LabelFlag` — special byte flag editing (background/outline checkboxes)
- `@IndexBool` — maps int -1/+1 to boolean

**Clipboard:** Ctrl+C serializes objective under mouse to clipboard; Ctrl+V deserializes and places at mouse tile position.

**Edit menu:** Copy/load objectives as JSON via clipboard, clear all.

### MapObjectivesCanvas (`MapObjectivesCanvas.java`)

Custom `WidgetGroup` with `setFillParent(true)` containing `ObjectiveTilemap` inner class.

**Grid:** `objWidth=5`, `objHeight=2`, `bounds=100`, `UnitSize=Scl.scl(48f)`

**ObjectiveTilemap:**
- Grid drawing with preview placement rect and moving tile highlight
- **Animated bezier curves** between parent/child objectives using `Lines.curve` and a moving dot (`Fill.circle`) whose position is computed via cubic bezier math

**ObjectiveTile** (extends `Table`):
- `Connector conParent` (left side, finds parents)
- `Connector conChildren` (right side, finds children)
- `Mover mover` (drag handler)
- Edit button opening `BaseDialog` with field interpreter
- Remove button

**Mover** — drag-and-drop with grid snapping, validation, and revert if invalid.

**Connector** (extends `Button`):
- `findParent` flag determines direction
- Custom `InputListener` for drag-to-connect behavior
- `canConnectTo()`: checks `findParent != other.findParent && tile() != other.tile()`
- Draws arrow/square indicators

---

## Map Serialization & IO

### MapIO (`MapIO.java`)

- **Format header:** `{'M', 'S', 'A', 'V'}` ("MSAV")
- **13 save versions** registered (Save1 through Save13)
- `createMap()` — reads header, version, meta region, constructs `Map` object
- `writeMap()` — delegates to `SaveIO.write()` with map tags and asset embedding option
- `loadMap()` — delegates to `SaveIO.load()`

### SaveIO (`SaveIO.java`)

- `save()` — writes with backup/restore on failure
- `write()` — fires `SaveWriteEvent`, writes header + version + version-specific data
- `load()` — loads with fallback to backup file on failure; fires `SaveLoadEvent` and `RulesLoadEvent`

### SaveVersion (`SaveVersion.java`)

**Region order:** meta, patches (v12+), content, map, entities, markers (v8+), custom

**Map serialization** (`writeMap`/`readMap`):
- Floor/overlay data with **run-length encoding** (consecutive same tiles)
- Block data with packed byte flags:
  - Bit 0: has entity (building)
  - Bit 2: has custom data (7 bytes: data, floorData, overlayData, extraData)
- Multiblock entities only written for center tile
- Consecutive non-entity blocks use RLE

**Version-gated IO:** Each `SaveVersion` handles format changes. `LegacySaveVersion` for pre-version-8 formats.

**Content header:** Fallback name mapping for renamed blocks. Hack for versions < 11 that don't read patch chunk.

### SaveFileReader (`SaveFileReader.java`)

- `fallback` ObjectMap — maps old block names to new names (e.g., `"dart-mech-pad"` → `"legacy-mech-pad"`)
- `modContentNameMap` — additional mod content renames
- `CustomChunk` interface for mods to register custom save data

### Map Preview Generation (`MapIO.generatePreview`)

The most complex method (lines 77–204):
- Uses `CachedTile` that overrides `setBlock` to **never create buildings** ("do not super.setBlock as that affects the current world; previews never create buildings")
- Custom `SaveLoadContext` with `preview = true` that touches no global state
- `onReadPreviewBuilding()` draws team-colored pixels
- Creates two pixmaps: floors and walls
- Final composite: `floors.draw(walls, true)`
- Post-processing: darkens liquid edges

### Image Export/Import

- `writeImage()` — renders world to `Pixmap` using `mapColor`. Synthetic blocks disabled for export to avoid data loss.
- `readImage()` — maps colors back to blocks via `ColorMapper`, only places environment tiles (no buildings). Defaults empty floors to stone.

### Maps (`Maps.java`)

- `saveMap()` — creates new `Map` from world dimensions and tags, calls `MapIO.writeMap()`, then scans world for teams/spawns, generates preview pixmap, writes cache file, creates texture
- `importMap()` — copies file, loads it, creates preview
- `loadPreviews()` — loads preview textures and cache files for all maps
- `createNewPreview()` — generates preview off main thread, creates texture on main thread
- `writeCache()`/`readCache()` — binary cache of spawns/teams (version byte + int + team data)

---

## Editor ↔ Game Integration

### The `rules.editor` Flag

`GameState.isEditor()` simply returns `rules.editor`. This one boolean gates behavior across the entire game:
- `Tile.setBlock` skips `update` in editor mode
- `Build` placement skips visibility/core-radius checks
- `LogicBlock`, `SwitchBlock`, etc. allow editing in editor mode
- `PlacementFragment` allows placing any block
- HUD shows editor-specific buttons

### Two Editor Modes

1. **Standalone editor** (`MapEditorDialog` shown directly): `shown()` callback calls `logic.reset()` and `editor.beginEdit(200, 200)`
2. **In-game editor** (`editInGame`): hides dialog, applies `Gamemode.editor` rules, spawns player unit, enters `State.playing`. Game runs with editor rules. `resumeEditing()` returns to dialog.

### Playtest Round-Trip

`playtest()` saves map then calls `control.playMap(...)`. `PausedDialog` offers "Resume editing" which calls `ui.editor.resumeEditing()` / `resumeAfterPlaytest()`.

### `state.rules.editor` Toggle Pattern

Several places temporarily set `state.rules.editor = true` to perform an action normally blocked in editor mode, then restore it. Examples: `MapEditor.resize`, `EditorClipboard.paste`, `LogicBlock`. Useful pattern for editor mods.

### Vars Wiring

- `Vars.editor` — static `MapEditor` instance
- `Vars.ui.editor` — the `MapEditorDialog`

---

## Procedural Generation

### MapGenerateDialog (`MapGenerateDialog.java`)

**Live preview with double buffering:**
- `buffer1`/`buffer2` — double-buffered generation to prevent race conditions
- `Pixmap` + `Texture` for preview rendering
- `BorderImage` with custom `draw()` that overlays filter visuals
- `InputListener` on preview for dragging `MirrorFilter` axes
- Loading overlay: `Stack` with `Styles.black8` + refresh icon

**Generation pipeline** (`update()` method):
1. Copies filters to avoid race conditions
2. Submits to `mainExecutor` (background thread)
3. For each filter: reads from buffer1, applies, writes to buffer2, swaps
4. Renders to `Pixmap` using `MapIO.colorFor()`
5. Posts texture update to main thread
6. Handles queued updates

**Filter list UI:** Grid of filter cards with name, randomize/move up/move down/copy/delete buttons. Filter options built via `FilterOption.build(t)`.

**Apply to editor:** Writes directly to world tiles using double-buffered `writeTiles`/`writeData` arrays, then `editor.load()` to apply.

**Edit menu:** Copy/load filters as JSON via clipboard, clear all filters, reset to defaults.

---

## Wave Editor

### WaveInfoDialog (`WaveInfoDialog.java`)

- Table of `SpawnGroup` entries with unit icon, name, begin wave display
- Copy/effect/unit/remove buttons
- Expanded row: begin/end/spacing/unitAmount+scaling/max/shields/guardian/team/spawn fields
- Spawn point selection dialog with grid of spawn points (limited to first 20, with "All" option)
- Sort system: `begin`, `health`, `type` with `Floatf<SpawnGroup>` comparators
- Clipboard: Copy via `maps.writeWaves(groups)`, Load via `maps.readWaves(...)`

### WaveGraph (`WaveGraph.java`)

- Three modes: `counts`, `totals`, `health`
- Pan via `ElementGestureListener.pan`
- Zoom via `scrolled` + `zoom` gestures
- Custom `rect()` drawing with grid lines, per-unit-type lines or totals/health bars, selection highlight, axis labels
- Color per unit type: `Tmp.c1.fromHsv(type.id / units.size * 360f, 0.7f, 1f)`
- Unit toggle buttons with hide/show all

---

## Localization Editor

### MapLocalesDialog (`MapLocalesDialog.java`)

- Three-panel layout: locale list (left), property cards (center), property add form (right)
- `MapLocales` data structure: locale name → `StringMap` of key-value pairs
- Property cards with collapse, key `TextField` with duplicate detection, remove, edit, value `TextArea` in `Collapser`
- **Color coding:** `missing` = accent, `same` = techBlue, `correct` = gray
- Property status compares across all locales to detect missing/same/correct translations
- **Icon insertion:** Searchable grid of icons from `Iconc.codes`, content icons, team icons
- **Bundle import/export:** Custom text format: `locale:\nkey = value\n...` with `\n` escaping

---

## Data Asset Management

### MapAssetsDialog (`MapAssetsDialog.java`)

- 7 tabs: patches, content, bundles, images, sound, music, emojis
- Each tab is an `AssetView` with `build()` and `buildButtons()` methods
- **Zip import:** Opens file, iterates `DataAssetType.all`, reads files by extension, handles content sub-directories
- **Zip export:** Writes all assets to `ZipOutputStream`, handling both embedded and cached assets
- **Clear all:** `state.data.unload()`

---

## Clever Patterns & Non-Obvious Solutions

### 1. Packed Long Operations
Tile operations packed into single `long` values using `@Struct` annotations — extremely memory-efficient for undo storage. 14-bit x/y fields limit maps to 16384×16384.

### 2. `EditorTile.skip()` — The Central Mutation Gate
Every mutation method calls `skip()` first. When true (game/loading/generating), mutations happen without recording undo. The same tile class works for both editor and game because `state.isGame()` distinguishes the context.

### 3. Data-Op Rollback for No-Op Draws
When a block has `saveData`, data ops are recorded before mutating. After mutation, if data didn't change, `removeLastOps(2)` rolls back the speculative ops.

### 4. `loading` Flag Suppresses Undo
Set true during `beginEdit`/`load`. While loading, `addTileOp` returns early, so thousands of tile changes during map load don't flood the undo stack.

### 5. Resize Preserves Relative Configs
`MapEditor.resize` fetches `build.config()` **before** reassigning tile coordinates, because configs can be relative to `tileX`/`tileY`. Uses `BuildPlan.pointConfig` to shift point-based configs by the resize offset, and temporarily sets `state.rules.editor = true`.

### 6. Flood Fill with OOM Guard
Catches `OutOfMemoryError`, sets `stack = null`, calls `System.gc()`, and reallocates — a pragmatic hack.

### 7. Clipboard Capture Grows to Fit Multiblocks
First computes selection rectangle, then **expands** it to fully contain any multiblock buildings partially touched. Prevents cutting buildings in half.

### 8. Clipboard Transform Remaps Directional Data
When rotating/flipping, directional block data is remapped: cliff `data` (8-directional bitmask) via `remapCliff`, `CharOverlayData` rotation via `remapChar`. Building configs that are point-based are also transformed.

### 9. In-Place Undo via Value Swapping
`DrawOperation.updateTile` reads current state, overwrites array entry with current state, then applies old value. Undo and redo use the same code — calling `updateTile` twice returns to original state.

### 10. Texture Atlas of the Whole Map
The editor renderer bakes all building sprites into UV space using `Pack.packUv()`, drawing the world as a single textured quad with a `sampler2DArray` shader.

### 11. Alpha Hack
Vertex shader multiplies alpha by `255.0/254.0` — classic workaround for alpha rounding issues.

### 12. Cross-Shaped Recache
`updateStatic` recaches the tile and its 4 neighbors because floor/wall rendering spans tile boundaries.

### 13. Reflection-Driven UI for Objectives
`MapObjectivesDialog` uses reflection + annotations to auto-generate edit UI for any `MapObjective` subclass. Mod writers can register custom interpreters/providers via `MapObjectivesDialog.setInterpreter`/`setProvider`.

### 14. Node Graph with Bezier Connections
`MapObjectivesCanvas` draws animated bezier curves between parent/child objectives with a moving dot computed via cubic bezier math.

### 15. Double-Buffered Generation
`MapGenerateDialog` uses `buffer1`/`buffer2` for parallel generation and preview rendering, with queued update handling.

### 16. `MapView.project` — Even-Size Block Offset
Subtracts 0.5 from coordinates when the draw block has even size, so even-sized blocks are centered on the cursor.

### 17. `addCliffs` — 8-Directional Bitmask
Computes cliff rotation as an 8-bit mask (one bit per neighbor direction) and stores in `tile.data`.

### 18. Hidden Simulate Feature
`MapEditorDialog.Simulate` — gated behind `OS.hasProp("mindustry.editor.simulate.button")`. Runs map logic at 2× delta for configurable seconds without spawning units.

### 19. `beginEdit(Pixmap)` — Image-to-Map
Imports a PNG as a map. Only places environment tiles (no buildings), defaults empty floors to stone.

### 20. Variant-Aware Clipboard Rendering
Clipboard renderer uses `f.variant(x + c.srcX, y + c.srcY)` based on **source world position**, not local clipboard position — preserves terrain pattern when pasting.

### 21. `CachedTile` for Preview Safety
Preview loading uses `CachedTile` which overrides `setBlock` to never create buildings, and `preChanged()` to avoid proximity/garbage. Runs off the main thread.

### 22. Run-Length Encoding for Map Data
Both floor/overlay and block data use consecutive-count RLE for compression in the save format.

### 23. `SaveFileReader.fallback` Name Mapping
Maps old block names to new names for backwards compatibility (e.g., `"dart-mech-pad"` → `"legacy-mech-pad"`).

### 24. `EditorTile.fireChanged` Suppression
Overrides `fireChanged`, `firePreChanged`, and `changed` to no-op when not in game mode, preventing the game's tile-change event system from firing during editing.

### 25. Shared Index Buffer
`SpriteIndices.get()` provides a shared index buffer across all `EditorSpriteCache` instances, avoiding per-chunk allocation.

---

## Key File Reference

| File | Purpose |
|------|---------|
| `editor/MapEditor.java` | Core editor state, drawing pipeline, undo/redo API |
| `editor/MapEditorDialog.java` | Main editor window, tool palette, block selection, menus |
| `editor/MapView.java` | Input handling, tool state, selection, clipboard |
| `editor/EditorRenderer.java` | Chunked rendering with custom shader |
| `editor/EditorTool.java` | Tool enum with 8 tools, flood fill, line drawing |
| `editor/EditorTile.java` | Tile subclass intercepting all mutations for undo |
| `editor/DrawOperation.java` | Packed long tile operations |
| `editor/OperationStack.java` | Undo/redo stack (max 30) |
| `editor/EditorSpriteCache.java` | Custom sprite batching with mesh |
| `editor/EditorClipboard.java` | Copy/paste with rotate/flip transforms |
| `editor/EditorClipboardRenderer.java` | GPU-accelerated clipboard preview |
| `editor/MapObjectivesDialog.java` | Visual node-graph objective editor |
| `editor/MapObjectivesCanvas.java` | Canvas widget for objective node graph |
| `editor/MapGenerateDialog.java` | Procedural generation with live preview |
| `editor/MapInfoDialog.java` | Map metadata editing hub |
| `editor/MapResizeDialog.java` | Map resize dialog |
| `editor/MapLoadDialog.java` | Map browser with thumbnails |
| `editor/WaveInfoDialog.java` | Wave/spawn group editor |
| `editor/WaveGraph.java` | Live wave visualization graph |
| `editor/MapLocalesDialog.java` | Localization/translation editor |
| `editor/MapProcessorsDialog.java` | World processor management |
| `editor/SectorGenerateDialog.java` | Sector-based generation |
| `editor/BannedContentDialog.java` | Ban/unban content items |
| `editor/data/MapAssetsDialog.java` | Data asset management (7 tabs) |
| `io/MapIO.java` | Map file I/O, preview generation, image export/import |
| `io/SaveIO.java` | Save/load system with backup/restore |
| `io/SaveVersion.java` | Version-specific serialization |
| `io/SaveFileReader.java` | Fallback name mapping, custom chunks |
| `io/SaveLoadContext.java` | Abstract bridge between serialization and world |
| `maps/Maps.java` | Map collection management, save/load/previews |
| `maps/Map.java` | Map metadata container |
| `core/GameState.java` | Game state with `isEditor()` check |
| `core/World.java` | Tile data container |
| `core/UI.java` | UI manager with `ui.editor` reference |
| `Vars.java` | Static references including `Vars.editor` |
| `input/Placement.java` | Line/rectangle normalization, A* pathfinding |
| `input/Binding.java` | Key binding definitions |
| `ui/dialogs/BaseDialog.java` | Base dialog with title, pause, resize |
| `ui/dialogs/PausedDialog.java` | Pause menu with "Resume editing" |
| `ui/dialogs/EditorMapsDialog.java` | Map list in editor context |
| `world/blocks/logic/LogicBlock.java` | Logic building with compressed code serialization |
| `world/blocks/logic/CanvasBlock.java` | Canvas building with pixmap preview |
| `world/blocks/logic/MemoryBlock.java` | Memory building with sentinel-based storage |
| `world/blocks/logic/MessageBlock.java` | Message building with string serialization |
