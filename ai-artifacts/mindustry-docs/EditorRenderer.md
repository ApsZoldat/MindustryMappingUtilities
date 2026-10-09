<!-- verified: v9 @ 2026-10-09, sources: editor/EditorRenderer.java, editor/EditorSpriteCache.java -->

# EditorRenderer

Chunked static renderer for the editor canvas. `implements Disposable`, owned by
`MapEditor` (public field `editor.renderer`), drawn from `MapView.draw()`.

## How it renders

- The world is split into `chunkSize = 60` tile chunks, each an
  `EditorSpriteCache` (a mesh of packed vertices: position + color + uv +
  texture-array depth). Caches share the **game's** static index buffer
  (`SpriteIndices.get()`) and reuse the floor renderer's shader/framebuffer.
- `draw(tx, ty, tw, th)` swaps `Core.camera`/`Draw.proj` manually and restores
  them afterwards - it must not nest. Updates/recaches only run on even frame ids
  (`Core.graphics.getFrameId() % 2 == 0`).
- Team colors: `recacheChunk` draws a `block-border` sprite tinted
  `tile.build.team.color` per building.
- Layering: floors first, blocks sorted so synthetic draw-only blocks come last;
  overlays are part of the floor pass.

## Cache invalidation

- `updateStatic(x, y)` - floor changed: recaches that tile plus its 4 neighbors.
- `updateBlock(Tile)` / `updateBlock(x, y)` - queues the containing chunk into
  `recacheChunks`; `recache()` flushes the queue; `recacheTerrain()` /
  `recacheShadows()` / `recacheChunk(cx, cy)` do full rebuilds.
- `resize(w, h)` disposes and rebuilds the chunk grid and clears `StaticWall`
  darkness. Called by `MapEditor.updateRenderer()`.

`showFloor/showTerrain/showBuildings` (`MapEditor` fields) gate the passes inside
`draw`.

## EditorSpriteCache

One chunk: a `Mesh` built lazily by `build(IndexBufferObject)` - `draw(...)`
appends a vertex quad (throws if called after building), `render(Shader)` draws,
`dispose()` frees. The vertex buffer is caller-supplied (passed in from
`EditorRenderer`), and the mesh shares the game's static index buffer rather than
owning one.
