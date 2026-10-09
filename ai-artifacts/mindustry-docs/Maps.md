<!-- verified: v9 @ 2026-10-09, sources: maps/Maps.java, io/MapIO.java, io/SaveIO.java, maps/Map.java, editor/MapEditorDialog.java -->

# Guide: working with map files

Read first: [MapFiles.md](MapFiles.md).

## Listing and loading maps

```java
for(Map map : maps.all()){ ... }          // all discovered maps
maps.customMaps()/defaultMaps()/moddedMaps()
```

- Parse a file cheaply (meta only): `MapIO.createMap(fi, custom)`.
- Load into the live world: `MapIO.loadMap(map)` (optionally with a
  `SaveLoadContext`); the editor path is `editor.beginEdit(map)`, which loads
  through its own context.
- Play: `control.playMap(map, rules, playtesting)` (see `MapPlayDialog`).
- Import: `maps.importMap(fi)` (copies + loads + regenerates preview, rolls back
  on failure); PNG detection via `MapIO.isImage`.

## Reading rules, waves, filters

All come from `map.tags` strings:

```java
Rules rules = map.rules();                    // JsonIO parse of tags["rules"]
Rules withBase = map.rules(baseRules);
Seq<GenerateFilter> filters = map.filters();  // tags["genfilters"]
```

- Waves for display: `maps.readWaves(map)`; to build a list yourself,
  `maps.writeWaves(tags, groups)` into a scratch tag map first.

## Saving a map from editor state

`MapEditorDialog.save()` is the reference implementation:

1. `editor.tags.put("rules", JsonIO.write(state.rules))` (also strip
   `width`/`height` tags - the writer regenerates them).
2. Temporarily clear `state.rules.editor` (restore it after).
3. `maps.saveMap(editor.tags, false)` - overwrites the same-name custom map,
   writes the `MSAV` file via `MapIO.writeMap`, rescans the world for
   teams/spawns, writes the binary cache and queues an async preview PNG.

Built-in maps cannot be overwritten (`handleSaveBuiltin` blocks it).
`maps.removeMap(map)` deletes the file, list entry and preview texture.

## Preview regeneration

- `maps.createNewPreview(map)` (background `MapIO.generatePreview`); previews
  render with reflection-forced safe rules (see [MapFiles.md](MapFiles.md) -
  MapPreviewLoader). If minimap previews break after a game update, check the
  `MapPreviewLoader.setupLoaders()` field names first.

## Gotchas

- `Map` has no live world pointer - tags are the source of truth, and `rules()`
  re-parses JSON on every call.
- The editor and a save game share the `SaveIO` format; versioned readers
  (`Save1..Save13`) pick the right `SaveVersion`. `SaveIO.load` falls back to
  `-backup` files.
- Per-map data packs (bundles, locales, ...) are separate from `tags` - see
  [MapAssets.md](MapAssets.md).
