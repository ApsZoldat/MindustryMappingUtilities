<!-- verified: v9 @ 2026-10-09, sources: scene/Scene.java, scene/ui/Dialog.java, scene/event/ResizeListener.java, scene/event/SceneResizeEvent.java, Core.java, Input.java -->

# Scene (libGDX: Stage)

`arc.scene.Scene` is the renamed Stage. The mechanics (touch focus, capture/bubble, one root
group) are the same; the rename is incomplete - coordinates and events still say "stage"
(`screenToStageCoordinates`, `InputEvent.stageX`). Mindustry code says "stage" for a Scene.

## Structure

- One `public final Group root` - no layer stack. Z-order = child order in root (last child
  on top, gets input first). `Scene#add`/`table()`/`addListener` all delegate to root.
- `root.touchable = Touchable.childrenOnly`, so the root itself is never hit-tested.
- `marginLeft/Right/Top/Bottom` shrink the root inside the viewport; `act()` repositions it.
- Per-scene widget style registry (`getStyle`/`addStyle`/`registerStyles`) - no libGDX
  equivalent. Widgets look up default styles here; Mindustry registers them at startup.
- Only one global scene: `Core.scene` (public static field in `Core.java`). arc-core never
  assigns it; in Mindustry, `core/UI.java` does `Core.scene = new Scene()` and
  `Core.input.addProcessor(Core.scene)`. A second Scene can be registered as another
  input processor, but nothing in arc-core does this.

## Input entry

Scene implements `arc.input.InputProcessor`. Backends (outside arc-core) push events into
`Core.input`, an abstract `Input` holding an `InputMultiplexer` (a `KeyboardDevice` first,
then registered processors in order; a processor returning true stops the chain -
`arc/input/InputMultiplexer.java`). Registering the scene is the app's job (see above).

Dispatch inside the Scene (`Scene#touchDown` etc.): screen coords -> `viewport.unproject`
-> `hit()` -> `Element#fire` on the target. Keys go to `keyboardFocus` (or root), scroll to
`scrollFocus` (or root). touchDragged/touchUp skip hit testing and are delivered to
registered touch-focus listeners. Listener-level details: [events](Events.md).

## Keyboard/scroll focus

Two plain fields `keyboardFocus, scrollFocus` on the Scene - no focus stack, no tab order
in arc-core (click-to-focus only, e.g. TextField's inner listener calls
`Scene#setKeyboardFocus` on left click; `Element#requestKeyboard`/`requestScroll` are
helpers). `setKeyboardFocus` fires a cancellable `FocusEvent` (lose on old, then gain on
new; gain-cancel reverts). `act()` clears focus whose element became invisible/detached.
`hasField()` = focus is a TextField; `hasDialog()`/`getDialog()` infer a dialog from focus
(there is no dialog stack on the Scene).

## Dialog stacking (scene/ui/Dialog.java)

`Dialog#show(Scene, Action)` saves previous keyboard/scroll focus, `stage.add(this)`, then
takes both focuses; `hide(Action)` restores them (if current focus is the dialog or a
descendant). Modal dialogs block input by overriding `Dialog#hit` to return themselves when
nothing else was hit, and a `FocusListener` cancels focus moves to elements outside a
top-most modal dialog. `Dialog#draw` self-heals: if keyboard focus is null, it focuses the
top-most Dialog child of root.

## Camera/viewport

Arc v9 has `Viewport` (package `arc.util.viewport`, unlike old Arc): default is
`ScreenViewport` (1 unit = 1 pixel, y-up camera), replaceable via
`new Scene(Viewport)`/`setViewport`. `getWidth/Height` = viewport world size;
`getCamera()` = viewport camera; drawing uses the global batch: `Draw.proj(camera)`,
`root.draw()`, `Draw.flush()` (no SpriteBatch owned by the scene).

Conversions: `screenToStageCoordinates` (unproject), `stageToScreenCoordinates` (project +
y-flip), `toScreenCoordinates(coords, Mat)` for transformed points, `calculateScissors`.
Element-side: `localToStageCoordinates`/`stageToLocalCoordinates` (see [Element](Element.md),
[guide](Coordinates.md)).

## Resize

`Scene#resize(width, height)` only updates the viewport - it fires nothing. The only
`SceneResizeEvent` source in arc-core is `Dialog#act`, which polls `scene.root` size each
frame and fires the (pooled) event on itself when it changed; it bubbles from the dialog.
`ResizeListener` is a trivial adapter calling `resized()`, and `Dialog#resized(Runnable)`
adds one. Plain elements get no resize events - add your own listener to root or use
`Dialog#resized`.
