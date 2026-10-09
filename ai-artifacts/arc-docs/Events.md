<!-- verified: v9 @ 2026-10-09, sources: scene/event/SceneEvent.java, scene/event/InputEvent.java, scene/event/InputListener.java, scene/event/ClickListener.java, scene/event/DragListener.java, scene/event/ElementGestureListener.java, scene/event/FocusListener.java, scene/event/VisibilityListener.java, scene/event/Touchable.java, scene/Element.java, scene/Group.java, scene/Scene.java, scene/ui/TextField.java -->

# Scene events (libGDX: scene2d events)

Same model as libGDX: `Event` -> `SceneEvent`, `Actor` -> `Element`, `Stage` -> `Scene`.
All events are pooled (`Pools.obtain` in `Scene`/`Element`) - never retain one past
`handle()`. Dispatch is driven by [Scene](Scene.md); this file covers listener semantics.

## Propagation (scene/Element.java#fire, #notify)

`Element#fire` captures ancestor groups up front, then: root-down capture phase (each
ancestor's capture listeners), target capture, target bubble listeners, then bubbles
target-up to root. `event.stop()` halts; `event.cancel()` = stop + handle + `cancelled`.
`bubbles=false` skips the parent phase. `fire()` returns `event.cancelled` (not handled!),
while Scene input methods return `event.handled`.

Per element, `notify` walks two lists (`captureListeners`, `listeners`) in insertion order;
list mutation during iteration is deferred (`DelayedRemovalSeq`). A listener returning true
calls `event.handle()`; for `touchDown` it also registers touch focus (below).

Flags on `SceneEvent` (`scene/event/SceneEvent.java`): `capture, bubbles, handled, stopped,
cancelled`, plus `targetActor`/`listenerActor` public fields.

## InputEvent

`type` is the `InputEventType` enum: `touchDown, touchUp, touchDragged, mouseMoved, enter,
exit, scrolled, keyDown, keyUp, keyTyped`. Delta: `keyCode` and mouse buttons are
`arc.input.KeyCode` enums everywhere (no int keycodes/Buttons); scroll carries
`scrollAmountX/Y`. Coordinates are stage-space in `stageX/stageY`; `toCoordinates(actor,
out)` converts to a listener's local space (`InputListener` does this for you).

Hit testing: `Element#hit` = bounds (+ translation offset) and, when the `touchable`
argument is true, requires `Touchable.enabled`. `Group#hit` tests children last-to-first -
topmost (last added) wins, deepest hit returned. `Touchable` values in v9 are lowercase
`enabled`, `disabled`, `childrenOnly` (element not hit, children are; events still bubble
through it). `enter`/`exit` are fired by `Scene#act`, not by touch events; on desktop the
pure-mouse one uses `pointer == -1` (`ClickListener.over` relies on this).

## Touch focus (Scene-level)

Any listener returning true from `touchDown` is registered via `Scene#addTouchFocus` and
then receives `touchDragged`/`touchUp` anywhere (no re-hit-test), with
`listenerActor`/`targetActor` set. `Scene#cancelTouchFocus*` sends a synthetic touchUp at
`Integer.MIN_VALUE` - filter with `InputEvent#isTouchFocusCancel()`.

## Concrete listeners

- `InputListener`: unpacks the event into typed methods; `touchUp`/`touchDragged`/`enter`/
  `exit` always report handled/never handled respectively regardless of return value.
- `ClickListener`: press-and-release = `clicked()` on touchUp *over* the element
  (hit-test `isOver`, not the tap square); filters one button (`mouseLeft` default, null =
  any); one pointer at a time; `isPressed`/`isOver`/`isVisualPressed` (static
  `visualPressedDuration`, ~0.1s release grace), `getTapCount()` double-taps, `cancel()`
  drops the in-flight gesture. Arc extras: static `ClickListener.clicked` Runnable run on
  every click.
- `DragListener`: `dragStart`/`drag`/`dragStop` after leaving the 14px tap square;
  `getDeltaX/Y` = per-event movement with flipped sign. **Caveat:** its `touchDown` takes
  `int button` (no `@Override`) and does not match `InputListener#touchDown(KeyCode)`, so
  the dispatcher never reaches it and no touch focus is ever registered - verify before
  relying on it; `Element#dragged(Floatc2)` is the working drag helper.
- `ElementGestureListener`: wraps `arc.input.GestureDetector` - tap/longPress/fling/pan/
  zoom/pinch with coords converted to the element's local space. Use for gestures;
  `ClickListener` for plain taps. Ignores touch-focus-cancel touchUps; `getTouchDownTarget()`.

## Focus and visibility

`FocusEvent` (nested in `FocusListener`) carries `type` (`keyboard`/`scroll`), `focused`,
`relatedActor`; fired by `Scene#setKeyboardFocus`/`setScrollFocus` (lose on old, gain on
new, cancellable). `FocusListener` always returns false - cancel via `event.cancel()` inside
the handler. TextField grabs focus by calling `setKeyboardFocus` from its click listener;
[Dialog](Scene.md) uses the same events to trap/restore focus.

`VisibilityListener` handles `VisibilityEvent` (`isHide()`); `Dialog#show`/`hide` fire it,
and `Dialog#shown(Runnable)`/`hidden(Runnable)` are the convenience wrappers.
