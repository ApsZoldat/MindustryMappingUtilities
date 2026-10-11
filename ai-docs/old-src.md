# `old-src/` structure (reference only)

47 Java files, ~4 000 LOC, package root `mu`.

`old-src/` holds the pre-v9-rewrite implementation, kept for reference. It is committed,
but do **not** treat it as buildable - `build.gradle.kts` compiles the new `java/` + `assets/`
layout, so `old-src/` is never on the source path.

```
old-src/
├── resources/
│   ├── bundles/            localization: bundle.properties (en), bundle_ru, bundle_vi
│   └── subtitles           subtitle randomizer data
└── java/mu/
    ├── MUMain.java         mod entry point (extends mindustry.mod.Mod), hooks ClientLoadEvent
    ├── EditorVars.java     global mutable singletons: editor, view, ui, state, dialog, jsManager,
    │                       allMods + JSON class tags + package list for Rhino JS import
    ├── mods/               settings-toggleable patches of vanilla dialogs (each extends MUMod:
    │   │                   settingName + enable()/disable()/update() driven by Core.settings)
    │   ├── MUMod.java          abstract base of the mini-plugin framework
    │   ├── SettingsDialogMod   injects the mod's settings category (ui.settings.addCategory)
    │   ├── RulesDialogMod      patches CustomRulesDialog: hidden rules, revealed blocks,
    │   │                       better banned-content dialogs, planet background dialogs
    │   ├── ResizeDialogMod     patch of MapResizeDialog: minSize=1, maxSize=Integer.MAX_VALUE
    │   └── EditorDialogMod     swaps Vars.editor / Vars.ui.editor for the modded editor
    ├── editor/             the mod's parallel editor implementation
    │   ├── MUMapEditor         extends MapEditor; mode system + own undo/redo stack,
    │   │                       reflection into EditorRenderer.updateBlock/updateStatic
    │   ├── MUMapEditorDialog   extends MapEditorDialog; shadows private view/menu fields,
    │   │                       overrides build() and save()
    │   ├── MUMapView           extends MapView; replaces input handling (clears private
    │   │                       listeners via reflection)
    │   ├── EditorState         serializable editor session state
    │   ├── EditorMode          mode interface; NavigationMode (pan/zoom) and BlocksMode
    │   ├── EditorOperation / EditorOperationStack    undo/redo (superseded by v9 OperationStack)
    │   └── blocks/             block-editing subsystem used by BlocksMode
    │       ├── BlocksMode          block editing mode (brush, pick, selection)
    │       ├── TileData            per-tile snapshot used by operations
    │       ├── tools/              BlocksTool, BlocksBrushTool, BlocksPickTool
    │       ├── brushes/            BlocksBrush, RectBrush
    │       ├── actions/            BlocksAction, BlocksDrawAction, BlocksSelectionAction,
    │       │                       BlocksCliffsAction   (undo/redo steps)
    │       └── operations/         BlocksSelectionOperation, BlocksTilesOperation
    ├── ui/                 data-driven UI layer
    │   ├── EditorUI             builds floating windows from WindowData, hosts them over the view
    │   ├── Window               movable/resizable window widget
    │   ├── data/                JSON-serialized UI DSL: WindowData, TableData, CellData,
    │   │                        ButtonData, UIObjectData
    │   └── dialogs/             BetterBannedContentDialog, PlanetBackgroundDialog,
    │                            UIExplorerDialog (in-mod file browser)
    └── utils/              helpers
        ├── ChunkedGridBits     compact chunked bitset for large-map selections
        ├── MUReflect           reflection helpers used everywhere (private field access)
        ├── MUJson              JSON class tags / singleton handling for editor state
        ├── MUFiles             moves saved maps into subfolders
        ├── MUAnnotations       internal annotations (e.g. singletons)
        ├── JSManager           imports mod packages into the Rhino JS console
        ├── PlanetBackgroundDrawer  draws the selected planet background in the editor
        ├── UpdateChecker       checks GitHub releases for a newer mod version
        └── SubtitleRandomizer  randomizes the mod's subtitle (network fetch)
```
