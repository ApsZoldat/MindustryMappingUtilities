<!-- verified: v9 @ 2026-10-09, sources: scene/ui/layout/Table.java, scene/ui/layout/Cell.java, scene/ui/layout/WidgetGroup.java, scene/ui/layout/Stack.java, scene/ui/layout/Collapser.java, scene/ui/layout/Spacer.java, scene/ui/layout/WrapTable.java, scene/ui/layout/Scl.java, util/Align.java -->

# Table

Arc's grid layout container, a libGDX `Table` fork (Actor renamed to `Element` throughout). Deltas
below were checked against libgdx/libgdx master `scenes/scene2d/ui/Table.java` + `Cell.java`, fetched 2026-10-09.

## Cell model

- Cells: a `Seq<Cell>` in add order; position derived at add time (next cell at `last.column + last.colspan`, column 0 after a row end). `getCell` is a linear scan; elementless `add()` still occupies a slot.
- `add(T)` returns the new `Cell<T>`; `add(Element...)` returns **void** (libGDX returns the table for chaining).
- Cells come from a **static** pool shared by every table; `clearChildren()` frees them and `Cell#reset()` re-applies static `Cell.defaults()`. libGDX pools too - but never keep a `Cell` reference past `clearChildren()`, it may already be another table's cell.
- `row()` returns **Table** (libGDX returns the row-defaults `Cell`): Arc has **no per-row or per-column defaults**. The `rowDefaults` cell that `row()` maintains is never read by `add()` (only obtain/free sites exist), and there is no `columnDefaults(int)`. The one layer is `defaults()`, copied wholesale by `Cell#set` - all fields overwrite, no per-field merge like libGDX's `Cell#merge`.
- Missing the final `row()` is harmless (`implicitEndRow` in `computeSize()`, reverted on the next `add()`); a repeated `row()` inflates `getRows()` (libGDX early-returns), layout unaffected.

## Cell properties

- **No cell-level preferred size**: `Cell` has only min/max floats; `prefWidth()` is the element's own pref clamped into [min, max] (`max <= 0` uncapped). The `size/width/height` javadocs claiming to set "prefWidth" are stale libGDX text - they set min=max only. No `prefSize()`, no `Value`/`Fixed` overloads anywhere: sizes are plain floats.
- **No `space()`** (libGDX cell spacing does not exist here): use `pad` or `spacer(...)`.
- **All size/pad/margin setters run through `Scl.scl`** (layout/Scl.java): value * global UI scale (desktop `product`, default 1; mobile density) - see the [coordinates guide](Coordinates.md) before mixing these with raw pixels.
- **Table inset is `margin()`, not `pad()`**: `margin(...)`, `marginTop(...)`, `getMarginTop()`. Unset falls back to the background drawable's insets ([Drawable](Drawable.md)), else 0. Cell padding stays `pad`; `Cell#margin(...)` is unrelated: it forwards to the element and silently no-ops unless that element is a Table.
- **expand vs fill**: `grow()` = expand+fill, as in libGDX. expand picks which rows/cols absorb space past pref (`expand(x,y)` gives weights); fill then stretches the element inside its cell, clamped by min/max. Without fill the element keeps its pref size, placed per `align`.
- Conflicting expand in one row/col: **first non-zero wins**; a colspan cell claims its spanned columns only if none of them already expand, otherwise its expand is ignored.
- `align` 0 (the default) renders centered on both axes; `top()`/`left()` clear the opposite bit, `align(int)` replaces all bits.

## Builder DSL (Arc-only; libGDX's Table has none of it)

Constructors `Table(Cons<Table>)` and `Table(Drawable, Cons<Table>)` besides plain/background. Table side: `table()` (5 overloads: plain, background, cons, bg+cons, bg+align+cons), `wrapTable(Cons)`, `stack(Element...)`, `pane(...)` (4), `collapser(...)` (4), `label`, `labelWrap`, `add(CharSequence...)`, `image`/`imageDraw`, `button` (text, icon, image+text, `buttonRow`, `buttonCenter`), `check`, `field`, `area`, `slider`, `spacer`/`spacerX`/`spacerY(Floatp)`, `fill()` (nested fill-parent table), `rect(DrawRect)` - all return `Cell<T>` for chaining. Callbacks are `arc.func` types (`Cons`, `Prov`, `Boolp`, `Floatc`), not `java.util.function`.
Cell sugar libGDX lacks: `with(Cons<T>)` (configure the element), `self`, `get()`, `update`, `disabled`, `checked`, `tooltip`, `visible`, `name`, `color`, `fontScale`, `labelAlign`, `wrap`, `ellipsis`, `row()`. Gotcha: `style(...)` stores a **copy** - later edits to your style object never reach the widget.

## Invalidation

See [Element](Element.md) for the dirty model. Table adds `sizeInvalid`: `invalidate()` sets it plus `needsLayout`; `validate()` (called from `draw()`) runs `layout()`, which calls `computeSize()` only when `sizeInvalid`, then validates children. `add()` invalidates through `childrenChanged()`; `row()` calls `invalidate()`; `setBackground()` diffs old vs new insets.
**Property changes after layout do not invalidate themselves**: `Cell` setters, `Table#margin()` and `Table#align()` only set `sizeInvalid` (or nothing) - call `table.invalidate()` manually.

## Stack / Collapser / Spacer / WrapTable

- `Stack` - overlay: all children sized to the stack, pref/min = max of children; starts at 150x150 actual size, `Touchable.childrenOnly`.
- `Collapser` - wraps exactly one table (more children throw), animates its visible height via the [action](Actions.md) system; `setCollapsed(Boolp)` is polled every `act`, so the shown-state lives outside. Collapsed: pref height 0, min 0 unless `setEnforceMinSize`. Prefer `table.collapser(cons, shown)`, which wires both.
- `Spacer(Floatp, Floatp)` - element whose pref size is re-evaluated each frame (invalidates on change); for dynamic gaps. Static gap = `pad`.
- `WrapTable` via `wrapTable(Cons)` - flow layout: cells wrap to the next line past the available width; own `computeSize`/`layout`, so expand, colspan and row ends are ignored.

Dialog recipes: [UI guide](UI.md).
