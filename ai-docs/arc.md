# Arc Gotchas

Non-obvious Arc behaviors that the compiler will not catch.
Source: Arc embedded in the Mindustry v9 repo. Paths are relative to `arc-core/src/arc/`; line numbers intentionally omitted.

Status tags:
- untagged: taken from a source dump, plausible, not re-checked by hand.
- `[verify]`: surprising or high-impact claim; check the source before relying on it.

Section 0 is the short must-read. Sections 1–5 are on-demand: load only what the task touches.

---

## 0. Must-read core

- No `java.util.function`, `java.awt`, `java.util.Objects`, or streams. Use `arc.func`, `arc.struct`.
- Prefer `Seq`, `ObjectMap`, `IntSeq`, `IntMap` over `List`, `HashMap`, `HashSet`, `List<Integer>`.
- **`ObjectMap`/`ObjectSet` throw on null keys** (`IllegalArgumentException`).
- **Collection iterators are reused.** Nested iteration of the same collection can throw (`ObjectMap`: "#iterator() cannot be used nested.") or clog (`Seq`). A `break` mid-iteration on `Seq` leaves its iterator in use. Use index loops for nested/early-exit cases.
- **`Entries.next()` returns the same `Entry` object every call.** Never store entries; copy key/value.
- **Unordered `Seq`**: `remove(index)` swaps in the last element (order changes). `insert` does not shift.
- `Log.format` / `Strings.format` use **`@`** as the placeholder, not `{}` or `%s`.
- `Time.runTask(delay, ...)` takes **ticks**, not seconds.
- `Button.setChecked()` is **silent** unless `programmaticChangeEvents` is true.
- `Cell.defaults()` is a global shared object; mutating it affects every table. `[verify]`
- `ClickListener.clicked` is a **static** field; assigning it affects all listeners. `[verify]`
- Overriding `Element.draw()` without `super.draw()` skips validation/layout.
- Never allocate in the main loop: use `Pools`, `Tmp`, or reused statics.
- Log messages carry the `[MU] ` prefix where applicable.

---

## 1. Collections

- `OrderedMap.put` on an existing key keeps its position; `remove` + `put` moves it to the end.
- `ObjectMap.get(key, Prov)` inserts the default if missing: a "read" can mutate/resize.
- `ObjectMap.get(key, default)` returns `null` for a present key with a null value.
- Map iteration order is hash order unless you use `OrderedMap`.
- `ObjectMap.remove()` during iteration may skip an element (backward-shift deletion). `[verify]`
- `IntMap`: key `0` is stored separately; its iterator uses a sentinel for it.
- `Seq.get/set` only check `index >= size`; negative indices throw a raw `ArrayIndexOutOfBoundsException`.
- `Seq.iterator().remove()` removes the previously returned element position (decrements first). `[verify]`
- Unordered `Seq` has identity `hashCode()` and `equals()` is false even for same contents.
- `Seq.sort()` uses a thread-local sorter: do not sort one `Seq` from two threads.
- `EnumSet` is a `long` bitmask: more than 64 constants corrupts silently.
- `DelayedRemovalSeq` allows only `remove`, `removeRange`, `clear`, `add` between `begin()` and `end()`.

## 2. Scene2D

- `Actor` → `Element`, `Stage` → `Scene`.
- Capture listeners run root→target; bubble listeners target→root.
- `Element.clicked()` / `changed()` do **not** fire if the element is `Disableable` and disabled.
- `Element.update(Runnable)` is a single slot: a second call replaces the first.
- `Element.visible(Boolp)`, `touchable(Prov)`, `Button.setDisabled(Boolp)` re-evaluate every `act()`.
- Cancelling the `ChangeEvent` on a `Button`/`TextField`/`Slider` reverts the change.
- `Group.addChild` removes the element from its previous parent first.
- `Group.hit()` iterates children in reverse (topmost first).
- `Table.collapser()` / `Collapser` accept **exactly one** child; otherwise it throws.
- `Collapser` draws nothing when collapsed (height 0).
- `Label`: text starting with `$` or `@` is resolved through the bundle in `setText`. `[verify]`
- `Label.getMinWidth()` is 0 when wrapping; set explicit widths for wrapped labels.
- `TextField` preferred width is hard-coded to 150.
- `Slider` snaps to step size unless Shift is held.
- `Dialog`: modal dialogs capture all touches, including outside their bounds. `show()`/`hide()` set `setTransform(true)` and `setClip(false)`.
- `Table` rounds child bounds by default (`setRound(false)` to keep fractions).
- `Scl.scl()` is cached; recomputed only when the scale changes.
- `Table.clearChildren()` frees cells back to the cell pool: do not keep `Cell` references across it.
- `Scene.registerStyles()` scans fields starting with `"default"`; `getStyle()` throws if no default exists.
- `DragListener.touchDragged` yields **negative** deltas; `Element.dragged()` yields positive deltas. `[verify]`
- `ElementGestureListener.pan()` deltas are in stage coordinates.
- `GestureDetector` tracks only 2 pointers.

## 3. Graphics

- **Sorting is off by default.** `Draw.sort(true)` enables z-sorting; `Draw.z()` has no effect without it. `[verify]`
- Custom shaders must replicate the `255.0/254.0` alpha scaling of the default shader, or colors drift.
- Shader preprocessing rewrites GLSL ES 2.0 to 3.0 automatically. Do not write `#version`, do not declare `fragColor`; explicit `#version` or `#ifdef GL_ES` throws. `[verify]`
- `FrameBuffer.begin()` twice without `end()` throws. `end()` restores the previous framebuffer and viewport.
- Default `FrameBuffer(w, h)` has no depth buffer.
- `Camera` is y-up with bottom-left origin.
- `Mat` is column-major; `translate/rotate/scale` post-multiply.
- `Mat.setOrtho(x, y, w, h)` takes corner + size, not libGDX `(left, right, bottom, top)`.
- `Color.toFloatBits` packs ABGR.
- `Shader.setUniformMatrix(int, Mat)` reportedly calls the 3×3 uniform setter; use `setUniformMatrix4` for 4×4. `[verify]`
- `TextureRegion.split` is indexed `[column][row]` despite its javadoc. `[verify]`
- `Gl` caches state; use `Gl.reset()` if external code touched GL state.

## 4. Files, assets, audio

- `FileType.internal` on desktop silently falls back to the classpath, which can mask missing files.
- `FileType.classpath` is read-only.
- `AssetManager` runs on a single thread; one failing load clears the whole pending task stack.
- `Audio.newSound()`/`newMusic()` return a dummy instance on failure instead of throwing; check `valid()`.
- `Sound.play()` returns **-1** on failure (0 is a valid handle).
- `Sound.play()` with `checkFrame` intensifies the existing voice instead of starting a new one when called within `minInterval`.

## 5. Events, input, math, utils

- **`Events.fire(T)` dispatches by exact class**: a listener on a superclass will not receive subclass events.
- `Events.remove` needs the same `Cons` reference; an inline lambda cannot be removed later.
- `Events.run(Object, Runnable)` ignores the event payload.
- `KeyBind` constructor calls `load()`, which no-ops if `settings` is null.
- `KeyboardDevice.scrolled` stores `-amountY`.
- `Mathf.mod` returns `[0, n)` for negatives (unlike `%`).
- `Mathf.sign(0)` returns 1. `[verify]`
- `Mathf.sin/cos` use a lookup table: approximate.
- `Mathf.lerpDelta` multiplies by `Time.delta`: a factor of 1 does not guarantee reaching the target in one frame.
- `Vec2` mutators return `this`; call `cpy()` before mutating shared vectors.
- `Rect.tmp`, `Intersector` statics, and `Strings.fixedBuilder` buffers are shared globals: not reentrant, not thread-safe.
- `Strings.parseInt` returns `Integer.MIN_VALUE` on failure; use `canParseInt`. `[verify]`
- `Strings.join` on empty input throws `StringIndexOutOfBoundsException`. `[verify]`
- `Reflect.get(Object, String)` uses `getDeclaredField`: inherited fields are not found.
- `Timer.Task` cannot be scheduled twice without `cancel()`.
- `Pool` does not detect double-free; freeing twice corrupts it.
- `Http` result getters are single-shot; `getResultAsStream()` closes after the callback returns.
- Angle conventions (`Mathf.angle`, `Vec2.angle`, `Mathf.atan2` argument order) were **dropped**: the source dump claimed non-standard behavior that contradicts Mindustry's usual convention (0° = right). Check the source before using these.