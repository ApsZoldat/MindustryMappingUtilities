<!-- verified: v9 @ 2026-10-09, sources: scene/Scene.java, scene/Element.java, scene/event/SceneEvent.java, scene/event/ResizeListener.java, scene/event/SceneResizeEvent.java, scene/ui/Dialog.java, scene/ui/ScrollPane.java, scene/ui/layout/Scl.java, scene/ui/layout/Table.java, scene/ui/layout/Cell.java, util/viewport/Viewport.java, util/viewport/ScreenViewport.java, graphics/Graphics.java, graphics/Camera.java, graphics/HdpiUtils.java, math/Mat.java, ApplicationCore.java, ../../../backends/backend-sdl3/src/arc/backend/sdl/SdlApplication.java, ../../../backends/backend-sdl3/src/arc/backend/sdl/SdlGraphics.java, ../../../backends/backend-sdl3/src/arc/backend/sdl/SdlInput.java, ../../../backends/backend-android/src/arc/backend/android/AndroidInput.java; mindustry: core/src/mindustry/core/UI.java, core/src/mindustry/Vars.java -->

# Working with positions, sizes and scaling

Read [Element](Element.md) and [Scene](Scene.md) first; this guide only covers how the
coordinate spaces fit together and which conversions exist.

## The four spaces

1. **Physical (backbuffer) pixels** - `Graphics#getBackBufferWidth/Height`. Convert with
   `HdpiUtils#toBackBufferX/Y` and `toLogicalX/Y`.
2. **Logical pixels** - `Graphics#getWidth/getHeight`, the OS client area. On HiDPI they are
   window points, *not* backbuffer pixels, unless `app.config.hdpiMode == HdpiUtils.HdpiMode.pixels`
   makes them equal (`SdlGraphics#getWidth`). `Viewport#apply` goes through
   `HdpiUtils.glViewport`, which scales logical -> backbuffer when `HdpiUtils.setMode` is `logical`.
3. **Scene units** - `Scene#getWidth/getHeight` return the viewport world size. The default is a
   `ScreenViewport` (`Scene#Scene`), so 1 unit = 1 logical pixel unless you call
   `ScreenViewport#setUnitsPerPixel`.
4. **Element-local** - `Element#x, y` is the **bottom-left** corner; y grows **upward**
   (`Element#getY(Align.top)` returns `y + height`, `Element#setPosition` javadoc). Plus
   `originX/originY`, `scaleX/scaleY`, `rotation`, and `translation`.

Arc screen/input coordinates are **bottom-left origin, y-up**: both `SdlInput` (SDL gives top-left,
it sends `Core.graphics.getHeight() - scaleY(...)`) and `AndroidInput` flip y before dispatch. This
is the opposite of libGDX's top-left `Gdx.input.getY()` - every y you port needs checking.

## Conversions (all mutate the passed `Vec2` in place)

- `Scene`: `screenToStageCoordinates`, `stageToScreenCoordinates`, `toScreenCoordinates(Vec2, Mat)`,
  `hit(stageX, stageY, touchable)`.
- `Element`: `screenToLocalCoordinates`, `stageToLocalCoordinates`, `localToStageCoordinates`,
  `localToAscendantCoordinates(Element, Vec2)`, `localToParentCoordinates`,
  `parentToLocalCoordinates`.

"Stage" is a libGDX `Stage` -> `Scene` rename leftover (`Element`'s private field is still `stage`);
there is no `localToScene` alias. Gotcha: `stageToScreenCoordinates` and `toScreenCoordinates` flip y
(`Viewport#toScreenCoordinates` javadoc: origin top-left, y down) while `screenToStageCoordinates`
takes bottom-left input - they are **not** inverses, so do not round-trip them.

## Scl: authoring units

`Scl` (scene/ui/layout/Scl.java) is a plain multiplier, `Scl.scl()` / `Scl.scl(amount)`; it never
touches the viewport or scene coordinates. Desktop/web use the `product` factor; mobile rounds
screen density to 0.5 steps (min 1). The factor is cached until `Scl.setProduct/setAddition`
invalidates it - Mindustry sets product from its `uiscale` setting (`Vars`) and the mobile launchers
call `setAddition`.

How to size UI:
- **Layout constraints are unscaled**: `Cell` setters (`size/width/height/minWidth.../pad`) run
  through `Cell#scl` -> `Scl.scl`, and so do `Table#margin(...)`/padding and `Spacer`. Write
  `.width(300).pad(4)` in unscaled units; Arc scales them.
- **Raw `Element` state is scene units**: `x, y, width, height`, `setPosition`, `setWidth` (e.g.
  `Dialog`'s constructor `setWidth(150)`) are *not* scaled. Convert explicitly with `Scl.scl(n)`.
- Screen-relative sizing: `Core.graphics.getWidth() / Scl.scl(1f)` is the window width in unscaled
  units - the idiom Mindustry uses before passing it to a constraint.

## Window resize

Chain: backend (`SdlApplication`: pixel-size/display-scale change -> `graphics.updateSize()`, then
`resize(graphics.getWidth(), getHeight())` in logical pixels) -> `ApplicationCore#resize` fans out to
modules -> Mindustry's `UI#resize` -> `Core.scene.resize(w, h)` plus a Mindustry `ResizeEvent`.

`Scene#resize` only calls `viewport.update(w, h, true)`: world size becomes the new size and the
camera recenters (`Viewport#apply`). On the next `Scene#act`, root bounds are recomputed from
`Scene#marginLeft/Top/...`, so `setFillParent(true)` children and `Scene#table()` follow for free.
**Nothing else moves**: absolutely positioned children keep their x/y and can end up off-screen.

`SceneResizeEvent` is fired only by `Dialog#act`, when `scene.root` size changed since the last frame;
it bubbles (`SceneEvent#bubbles`, `Element#fire` walks ancestors to root). So it exists only while a
Dialog is in the tree - a `ResizeListener` on a plain element never fires.

Concrete patterns:
- Dialog-scoped (Arc): `Dialog#resized(invoke, Runnable)` or `new ResizeListener(){resized(){...}}`.
- Global (Mindustry): `Events.on(ResizeEvent.class, ...)`, fired after `scene.resize`.
- Eventless: fillParent + table layout; recompute fixed positions from
  `scene.getWidth()/getHeight()` inside `draw`/`act`.

## Moving the view

There is no Stage-camera equivalent: `Scene#getCamera`/`setViewport` exist, but the default camera is
recentered on every resize and nothing in Mindustry pans it - don't move the UI camera, use layout.
`ScreenViewport#setUnitsPerPixel` is the only scene-level zoom lever. For pannable content use
`ScrollPane`: `getScrollX/getScrollY`, `setScrollX/setScrollY` (clamped to 0..`getMaxX/getMaxY`),
`setScrollXForce/setScrollYForce`, `scrollTo(...)` in widget coordinates, percent accessors, and
`getVisualScrollX/Y` + `updateVisualScroll()` to snap after programmatic scrolling. **ScrollPane has
no zoom state at all** - zoom a content `Element` yourself.

## Gotchas

- `Table#setRound` is true by default: cell bounds go through `Math.round` in `Table#layout`, so
  fractional positions/sizes vanish; disable it for pixel-exact placements.
- Conversions mutate your vector; pass a `Tmp`/copy you own.
- `translation` is added on top of `x, y` inside `localToParentCoordinates` conversions.
- Rotation and scale are applied around `originX/originY` in local space (scale, then origin, then
  rotation - see `localToParentCoordinates`/`parentToLocalCoordinates`).
- Negative coordinates are legal everywhere (root sits at the margins; ScrollPane overscroll lets the
  internal amount go negative), but public `setScrollX/Y` clamp to 0..max.
- World <-> screen conversion for the map editor is Mindustry-side:
  [../mindustry-docs/MindustryIndex.md](../mindustry-docs/MindustryIndex.md).
