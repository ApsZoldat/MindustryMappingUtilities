<!-- verified: v9 @ 2026-10-09, sources: editor/MapView.java, input/InputHandler.java, input/DesktopInput.java -->

# MapView

The editor canvas: an `Element implements GestureListener` that owns the editor
camera and all editor input. There is **no `EditorController` in v9** - the game
input classes (`InputHandler`/`DesktopInput`/`MobileInput`) only guard a few calls
with `state.isEditor()`; everything editor-input lives here.

## Camera

- `offsetx/offsety/zoom` (private; zoom clamped 0.2..20 via `clampZoom`).
- Pan: gesture `pan()` - only when `active()` (requires `tool == EditorTool.zoom`,
  scroll focus, and `ui.editor.isShown()`) - or WASD in `act()` via
  `Core.input.axis(Binding.moveX/moveY)`.
- Zoom: scroll wheel (`Binding.zoom` axis in `act()`) or pinch; both scale
  relative to the current zoom.
- Coordinates: `project(float, float)` -> `Point2` tile (screen->world; accounts
  for even-size `drawBlock` offset), `unproject(int, int)` -> `Vec2` screen.
  `center()` re-centers on the map.

## Input

- The ctor inserts a `GestureDetector` at input processor index 0 (pan/zoom/pinch);
  a scene `InputListener` handles mouse and keyboard.
- Right mouse temporarily switches to `EditorTool.eraser`, middle to
  `EditorTool.zoom`; holding shift/alt temporarily selects `pick`. On touch
  devices the brush cursor draws only while dragging (`!mobile || drawing`).
- Left-drag applies the current tool; drags interpolate along `Bresenham2.line` so
  fast strokes leave no gaps. `touchUp` calls `tool.touchedLine(...)` for the line
  tool, then **`editor.flushOp()`** - this is where a stroke becomes one undo
  entry.

## Drawing

`draw()` clips with `ScissorStack`, draws the map border, delegates the map itself
to `editor.renderer.draw(...)`, then grid, brush outline (`Lines.poly` over
precomputed `brushPolygons`, one polygon per brush size), pencil/multiblock
squares. Everything runs on the main thread; the element reads the globals `editor`
and `ui.editor` directly.

Reflect-worthy private fields: `zoom`, `offsetx/offsety`,
`drawing/lastx/lasty/startx/starty`, and `tool` (package-private).
