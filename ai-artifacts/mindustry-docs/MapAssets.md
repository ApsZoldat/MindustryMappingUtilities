<!-- verified: v9 @ 2026-10-09, sources: editor/data/MapAssetsDialog.java, editor/data/AssetView.java, editor/data/MapBundlesView.java, editor/data/MapImagesView.java, editor/data/MapAudioView.java, editor/data/MapContentView.java, editor/data/MapEmojisView.java, editor/data/MapPatchesView.java, editor/MapLocalesDialog.java, editor/MapProcessorsDialog.java, core/GameState.java, mod/data/DataAssetType.java -->

# map-assets

v9 per-map data packs: bundles, images, audio, content overrides, emojis, JSON
patches, locales, logic processors - editable from the editor (MapInfoDialog ->
assets/locales/processors).

## Where the data lives (three places)

1. **`Vars.state.data`** - a `DataManager` (`GameState.data`) holding
   bundles/images/audio/content/emojis/patches. This is *not* in editor fields;
   views call `state.data.getBundles()` etc.
2. **Locales** - `MapLocalesDialog` writes `editor.tags["locales"]` (JSON) and
   `state.mapLocales` on Apply.
3. **Processors** - the tiles themselves: `MapProcessorsDialog` scans
   `state.world` for center tiles with `Blocks.worldProcessor`.

## MapAssetsDialog (editor/data/)

`extends BaseDialog`. Tabs = `DataAssetType` enum order (patch, content, bundle,
image, sound, music, emoji - `mindustry.mod.DataAssetType`). The active view is
`views[currentType.ordinal()]` - an **ordinal-coupled array**; inserting an enum
constant mid-list silently misroutes views. All fields (`views`, `currentType`,
`searchString`, `rebuild()`) are package-private/private - patch by reflection or
same-package code.

- `changeType` special-cases `content`: `state.data.reloadContent(false)` +
  `regenerateContentSprites(false)`.
- `hidden`: `DataPatcher.fixContentArrays()` +
  `ui.editor.rebuildBlockSelection()`.
- `showEditMenu()`: zip import/export/clear over all assets.

`AssetView` interface: `build(dialog, table)` + `buildButtons(dialog, buttons)`;
views are stateless and read `diag.searchString`.

## View gotchas

- **MapContentView delete mutates the global `Vars.content` registry**; edit +
  images views call `reloadContent`/`regenerateContentSprites`.
- **MapImagesView rename/delete mutates the global `Core.atlas`** region map; zip
  import runs on `mainExecutor`.
- MapBundlesView reads lazily from cache (`bundle.tryLoadCache()`); add does not
  reload.
- MapEmojisView's `selected` set is cleared at the top of every `build()`.
- MapAudioView is instantiated twice (sound/music); an anonymous guard `Element`
  keeps audio silent while the dialog is open (removed on rebuild).
- MapPatchesView validates JSON (`Jval.read`) and rolls back the list on error.

## Locales and processors

- **Locales** = per-`Locale` `StringMap` translations consumed by logic
  (`LocalePrintI`) and objectives - not game bundles. The dialog stages edits
  (`lastSaved = locales.copy()`); Apply writes the tag + `state.mapLocales`.
  Rebuild ladder: `setup()` -> `buildTables()` -> `buildMain()`.
- **Processors** = world-processor logic blocks; rows rename `log.tag` (bypassing
  configuration), pick `log.iconTag`, open `log.showEditDialog`, delete the tile.
  The same dialog is reused in PausedDialog (play mode), so patches there affect
  gameplay UI too.
