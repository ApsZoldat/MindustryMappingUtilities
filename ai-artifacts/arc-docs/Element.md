<!-- verified: v9 @ 2026-10-09, sources: scene/Element.java, scene/Group.java, scene/Scene.java, scene/ui/layout/WidgetGroup.java, scene/ui/layout/Collapser.java, scene/ui/ScrollPane.java, scene/event/Touchable.java, struct/SnapshotSeq.java, graphics/g2d/Draw.java, graphics/g2d/ScissorStack.java; libGDX master: scene2d/event/Touchable.java -->

# Element (libGDX: Actor)

Same scene2d model, different names and a different rendering hook. This covers the delta only; dispatch and bubbling are in [events](Events.md), conversion math in [coordinates](Coordinates.md).

## Rename map

| libGDX | Arc v9 |
|---|---|
| `Actor` | `Element` (scene/Element.java) |
| `Stage` | `Scene`; accessor `getScene()` / `protected setScene()` - no `getStage()` exists in `arc/scene` |
| `addActor(At/Before/After)` | `addChild(At/Before/After)` |
| `removeActor` | `removeChild(child[, unfocus])`; `Element#remove()` delegates with unfocus |
| `fire(Event)` | `fire(SceneEvent)` |
| `draw(Batch, float parentAlpha)` | `draw()`, no arguments |
| `getName/setName`, `setX`, `setVisible`, `setTouchable` | public fields `name`, `x`, `y`, `visible`, `touchable` - no setters (`width`/`height` are protected, "do not modify without sizeChanged"; `getWidth/setWidth` survive, `getX()` only as `getX(int align)`) |

Coordinate helpers keep the old vocabulary: `stageToLocalCoordinates`, `localToStageCoordinates`, `keepInStage` say "stage" while the type is `Scene`. `hit()`'s javadoc still links `getTouchable()` and `isVisible()` - neither exists, read the fields.

## Draw pipeline

- No batch parameter: `Element#draw()` body is `validate()` only; widgets call static `Draw.*` (arc/graphics/g2d/Draw.java) into the global `Core.batch`.
- `parentAlpha` is a protected **field**, not an argument: `Group#drawChildren` does `parentAlpha *= this.color.a` and assigns it to each child; leaves multiply it themselves (`Image#draw`: `Draw.alpha(parentAlpha * color.a)`).
- `Group#draw`: `if(transform) applyTransform(computeTransform()); drawChildren(); if(transform) resetTransform();` - the Mat helpers survive but swap the global transform via `Draw.trans()` (flushes).
- `transform` defaults to **false**, though `setTransform`'s javadoc still says "When true (the default)". Off = each child's `x/y` temporarily offset by group `x/y` + `child.translation` around its `draw()`, then restored; the group's rotation/scale/origin are *not* applied to children. Rotation-dependent widgets opt in: the `ScrollPane` and `Collapser` constructors call `setTransform(true)`.
- A non-group element gets no transform application at all; its `draw()` reads `rotation`/`scaleX`/`originX` itself. An override skipping both `validate()` and `super.draw()` silently skips layout too.
- `drawChildren` runs in child index order, skipping `!visible` children and `cullable` children outside `cullingArea`.

## Invalidation / layout

- State: private `needsLayout` (starts true) + `layoutEnabled`. `invalidate()` sets the flag; `invalidateHierarchy()` also climbs `parent` calling `invalidate()` on each but bails immediately `if(!layoutEnabled)`. `setLayoutEnabled(false)` freezes a subtree (WidgetGroup's version recurses into children).
- `validate()`: if `fillParent && parent != null` it resizes to the parent first, then clears the flag and runs `layout()`. `pack()` = setSize(pref) + `validate()`.
- **When it runs:** lazily inside the draw path - `Element#draw()` validates first, and widgets overriding `draw()` re-add the call (`WidgetGroup#draw`, `Table#draw`, `Label#draw`, `Button#draw`). `Scene#draw()` has no validation pass and a plain `Group#draw()` does not validate itself: a bare group never re-layouts, only widget elements do.
- Every add/remove ends in `childrenChanged()`: `WidgetGroup` overrides it to `invalidateHierarchy()` so tables re-layout next frame; `Group#childrenChanged()` is a no-op.
- `WidgetGroup` carries a *second* private `needsLayout`/`layoutEnabled` pair (Element's are private) and overrides `invalidate`/`validate`/`needsLayout` around its own copy - reflection must know which level it is poking.
- Mutating children during draw is safe: `children` is a `SnapshotSeq` and `drawChildren` iterates the `begin()` snapshot. Adds join the live list but are not drawn until next frame; a child removed mid-draw may still be drawn that frame (its `parent`/scene are already null). Layout for either change lands next frame, when the parent next validates.

## Coordinates, clipping, z

- `x/y` are parent-relative, bottom-left origin. `translation` (Vec2) is Arc-only: a visual offset the parent folds into `child.x/y` during draw, included in every conversion and in `hit()` bounds, without changing `x/y`.
- Clipping is scissor-based: `clipBegin()` / `clipBegin(x,y,w,h)` derives scissors from batch transform + camera (`Scene#calculateScissors`) and pushes onto `ScissorStack`. A `true` return **must** be paired with `clipEnd()`; `false` means the area is under 1px - draw nothing and do not call `clipEnd()`. Nesting works because it is a stack. Javadoc constraint: no rotational components, axis-aligned only.
- Separate mechanism: `Group#setCullingArea(Rect)` + the per-element `cullable` flag skip both drawing and hit testing for children outside the rect (valid only unrotated/unscaled).
- Z-order is the child index (`getZIndex()` returns it): drawn low-to-high, hit-tested high-to-low; `toFront`/`toBack`/`setZIndex` behave as in libGDX.

## visible / touchable

- Public fields, no setters: `visible`, `touchable` (enum `Touchable`: `enabled`, `disabled`, `childrenOnly` - same three values libGDX has).
- Hit rules (`Element#hit`, `Group#hit`): a leaf is hit only when `touchable == enabled`; a group excludes itself only when `disabled`, so a `childrenOnly` group passes hits to children but never matches itself. A group's `hit()` skips `!child.visible` children - the leaf's own `hit()` never checks `visible`.
- Dynamic versions: `visible(Boolp)` and `touchable(Prov<Touchable>)` store suppliers (that field is misspelled `touchablility`). Polled during `act`: `touchablility` inside `Element#act`, `visibility` via `updateVisibility()` called by the *parent's* `Group#act` - so a supplier on the scene root is never polled.
- Invisible children do not tick: `Group#act` runs `updateVisibility()` then acts only when `visible`, so their [actions](Actions.md) freeze.

## Scene linkage and the update loop

- Link is a private field literally named `Scene stage`, exposed as `getScene()`; `Group#setScene` recurses into children (its comment: a cycle means StackOverflowError). `parent` is a public field; the ambient current scene is `Core.scene`.
- `addChild` detaches from the old parent first, sets `parent`, propagates `setScene(getScene())`, then `childrenChanged()`; `removeChild(actor, unfocus)` nulls parent/scene, unfocuses via `Scene#unfocus`, then `childrenChanged()`.
- Update order: `Scene#act(delta)` sizes `root` to the viewport minus margins, fires hover enter/exit, prunes keyboard/scroll focus, then `root.act(delta)`; `Group#act` = `super.act` (ticks actions, evaluates `touchablility`, runs the `update(Runnable)` hook) then each child.
- `Scene#draw()` = `Draw.proj(camera)`, `root.draw()`, `Draw.flush()` - no act or layout work there.

## Arc-only conveniences on Element

Listener shortcuts with no libGDX counterpart: `clicked(...)`, `changed(...)`, `hovered(...)`, `exited(...)`, `tapped(...)`, `released(...)`, `dragged(Floatc2)`, `scrolled(Floatc)`, `keyDown(KeyCode, Runnable)`, `fireClick()`, `update(Runnable)`, `hasMouse()/hasKeyboard()/hasScroll()`, `requestKeyboard()/requestScroll()`, plus `fill(...)` on `Group`. Cell layout: [Table](Table.md); scene focus and hit entry points: [Scene](Scene.md).
