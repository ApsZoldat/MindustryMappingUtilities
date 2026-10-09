<!-- verified: v9 @ 2026-10-09, sources: scene/Scene.java, scene/Element.java, scene/ui/Dialog.java, scene/ui/layout/Table.java, scene/ui/layout/Cell.java, scene/ui/Button.java; mindustry: core/src/mindustry/ui/Styles.java, annotations/src/main/java/mindustry/annotations/impl/AssetsProcess.java -->

# Building or modifying UI

Read-before-starting guide for creating Arc UI or changing UI that already exists.
Background: [Element](Element.md), [Table](Table.md), [Scene](Scene.md),
[Events](Events.md), [Drawable](Drawable.md), [Actions](Actions.md),
[coordinates](Coordinates.md).

## Where UI lives

- One scene: `Core.scene` (`scene/Scene.java`). It owns a single `root` group; `scene.add(e)`
  puts an element in it, and `scene.table(cons)` adds a table with `setFillParent(true)` and
  hands you a `Cons` to fill it. There is no Stage - the field on `Element` that libGDX would
  call `stage` holds a `Scene`.
- Windows are `Dialog`s. `show()` targets `Core.scene`, packs, centers, and runs the default
  show action; `isShown()` literally means `getScene() != null`.

## Dialog anatomy

libGDX's `contentTable`/`buttonTable` are now the public finals `cont` and `buttons` (plus
`titleTable` and a `Label title`) - all dialog layout touches these. Defaults from
`Dialog(String)`: modal, centered, not movable/resizable, `resizeBorder = 8`.

```java
Dialog dialog = new Dialog("Exporter");
dialog.cont.defaults().pad(8f);        // defaults(): defaults for all cells (javadoc)
dialog.cont.button("Close", dialog::hide);
dialog.cont.button("Export", this::export);
dialog.shown(() -> { /* VisibilityListener under the hood */ });
dialog.resized(() -> { /* window resize hook */ });
dialog.show();
```

- `show()` saves keyboard/scroll focus and points both at the dialog; `hide()` restores them
  and removes the dialog with `Actions.remove()` - see [actions](Actions.md).
- Helpers: `addCloseButton()`, `closeOnBack(...)`, `toggle()`, `setMovable/setModal/
  setResizable`, `setShowAction/setHideAction`.
- Default widget styles resolve through the scene registry: `Dialog(String)` does
  `scene.getStyle(DialogStyle.class)`, which throws if unregistered. Mindustry fills it from
  `mindustry.ui.Styles`: the `@StyleDefaults` processor generates `loadStyles()` emitting
  `Core.scene.addStyle(type, Styles.x)` for every static `default*` field (Arc's reflective
  `Scene#registerStyles(Class)` is never called). Register your own the same way.

## Laying out with the Table DSL

Everything is a Table (`scene/ui/layout/Table.java`). Helpers add the child and return its
`Cell` in one call: `table`, `pane`, `stack`, `collapser`, `button`, `check`, `slider`,
`field`, `area`, `label`, `labelWrap`, `image`, `spacerX/spacerY`. Chain on the cell
(`scene/ui/layout/Cell.java`): `size/w/h`, `pad`, `minSize/maxSize`, `expand/fill/grow`,
`align/top/...`, `uniform`, `colspan`, `wrap`, `fontScale`, `color`, `disabled`, `visible`,
`tooltip`, `name`.

- `cell.name("my-widget")` makes the element findable (below); `cell.update(...)` wires the
  element's per-act `update` callback - these Cell conveniences are Arc-only vs libGDX.
- `margin(...)` on the table is its *outer* margin and is stored through `Scl.scl()` - author
  it in unscaled units; per-cell padding semantics are in [Table](Table.md).

## Wiring input

Element-level sugar on `Element` (`scene/Element.java`), built on [events](Events.md):

- `clicked(Runnable)` - a `ClickListener`, skipped while a `Disableable` is disabled;
  `clicked(Cons<ClickListener>, ...)` tunes the listener (mouse button, etc).
- `changed(Runnable)` - a `ChangeEvent` listener, **not** a click: it runs when `change()`
  fires (checkbox/toggle state). For toggle buttons: `setChecked(...)`,
  `setProgrammaticChangeEvents(true)`.
- `tapped` (fires on touchDown and stops the event), `hovered`/`exited`, `released`.
- `update(Runnable)` - runs on every `Scene#act`, outside the action system.
- Text input needs focus: `element.requestKeyboard()`; query with `hasKeyboard()`.

## Finding and modifying existing UI

```java
Element e = Core.scene.find("my-widget");       // by name, from root (root.find)
Element any = Core.scene.find(el -> el instanceof TextField);
```

- `findVisible(name)` skips invisible subtrees. `Table#getCell(element)` returns the cell to
  retune (`.size()`, `.pad()`...); `clearChildren()` removes children *and* their cells,
  `reset()` additionally resets margins, alignment, and cell defaults.
- Vanilla Mindustry dialogs are named and assembled Mindustry-side - to patch those, see
  [the Mindustry index](../mindustry-docs/MindustryIndex.md).

## Animation and the frame loop

- Actions can wake demand rendering: `Element#act` calls `Core.graphics.requestRendering()`
  while actions run (gated by `Scene` actions-request-rendering). An `update(Runnable)`
  callback does **not** - request rendering yourself or nothing redraws.
- Layout only reruns after invalidation and the `validate()` that starts `draw()` - see
  [Element](Element.md).

## Gotchas

- Don't assign `width`/`height` directly - the source comment says "DO NOT modify without
  calling sizeChanged".
- Listener lists are `DelayedRemovalSeq`, so removing listeners/actions while events are
  being dispatched is safe.
- libGDX naming survives in odd places: `screenToStageCoordinates`, `keepWithinStage`,
  `Element#stage`, javadocs saying "stage" for a `Scene`.
