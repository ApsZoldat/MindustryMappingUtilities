<!-- verified: v9 @ 2026-10-09, sources: maps/Map.java, maps/Maps.java, io/MapIO.java, io/SaveIO.java, io/SaveVersion.java, maps/MapPreviewLoader.java -->

# map-files

How Mindustry stores maps. A map file is a **save file** (same `MSAV` binary
format, header `{'M','S','A','V'}`); "map" vs "save" is only a matter of call-site
options.

## Map (maps/Map.java)

A metadata handle over a `Fi`; `implements Comparable<Map>, Publishable`. **No
`rules` or `sectors` field** - everything variable lives in the `tags`
`StringMap`:

- rules are a JSON string in `tags["rules"]`, parsed on demand by
  `rules(Rules base)`/`rules()` via `JsonIO`;
- generation filters: `tags["genfilters"]` (`filters()`);
- `name()` = `tags["name"]`; also `width/height`, `teams` (IntSet), `spawns`,
  `custom`, `version`, `mod`, `workshop`;
- `previewFile()` = `mapPreviewDirectory/<name>_v2.png`, `cacheFile()` =
  `<name>-cache_v2.dat` (binary spawns/teams cache).

## Maps (maps/Maps.java) - the manager, global `Vars.maps`

- `load()`/`reload()` scan defaults, `customMapDirectory`, workshop, and mod files
  (`loadMap(Fi, boolean)` -> `MapIO.createMap`).
- **Editor save chain**: `maps.saveMap(editor.tags, false)` -> picks or overwrites
  a file, builds `new Map(file, w, h, tags, true)`, calls
  `MapIO.writeMap(file, map, embedAssets)`, rescans the world for
  `teams`/`spawns`, writes the cache + preview PNG asynchronously.
  `removeMap(Map)` and `importMap(Fi)` round out management.
- Helpers the editor uses: `readFilters(String)` (parse `genfilters` or
  defaults), `writeWaves`/`readWaves`, `createNewPreview`/`queueNewPreview`,
  `loadPreviews`.

## MapIO / SaveIO (io/)

- `MapIO.createMap(Fi, custom)` reads **only the `meta` region** (header ->
  version -> `SaveIO.getSaveWriter(version)`) into tags - cheap listing without
  loading tiles.
- `MapIO.loadMap(map[, SaveLoadContext])` -> `SaveIO.load` reads everything into
  the live world.
- `MapIO.writeMap` -> `SaveIO.write(stream, SaveOptions{extraTags,
  embedAssets})`; `SaveVersion.write` writes regions `meta, patches, content, map,
  entities, markers, custom`; `writeMeta` injects `rules`
  (`JsonIO.write(state.rules)`), `width/height`, stats. The editor strips
  `state.rules.editor` before saving and restores it after.
- Image IO: `MapIO.isImage/writeImage/readImage` (PNG import/export).
- `SaveIO.load` falls back to a `-backup` file on corruption.

## MapPreviewLoader

`extends TextureLoader` (Arc asset loader); `MapPreviewParameter` carries the
`Map`, and a failed load queues a regenerated preview. Its `setupLoaders()` uses
**reflection into `Rules`/`GameState.rules`** (`header`, `schematicsAllowed`,
`staticFog`) to force preview-safe values while rendering minimaps - fragile
across versions; check here first when previews break after an update.

See also [MapAssets.md](MapAssets.md) for the separate per-map data packs
(bundles/images/audio/locales) added in v9.
