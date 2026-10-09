# Mindustry index

Mindustry itself (game logic, content, UI dialogs, editor). Source:
`../Mindustry/core/src/mindustry/`.

This file is the curated class tree for `mindustry-docs/` - which Mindustry files matter
here and how they relate. The file map with read-when notes lives in `AGENTS.md`.

## Class tree

<!-- Curated, ~100-150 lines max - NOT a full dump. Mindustry core is 1009 files / 177k
     LOC. Full trees are derivable with find/glob and rot fast; a stale entry makes agents
     skip the source that would have corrected them.
     Record only what source cannot give:
       curation     - which files actually matter for mapping-utilities work
       relationships - what extends / patches / reflects what
       purpose      - one line per entry, why it exists -->

### editor/ - the map editor (this mod's core target)

Editor model, canvas, rendering, undo:
- `MapEditor` - editor model/state, global `Vars.editor`; brush/draw state, tile
  mutation, resize, undo entry points
- `MapEditorDialog` - the editor screen, global `ui.editor`; `extends Dialog
  implements Disposable`; owns MapView + all sub-dialogs as fields, rebuilds all UI
  in `build()` on every show
- `MapView` - editor canvas; `extends Element implements GestureListener`; owns
  editor camera (offset/zoom) and ALL editor input - there is no EditorController
- `EditorTool` - enum of 7 tools (zoom/pick/line/pencil/eraser/fill/spray); holds
  shared mutable mode/key/edit state on the enum constants
- `EditorTile` - `extends Tile`; records undo ops on every set* mutation
- `DrawOperation` - one undoable stroke; packed LongSeq of tile ops, in-place swap
  undo/redo
- `OperationStack` - undo/redo history, 30-entry cap with redo cursor
- `EditorRenderer` - chunked editor renderer (`implements Disposable`); reuses the
  game floor shader/framebuffer, mutates Core.camera temporarily
- `EditorSpriteCache` - one 60x60-tile chunk's mesh; shares the game's static index
  buffer

Sub-dialogs (all `extends BaseDialog`, long-lived fields of MapEditorDialog or
MapInfoDialog, most commit on hide):
- `MapInfoDialog` - map tags hub (name/desc/author); owns the rules/waves/objectives/
  locales/processors/assets sub-dialog fields
- `MapLoadDialog` - map picker; hands the chosen map to a `Cons<Map>` loader
- `MapResizeDialog` - size/shift input form only; work done by `MapEditor.resize`
  via a `ResizeListener` callback
- `MapGenerateDialog` - ordered GenerateFilter list + live pixmap preview; applied
  mode (mutates tiles) vs stored mode (writes `genfilters` tag)
- `SectorGenerateDialog` - generate editor map from planet sector + seed; wipes and
  reloads world/rules via `state.loadSector`
- `WaveInfoDialog` - SpawnGroup list editor; stages edits on a copy, commits
  `state.rules.spawns` on hide
- `WaveGraph` - `extends Table`; pan/zoomable wave counts/totals/health graph
- `MapObjectivesDialog` - MapObjective node-graph editor; also hosts the global
  static reflection-based field-UI registry (providers/interpreters)
- `MapObjectivesCanvas` - `extends WidgetGroup`; pannable tilemap of objective nodes
  + parent connectors
- `MapLocalesDialog` - per-Locale translation editor; commits `editor.tags["locales"]`
  + `state.mapLocales` on Apply
- `MapProcessorsDialog` - lists worldProcessor logic blocks by scanning the world;
  also reused in PausedDialog (play mode)
- `BannedContentDialog<T extends UnlockableContent>` - generic banned-content picker;
  NOT editor-specific - instantiated by CustomRulesDialog

editor/data/ - per-map asset manager (global data in `Vars.state.data`, a
`DataManager`):
- `MapAssetsDialog` - tabbed manager over 7 asset types; views indexed by
  `DataAssetType` ordinal
- `AssetView` - interface: build(dialog, table) + buildButtons(dialog, buttons)
- `MapBundlesView`, `MapImagesView`, `MapAudioView`, `MapContentView`,
  `MapEmojisView`, `MapPatchesView` - per-type views; some mutate global game state
  (Core.atlas, Vars.content) on rename/delete

### maps/ + io/ - map storage

- `Map` - metadata handle over a Fi; tags StringMap is the source of truth (rules =
  JSON in `tags["rules"]`; no rules field)
- `Maps` - global `Vars.maps`; discovery, save/import/remove, previews, filter+wave
  tag helpers
- `MapIO` - map-specific read/write over SaveIO; meta-only `createMap`, full
  `loadMap`, `writeMap`, PNG image IO, preview generation
- `SaveIO` / `SaveVersion` / `SaveOptions` - MSAV binary format (same for maps and
  saves); versioned readers Save1..Save13; regions meta/patches/content/map/
  entities/markers/custom
- `MapPreviewLoader` - `extends TextureLoader`; minimap previews; reflection-forces
  preview-safe Rules values
- `filters/GenerateFilter` - abstract per-tile filter (seed, buffered/post variants)
- `filters/FilterOption` - one UI row per filter parameter (Slider/Block/Toggle)
- `maps/filters/*` - ~16 concrete filters (Noise, Ore, Mirror, Scatter, ...), driven
  by the `Maps.allFilterTypes` registry

### Related outside editor/

- `game/Rules` - everything configurable; serialized into the rules tag; `editor`
  flag marks editor mode
- `game/Gamemode` - `Gamemode.editor.apply(rules)` produces editor rules
- `core/GameState` - `state.data` (DataManager), `state.mapLocales`, `state.rules`,
  `state.world`
- `mod/data/DataAssetType` - asset tab enum (patch, content, bundle, image, sound,
  music, emoji); ordinal-coupled to MapAssetsDialog.views
- `ui/dialogs/CustomRulesDialog` - rules editor; hosts BannedContentDialog instances
- `ui/dialogs/EditorMapsDialog` - map manager (extends MapListDialog), global
  `ui.maps`; new/import/open-in-editor
- `ui/dialogs/MapListDialog` - abstract filtered map list; static persisted
  MapViewSettings
- `ui/dialogs/MapPlayDialog` - play rules picker; embeds CustomRulesDialog; used for
  editor playtest
- `input/InputHandler` / `DesktopInput` / `MobileInput` - game input; only
  `state.isEditor()` guards, no editor controller class exists in v9
