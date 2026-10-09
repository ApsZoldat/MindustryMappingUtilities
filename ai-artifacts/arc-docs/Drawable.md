<!-- verified: v9 @ 2026-10-09, sources: scene/style/Drawable.java, scene/style/BaseDrawable.java, scene/style/Style.java, scene/style/NinePatchDrawable.java, scene/style/ScaledNinePatchDrawable.java, scene/style/TiledDrawable.java, scene/style/TextureRegionDrawable.java, scene/style/StackDrawable.java, scene/style/TransformDrawable.java, scene/ui/Button.java, scene/ui/Image.java, scene/ui/layout/Table.java, graphics/g2d/TextureAtlas.java, graphics/g2d/NinePatch.java, Core.java; libGDX master: scene2d/utils/Drawable.java, scene2d/utils/TransformDrawable.java, scene2d/ui/Button.java -->

# Drawable

`arc.scene.style.Drawable` is libGDX's interface minus the `Batch`: both `draw(...)`
overloads live on the interface itself and render through the global batch (`Core.batch`,
via statics in `arc.graphics.g2d.Draw`). Current libGDX declares only the plain
`draw(Batch, ...)` on `Drawable` and puts the 9-arg transform overload on
`TransformDrawable`, so ported overrides must drop the `batch` argument. Arc-only member:
`default float imageSize()` (returns `getMinWidth()`; `TextureRegionDrawable` overrides it
to the raw region width, and `Table.button(...)` helpers use it to size an icon). libGDX's
default `setPadding`/`setMinSize` helpers do not exist here. Border sizes keep the libGDX
names: `get/setLeftWidth`, `RightWidth`, `TopHeight`, `BottomHeight`, plus
`get/setMinWidth`, `MinHeight`. There are no outline/padding/corner members.

`BaseDrawable` stores those sizes and draws nothing; each concrete drawable adds a
copy-constructor `X(XDrawable)` plus a no-arg constructor leaving it uninitialized (set the
region/patch before use). `Style` (scene/style/Style.java) is an empty abstract class: the
common base every `*Style` extends. libGDX styles have no such base class.

## Concrete drawables (scene/style/)

- `TextureRegionDrawable` - stretches the region, centered on `x + width/2`. Multiplies its
  `tint` by the current `Draw` color, so actor tinting works. Min size is
  `Scl.scl(scale * region.width)`, i.e. already in UI-scale units.
- `NinePatchDrawable` - wraps a `NinePatch`, implements `TransformDrawable`. `setPatch`
  copies the patch pads into the border sizes and sets min size to the patch's total size,
  so layout normally never shrinks it below natural size; lower `setMinWidth` yourself if
  you want that (its javadoc suggests left+right as the floor). `tint(Color)` and
  `tint(top, bottom)` return tinted copies.
- `ScaledNinePatchDrawable` - a `NinePatchDrawable` whose borders and min sizes are
  multiplied by `Scl.scl(multiplier)`; `draw` lays the patch into `width / scale` then
  scales vertices back up, so corners render at patch-size x UI scale instead of raw
  pixels. Gotcha: `getLeftWidth()` and friends are overridden to read
  `patch.getPadLeft() * scale`, so `setLeftWidth` is silently ignored on reads.
- `TiledDrawable` - repeats the region instead of stretching. Its 9-arg `draw` throws
  `UnsupportedOperationException`, yet it still passes `instanceof TransformDrawable`
  (inherited), so on an `Image` that is rotated or scaled (`Image.draw` checks exactly
  that) it throws. It sets `Draw.color` to its own color only, ignoring the actor tint
  `Table.drawBackground` just applied. Partial edge tiles crop UVs by
  `remaining / texture.width`, which lines up only while tile units == region pixels:
  `setTileSize` to anything else misaligns the last row/column.
- `StackDrawable` - draws `first` then `second` over identical bounds. `get*` sizing reads
  `first` only, min size is the max of both, setters fan out to both. In the transformed
  draw, children that are not `TransformDrawable` get the plain `draw` instead - the
  transform is silently dropped for them.
- `TransformDrawable` - not Arc-only; libGDX has it too, Arc's just re-declares the
  transformed draw without `Batch`. Consumers (`Image.draw`, `StackDrawable`) dispatch with
  `instanceof`, so which drawable honors rotation/scale is decided there, not in `Drawable`.

## Obtaining one

- `Core.atlas.drawable(name)` (typed: `Core.atlas.getDrawable(name)`) - cached; a region
  with `splits` becomes a `ScaledNinePatchDrawable` (atlas `pads` applied), anything else a
  `TextureRegionDrawable`. Unknown name throws unless an error region is set, in which case
  you get a drawable for it. `setDrawableScale` only affects drawables created afterwards.
- `Core.atlas.find(name)` - the `AtlasRegion` itself; throws `IllegalArgumentException` for
  unknown names when no error region exists.
- By hand: `new TextureRegionDrawable(region)`, `new TextureRegionDrawable(region, scale)`,
  `new NinePatchDrawable(patch)`, `new ScaledNinePatchDrawable(patch, multiplier)`,
  `new TiledDrawable(region)`.

## How styles consume them

`Button.ButtonStyle` is representative: `extends Style`, plain public fields
(`up, down, over, checked, checkedOver, disabled` plus press/check offsets) and a copy
constructor; libGDX's additionally has `focused`, `checkedDown`, `checkedFocused`, which
Arc lacks. The widget keeps the style in `setStyle`/`getStyle`, picks a field by state and
applies it through `Table.setBackground`; `Table.drawBackground` then calls the plain
`draw(x, y, width, height)`. `getStyle()` javadoc warns that mutating the style may have no
effect until `setStyle` is called again. The background's border sizes become the table's
default margins when no margin was set (`Table.getMarginTop` and friends), and its min size
floors the *preferred* size (`Table.getPrefWidth`/`getPrefHeight`, not `getMinWidth`).

Style construction and sharing: [ui](UI.md). Layout and `Scl.scl`:
[Table](Table.md), [coordinates](Coordinates.md). Scene-level drawing
context: [Scene](Scene.md).
