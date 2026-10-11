# Arc Gotchas Reference

Non-obvious behaviors, libGDX differences, and footguns in Arc (Mindustry's framework).
Source: `/root/projects/Mindustry/arc/arc-core/src/arc/`. Grouped by subsystem.
Cite this file instead of re-deriving from source.

---

## 1. Application / Lifecycle / Net

### Lifecycle
- `exit()` is a graceful-exit hook; never called after a crash. `dispose()` is always preceded by `pause()`. (`arc/ApplicationListener.java`)
- `ApplicationCore` uses `setup()` (not `create()`), then iterates modules from a raw `ApplicationListener[]` array. (`arc/ApplicationCore.java`)
- `Core` is a static holder, not a lifecycle: access `Core.app`, `Core.graphics`, `Core.scene`, etc. directly. (`arc/Core.java`)
- No `DesktopLauncher`/`NetApplication`. Platform detection via `Application.getType()` / `ApplicationType` enum (`android`, `desktop`, `headless`, `web`, `iOS`) and helpers `isDesktop()`, `isHeadless()`, `isMobile()`, `isWeb()`. (`arc/Application.java`)
- `removeListener` is deferred to the main loop; not immediate. (`arc/Application.java`)
- `isOnMainThread()` returns `true` if the main thread is uninitialized — false positives before app start. (`arc/Application.java`)
- `Application.defaultUpdate()` is NOT automatic — backends must call it before their own `update()`; it handles `settings.autosave()` and `Time.updateGlobal()`. (`arc/Application.java`)

### Net / Server / Client
- `Client.connect()` blocks but requires `update()` on a separate thread, else it times out with "Connected, but timed out during TCP registration." (`arc/net/Client.java`)
- `disconnected()` can fire on any thread; `connected()`/`received()` run on the update thread. (`arc/net/NetListener.java`)
- `sendTCP`/`sendUDP` return `0` on error, never throw — they catch, close, and call `ArcNet.handleError`. Check return values. (`arc/net/Connection.java`)
- `sendTCPBuffer` requires a length prefix; `sendUDPBuffer` sends raw bytes. (`arc/net/Connection.java`)
- `isIdle()` is based on write-buffer fill percentage, not time (default threshold 0.1). (`arc/net/Connection.java`)
- `NetSerializer` is shared across all UDP connections — must be thread-safe. (`arc/net/NetSerializer.java`)
- `Server` uses a single NIO selector thread for all TCP+UDP I/O, not thread-per-connection. (`arc/net/Server.java`)
- `Client extends Connection` — the client *is* a connection (has its own `sendTCP`/`sendUDP`/`addListener`). (`arc/net/Client.java`)

### HTTP
- `getResult()`/`getResultAsString()` are single-shot — they consume the InputStream; calling twice yields empty/null. (`arc/util/Http.java`)
- `getResultAsStream()` disconnects the connection after the callback returns; don't store the stream. (`arc/util/Http.java`)
- GET `content` becomes a query string (`?content`); POST/PUT send it as a body. (`arc/util/Http.java`)
- `submit()` is async; `block()` runs on the calling thread. Default `errorHandler` is `Log::err` (logs, doesn't throw). (`arc/util/Http.java`)

---

## 2. Scene2D

### Renames & basics
- `Actor` → `Element`; `Stage` → `Scene`. (`arc/scene/Element.java`, `arc/scene/Scene.java`)
- `draw()` calls `validate()` first, which triggers `layout()` — overriding `draw()` without `super.draw()` skips layout entirely. (`arc/scene/Element.java:50`)
- `setScene()` propagates recursively to children — StackOverflowError means the group is its own ancestor. (`arc/scene/Group.java:415`)
- `Scene.draw()` skips rendering if `!root.visible`. (`arc/scene/Scene.java:137`)
- `Scene.act()` resets root position/size every frame from margins. (`arc/scene/Scene.java:156-159`)
- `Scene.registerStyles()` scans fields starting with `"default"` — `getStyle()` throws if no default exists. (`arc/scene/Scene.java:75-94`)
- `Scene.cancelTouchFocus()` sends `touchUp` with `Integer.MIN_VALUE` coords — listeners can check `isTouchFocusCancel()`. (`arc/scene/Scene.java:547-549`)
- `Scene.cancelTouchFocus(actor)` does NOT return `TouchFocus` to the pool ("may still be in use"). (`arc/scene/Scene.java:534`)
- `Scene.setKeyboardFocus()` can be cancelled by `FocusListener` (`event.cancelled`). (`arc/scene/Scene.java:673`)
- `Scene.isInsideViewport()`/`stageToScreenCoordinates()` invert Y after projection. (`arc/scene/Scene.java:784,822`)

### Events & listeners
- Capture listeners run root→target; bubble listeners target→root. Capture can `stop()` before children see the event. (`arc/scene/Element.java:108-148`)
- `notify()` auto-adds touch focus on `touchDown` when a listener returns true — no manual touch-focus management needed. (`arc/scene/Element.java:164-169`)
- **`ClickListener.clicked` is a static field** (`public static Runnable clicked`) — setting it affects ALL ClickListeners. (`arc/scene/event/ClickListener.java:19`)
- Static `clicked.run()` fires before the instance `clicked(event, x, y)`. (`arc/scene/event/ClickListener.java:76-77`)
- `InputListener.touchUp`/`touchDragged` always mark the event handled. (`arc/scene/event/InputListener.java:31-35`)
- `ChangeListener.handle()` / `FocusListener.handle()` / `ResizeListener.handle()` always return false — these events are never "handled". (`arc/scene/event/ChangeListener.java:14`, `FocusListener.java:22`, `ResizeListener.java:11`)
- `ClickListener` ignores a second touch while the first is pressed (`if(pressed) return false`) — multi-touch needs explicit pointer tracking. (`arc/scene/event/ClickListener.java:41`)
- `inTapSquare()` (no args) just checks `touchDownX != -1`; only the coordinate version checks actual distance. (`arc/scene/event/ClickListener.java:116-123`)
- `Element.tapped()` calls `event.stop()` after running the runnable. (`arc/scene/Element.java:937`)
- `Element.dragged()` returns positive deltas; `DragListener.touchDragged` returns NEGATIVE deltas (`deltaX -= x`). (`arc/scene/Element.java:206-208`, `arc/scene/event/DragListener.java:37-41`)
- `DragListener` uses `int button`, not `KeyCode`. (`arc/scene/event/DragListener.java:16`)
- `Element.scrolled()` only provides `amountY`; `amountX` is discarded. (`arc/scene/Element.java:235`)
- `ElementGestureListener.pan()` receives deltas in stage coordinates. (`arc/scene/event/ElementGestureListener.java:54-60`)
- `ElementGestureListener.handle()` returns true for touchDown/touchUp/touchDragged. (`arc/scene/event/ElementGestureListener.java:99,107,112`)
- `HandCursorListener.exit()` restores cursor only for pointer -1. (`arc/scene/event/HandCursorListener.java:36`)

### Element/Group conveniences
- `Element.act()` runs the `update` runnable after actions — `update` is a single field, not a list; calling `update(r)` replaces the previous one. (`arc/scene/Element.java:79,993-996`)
- `Element.visible(Boolp)` / `touchable(Prov)` / `Button.setDisabled(Boolp)` set providers re-checked every `act()`. (`arc/scene/Element.java:78-84`, `arc/scene/ui/Button.java:81-83`)
- `Element.clicked()`/`changed()` overloads do NOT fire if the element is `Disableable` and disabled. (`arc/scene/Element.java:916-927,983-991`)
- `Group.hit()` iterates children in reverse order. (`arc/scene/Group.java:195`)
- `Group.addChild()` removes the actor from its previous parent first. (`arc/scene/Group.java:251-260`)
- `Group.removeChild()`/`clearChildren()` unfocus children by default. (`arc/scene/Group.java:328-353`)
- `Group.getChildren()` returns `SnapshotSeq<Element>`. (`arc/scene/Group.java:441`)
- `Group.localToDescendantCoordinates()` throws `IllegalArgumentException("Child is not a descendant")` if passed a non-descendant. (`arc/scene/Group.java:465-473`)

### Actions
- `Actions.translateTo()` sets translation, not position (different from `moveTo`). (`arc/scene/actions/Actions.java:97-101`)
- `TemporalAction.act()` sets its pool to null during execution — actions can't be returned to the pool mid-run. (`arc/scene/actions/TemporalAction.java:31-32`)
- `SequenceAction.act()` returns true immediately if `actor == null`. (`arc/scene/actions/SequenceAction.java:53`)
- `ParallelAction.setActor()` propagates to all child actions. (`arc/scene/actions/ParallelAction.java:88-92`)
- `RepeatAction.delegate()` restarts the wrapped action when it completes. (`arc/scene/actions/RepeatAction.java:20`)
- **`Action.setActor(null)` returns the action to its pool** — detaching an action silently frees it. (`arc/scene/action/Action.java:53-57`)

### Widgets
- `Button.setChecked()` fires `ChangeEvent` only when `programmaticChangeEvents` is true — default is false, so `setChecked()` is silent by default. (`arc/scene/ui/Button.java:123-125`)
- Cancelling `ChangeEvent` on a Button reverts the checked state. (`arc/scene/ui/Button.java:105-107`)
- `Button.draw()` applies pressed offset via `moveBy` on all children — custom draw assuming static positions may glitch. (`arc/scene/ui/Button.java:244-249`)
- `ButtonGroup.canCheck` uses `uncheckLast` to auto-deselect the previous button. (`arc/scene/ui/ButtonGroup.java:77-87`)
- `ButtonGroup.uncheckAll()` temporarily sets `minCheckCount = 0`. (`arc/scene/ui/ButtonGroup.java:95-101`)
- `ButtonGroup.setChecked(String)` finds the first `TextButton` whose text matches. (`arc/scene/ui/ButtonGroup.java:111-120`)
- `Label.getText()` starting with `$` or `@` is looked up in the bundle — happens in `setText`, not the constructor. (`arc/scene/ui/Label.java:113-119`)
- `Label.getMinWidth()` returns 0 when wrapping; `getPrefWidth()` returns 0 when `wrapFit` is true — set explicit widths. (`arc/scene/ui/Label.java:231-238`)
- `Label` mutates font scale temporarily during measurement (`scaleAndComputePrefSize`/`layout`). (`arc/scene/ui/Label.java:128-213`)
- `TextField` pref width is hardcoded to 150. (`arc/scene/ui/TextField.java:758`)
- `TextField.changeText()` reverts text if `ChangeEvent` is cancelled. (`arc/scene/ui/TextField.java:687-695`)
- `TextField.keyDown` bails out if `getKeyboardFocus() != this`. (`arc/scene/ui/TextField.java:971`)
- `TextField.keyTyped` lets ASCII < 32 through only for control chars (TAB/BACKSPACE/DELETE). (`arc/scene/ui/TextField.java:1090-1099`)
- `TextField` sets `hasSelection = true` on touchDown; clears only in touchUp if `selectionStart == cursor`. (`arc/scene/ui/TextField.java:932,944`)
- `Slider.calculatePositionAndValue()` snaps to step size unless Shift is held. (`arc/scene/ui/Slider.java:159-160`)
- `Slider.setValue()` fires a `ChangeEvent` that can be cancelled to revert. (`arc/scene/ui/ProgressBar.java:261-263`)
- `ProgressBar.getVisualValue()` interpolates during animation. (`arc/scene/ui/ProgressBar.java:212-215`)

### Table / Cell / Dialog / layout
- `Cell.defaults()` is a GLOBAL singleton shared across all tables — mutating it affects every table. (`arc/scene/ui/layout/Cell.java:53-75`)
- Table rounds child bounds when `round` is true (default) — `setRound(false)` keeps fractional positions. (`arc/scene/ui/layout/Table.java:887-898`)
- `Cell.setElement()` removes the old element from its parent. (`arc/scene/ui/layout/Cell.java:84`)
- `Table.collapser()` requires exactly one child — `childrenChanged()` throws otherwise ("Only one actor can be added to CollapsibleWidget"). (`arc/scene/ui/layout/Collapser.java:173-175`)
- `Collapser.draw()` clips to `currentHeight` — collapsed (height 0) draws nothing. (`arc/scene/ui/layout/Collapser.java:99-107`)
- `Stack` constructor defaults width/height to 150. (`arc/scene/ui/layout/Stack.java:48-50`)
- `Scl.scl()` is cached — only recomputed when `setProduct`/`setAddition` is called. (`arc/scene/ui/layout/Scl.java:26-37`)
- `WidgetGroup.setFillParent` does NOT trigger `invalidateHierarchy` (unlike `Element.setFillParent`). (`arc/scene/ui/layout/WidgetGroup.java:127-129`)
- `Dialog.show()`/`hide()` set `setTransform(true)` + `setClip(false)`. (`arc/scene/ui/Dialog.java:478-480`)
- `Dialog.draw()` forces keyboard focus if none is set (finds topmost dialog). (`arc/scene/ui/Dialog.java:292-303`)
- `Dialog.hit()` returns itself for modal dialogs even outside bounds — modals capture all touches. (`arc/scene/ui/Dialog.java:321-325`)
- `Dialog.hide()` adds `ignoreTouchDown` as a capture listener removed via action. (`arc/scene/ui/Dialog.java:543`)
- `Cell.style()` copies the style to prevent mutation. (`arc/scene/ui/layout/Cell.java:377-388`)
- `Table.clearChildren()` frees all cells back to `cellPool`. (`arc/scene/ui/layout/Table.java:625`)

---

## 3. Graphics / GL

### Batch / SpriteBatch
- SpriteBatch vertex layout is `xy + uv + depth + color + mix_color` (7 floats/vertex), not libGDX's 5. (`arc/graphics/g2d/SpriteBatch.java`)
- `flush()` forces `Gl.depthMask(false)` and enables blending — sprites never write depth during a flush. (`arc/graphics/g2d/SpriteBatch.java`)
- `setShader` throws while sorting unless the draw is wrapped in `Draw.draw(z, Runnable)`. (`arc/graphics/g2d/SpriteBatch.java`)
- **Sorting is OFF by default** — `Draw.sort(true)` enables z-sorting; `Draw.z(float)` is a no-op on most batches. (`arc/graphics/g2d/Draw.java`)
- `Batch.setBlending` flushes on change, but `SpriteBatch.setBlending` defers (applied per-request at flush). (`arc/graphics/g2d/Batch.java`, `arc/graphics/g2d/SpriteBatch.java`)
- Default shader scales alpha by `255.0/254.0` in both vertex and fragment shaders — custom shaders must replicate this or colors drift. (`arc/graphics/g2d/SpriteBatch.java`)
- `Draw.blit` bypasses the batch entirely — renders a `ScreenQuad` directly with the given shader. (`arc/graphics/g2d/Draw.java`, `arc/graphics/g2d/ScreenQuad.java`)
- SpriteCache layout is `xy + uv + depth + color` (6 floats, no mix color); `add(Texture, float[], ...)` expects 5 floats/vertex; default shader uses `u_projectionViewMatrix` (not `u_projTrans`). (`arc/graphics/g2d/SpriteCache.java`)

### GL abstraction
- `Gl` is one unified class (no GL20/GL30 split) with **state caching** — redundant `bindTexture`/`useProgram`/`enable`/`blendFuncSeparate`/`depthMask`/`activeTexture` calls are skipped; `deleteTexture` clears cached bindings; `Gl.reset()` clears all caches. (`arc/graphics/gl/Gl.java`)

### Shader
- Shader preprocessor rewrites GLSL ES 2.0 → 3.0: `varying`→`in`/`out`, `attribute`→`in`, `texture2D(`→`texture(`, `gl_FragColor`→`fragColor`, injects `#version 300 es` (or `150`/`130` desktop). Explicit `#version` or `#ifdef GL_ES` in source throws. (`arc/graphics/Shader.java`)
- Fragment shaders get `out lowp vec4 fragColor;` auto-added — don't declare your own. (`arc/graphics/Shader.java`)
- `Shader.setUniformMatrix(int, Mat)` calls `Gl.uniformMatrix3fv` — a copy-paste bug; use `setUniformMatrix4` for 4×4. (`arc/graphics/Shader.java`)
- `Shader.copyTransform` is a shared static `float[16]` — not thread-safe; it transposes M01/M10 during copy. (`arc/graphics/Shader.java`)

### FrameBuffer / Texture / Atlas
- `begin()` flushes the batch and sets the viewport; `end()` restores the previous framebuffer AND viewport. Calling `begin()` twice throws (`bufferNesting`). (`arc/graphics/FrameBuffer.java`)
- `begin(Color)` clears depth/stencil only if those attachments were explicitly added — default `FrameBuffer(w,h)` has no depth buffer. (`arc/graphics/FrameBuffer.java`)
- `resize()` re-initializes (disposing old textures) but preserves filter/wrap from the previous init. (`arc/graphics/FrameBuffer.java`)
- `TextureAtlas` uses a `TextureArray` + `ArraySliceTexture` per page — all pages live in one GPU texture array, not separate `Texture` objects. (`arc/graphics/g2d/TextureArray`, `arc/graphics/ArraySliceTexture.java`)
- `ArraySliceTexture.getDepth()` returns the layer index (z in array shaders); `dispose()` is a no-op — dispose the whole `TextureArray`. (`arc/graphics/ArraySliceTexture.java`)
- `TextureRegion.split` returns a `[column][row]`-indexed array despite javadoc claiming `[row][column]`. (`arc/graphics/g2d/TextureRegion.java`)
- `TextureRegion.set(u,v,u2,v2)` nudges 1×1 regions toward pixel center (AMD filtering fix). (`arc/graphics/g2d/TextureRegion.java`)

### Camera / viewport / blending / misc
- `Camera` is **y-up** — ortho projection with bottom-left origin; `unproject`/`project` assume y-up screen coords (matching GL, unlike most UI toolkits). (`arc/graphics/Camera.java`)
- `HdpiUtils.glScissor`/`glViewport` auto-convert logical → backbuffer coords on HDPI (default mode `logical`); use `HdpiUtils.setMode(HdpiMode.Pixels)` to bypass. (`arc/graphics/HdpiUtils.java`)
- `ScissorStack.push` intersects with the parent scissor and returns `false` if the area is empty (nothing should draw). (`arc/graphics/g2d/ScissorStack.java`)
- `Blending.normal` uses `(srcAlpha, oneMinusSrcAlpha)` for RGB but `(one, oneMinusSrcAlpha)` for alpha — alpha accumulates differently than stock libGDX. (`arc/graphics/Blending.java`)
- `Mat.setOrtho(x, y, w, h)` takes corner + size, not libGDX's `(left, right, bottom, top)`. (`arc/math/Mat.java`)
- `Color.toFloatBits` packs ABGR (alpha in the high byte); `Color(r,g,b)` defaults alpha to 1. (`arc/graphics/Color.java`)
- `Pixmap` requires direct/native ByteBuffers; `PixmapIO.writePng` sets `flipY=false` but `PngWriter` defaults to `flipY=true`. (`arc/graphics/Pixmap.java`, `arc/graphics/PixmapIO.java`)

### FX
- `FxProcessor` requires strict `begin()` → `end()` → `applyEffects()` → `render()` ordering — `clear()` throws while capturing/applying. (`arc/fx/FxProcessor.java`)
- `FxFilter.render()` uses `Draw.blit` (ScreenQuad), not a batch. (`arc/fx/FxFilter.java`)

---

## 4. Collections (arc/struct)

### Seq / arrays
- Unordered `Seq.remove(index)` swaps the last element into the removed slot — order changes silently. (`arc/struct/Seq.java:694-697`)
- Unordered `Seq.removeRange` copies from the END backward — surviving tail elements end up reversed. (`arc/struct/Seq.java:710-714`)
- Unordered `Seq.insert` writes to `size` without shifting — element lands at the end logically, not at `index`. (`arc/struct/Seq.java:529-530`)
- `Seq.iterator()` reuses the same instance (max 2 nested before allocating a 3rd); `break` mid-iteration clogs it. (`arc/struct/Seq.java:1115-1119`)
- `Seq.iterator().remove()` decrements `index` then removes — it removes the *previous* element, not the one just returned. (`arc/struct/Seq.java:1176-1180`)
- Unordered `Seq.hashCode()` returns identity and `equals()` returns false — two unordered Seqs with identical contents are never equal. (`arc/struct/Seq.java:1048,1062`)
- `Seq.get/set(index,...)` only check `index >= size`, not `index < 0` — negatives throw raw AIOOBE. (`arc/struct/Seq.java:513-520`)
- `Seq.sort()` uses a `ThreadLocal<Sort>` — the same Seq sorted from two threads is not safe. (`arc/struct/Seq.java:868-877`)

### ObjectMap / ObjectSet
- **Null keys throw `IllegalArgumentException`** in `ObjectMap`/`ObjectSet` (unlike `HashMap`/`HashSet`). (`arc/struct/ObjectMap.java:132`, `arc/struct/ObjectSet.java:162`)
- `get(key, defaultValue)` returns null for a present key with a null value — can't distinguish "missing" from "present-null" without `containsKey`. (`arc/struct/ObjectMap.java:234-237`)
- Map iterators are reused — nested iteration throws `ArcRuntimeException("#iterator() cannot be used nested.")`. (`arc/struct/ObjectMap.java:470-485`)
- `Entries.next()` returns the SAME `Entry` instance every call — storing entries and reading later gives the last entry's data. (`arc/struct/ObjectMap.java:601-611`)
- `remove()` during iteration uses backward-shift deletion and decrements `nextIndex` if an element shifted into the removed slot — skipping it. (`arc/struct/ObjectMap.java:570-591`)
- `clear()` early-returns when `size == 0` without shrinking — a map that held 1M entries keeps that capacity. (`arc/struct/ObjectMap.java:302-307`)
- Iteration order is hash-table order, not insertion order — use `OrderedMap`. (`arc/struct/ObjectMap.java:18-19`)
- `get(key, supplier)` mutates the map (calls `put` if missing) — a "read-only" get can trigger a resize. (`arc/struct/ObjectMap.java:225-231`)

### IntMap / IntSet / LongMap
- Key `0` is special-cased via `zeroValue`/`hasZeroValue` — never stored in `keyTable`; `contains(0)`/`get(0)`/`remove(0)` take separate paths. (`arc/struct/IntMap.java:140-148,204-234`)
- Iterator `INDEX_ZERO = -1` is a sentinel — `next()` returns `0` when `nextIndex == INDEX_ZERO`. (`arc/struct/IntMap.java:594-705`)
- `Entries.next()` returns the same `Entry` instance (same aliasing gotcha as ObjectMap). (`arc/struct/IntMap.java:630-645`)

### OrderedMap / OrderedSet
- `put` on an existing key does NOT change its position — stays at original insertion index. (`arc/struct/OrderedMap.java:59-72`)
- `remove` + re-`put` moves the key to the END — no "touch" operation preserves position. (`arc/struct/OrderedMap.java:96-99`)
- Iteration is insertion-order via a `keys`/`items` `Seq` — faster than ObjectMap iteration but adds overhead to every put/remove. (`arc/struct/OrderedMap.java:246-271`)

### Queue
- Circular buffer with wrapping `head`/`tail` — the backing array is not compacted on remove; `get(i)` wraps via modulo. (`arc/struct/Queue.java:19-25,352-362`)
- `removeIndex` has 3 distinct code paths (continuous, wrapped-end, wrapped-start) — a bug in any corrupts the queue. (`arc/struct/Queue.java:277-306`)
- `Queue.iterator().remove()` calls `removeIndex` which is O(n) — not O(1). (`arc/struct/Queue.java:555-559`)

### SnapshotSeq / DelayedRemovalSeq
- `SnapshotSeq.begin()` returns the live backing array — mutation between `begin()`/`end()` triggers copy-on-write, but the returned array is stable. (`arc/struct/SnapshotSeq.java:68-97`)
- `DelayedRemovalSeq` throws `IllegalStateException` for most mutators between `begin()`/`end()` — only `remove`, `removeRange`, `clear`, `add` allowed. (`arc/struct/DelayedRemovalSeq.java:128-176`)
- `DelayedRemovalSeq.remove(index)` returns `get(index)` — element still in the array until `end()`, so the return is the current element at that index. (`arc/struct/DelayedRemovalSeq.java:104-110`)

### BinaryHeap / PQueue / EnumSet / ArrayMap
- `BinaryHeap.remove(node)` uses `node.index` — a stale index removes the wrong element. (`arc/struct/BinaryHeap.java:63-65`)
- `PQueue.add(null)` throws; `peek()`/`poll()` return null on empty (no `NoSuchElementException`). (`arc/struct/PQueue.java:53-106`)
- `EnumSet` uses a `long` bitmask — limited to 64 enum constants; a 65th silently corrupts via `1L << ordinal` overflow. (`arc/struct/EnumSet.java:5,22`)
- `ArrayMap` with capacity 0 shares a static `empty` array; `get` is O(n) linear scan — performance trap as a general map. (`arc/struct/ArrayMap.java:11-13,49-50`)

---

## 5. Assets / Audio / Files

### AssetManager
- Uses a **single-thread executor** — blocking I/O in one load stalls every other asset. (`arc/assets/AssetManager.java:66`)
- `load()` throws if the same filename is already queued/loaded under a DIFFERENT type (filename+type uniqueness enforced). (`arc/assets/AssetManager.java:424-441`)
- Loading the same asset twice does NOT reload — just increments the reference count. (`arc/assets/AssetManager.java:573-581`)
- `unload()` decrements a reference count; asset is disposed only at 0. Dependencies are ref-counted recursively. (`arc/assets/AssetManager.java:222-244`)
- `clear()` only disposes root assets first; dependencies unload implicitly when their last referrer is removed. (`arc/assets/AssetManager.java:765-804`)
- `update()` catches `Throwable`; on failure `handleTaskError` unloads all dependencies of the failed asset and **clears the entire task stack** — one failure aborts all pending loads. (`arc/assets/AssetManager.java:461-476,676-703`)
- `contains()` only checks `tasks.first()` (currently processing task), not the whole queue. (`arc/assets/AssetManager.java:161-168`)
- `addAsset()` blindly overwrites — no check for existing entries. (`arc/assets/AssetManager.java:600-611`)
- `getProgress()` uses `peakTasks` for fractional progress — can appear non-linear. (`arc/assets/AssetManager.java:740-747`)
- `AssetLoadingTask.removeDuplicates` temporarily mutates `Seq.ordered` — subtle side effect if the Seq is shared. (`arc/assets/AssetLoadingTask.java:139-151`)
- The `Fi` on `AssetDescriptor.file` is resolved lazily on first use and cached — null before the task runs. (`arc/assets/AssetLoadingTask.java:130-133`)
- `loadRun(name, type, loadasync, loadsync)` registers a one-time `CustomLoader` — the loader instance itself becomes the asset. (`arc/assets/AssetManager.java:348-373`)
- `AssetDescriptor` carries `loaded`/`errored` callbacks directly on the descriptor, and constructors **replace backslashes** with forward slashes in `fileName`. (`arc/assets/AssetDescriptor.java:19-37`)

### Files (Fi / ZipFi)
- `FileType.internal` on desktop **falls back to classpath** when the file doesn't exist on disk — silent classpath reads can mask missing files. (`arc/files/Fi.java:212-216`)
- `FileType.classpath` files are always readonly — `write()`/`delete()`/`mkdirs()` throw. (`arc/files/Fi.java:400,765,790`)
- `exists()` for `internal` checks disk first, then classpath; for `classpath` it ONLY checks the classpath. (`arc/files/Fi.java:774-783`)
- `length()` for internal/classpath uses `InputStream.available()` — unreliable for zip entries; returns 0 on failure. (`arc/files/Fi.java:886-898`)
- `FileType.external` `file()` prepends `Core.files.getExternalStoragePath()` — stored path is relative. (`arc/files/Fi.java:188`)
- `ZipFi` is read-only; `list()` caches children on first call (mutating the zip won't refresh). (`arc/files/ZipFi.java:101-171`)

### Audio (Soloud)
- `Audio.newSound()`/`newMusic()` return a **dummy uninitialized instance** on failure — no exception; check `valid()` before use. (`arc/audio/Audio.java:79-98`)
- `Sound.play()` returns **-1 on failure** (not 0) — 0 is a valid voice handle. (`arc/audio/Sound.java:124-155`)
- `Sound.play()` with `checkFrame=true` **intensifies the last voice** instead of playing a new instance if called within `minInterval` ms. (`arc/audio/Sound.java:138-145`)
- `Sound.createStream()` does NOT set `maxConcurrent` — streamed sounds have no concurrency limit by default. (`arc/audio/Sound.java:43-53`)
- `Sound.loadLazy()` defers loading until first `play()` — the sound is NOT loaded after construction. (`arc/audio/Sound.java:86-114`)
- `AudioBus.init()` calls `sourcePlay(handle)` — the bus must be played to have a valid id; `stop()` stops the bus voice, not individual sources. (`arc/audio/AudioBus.java:23-43`)
- `Audio.dispose()` calls `stopAll()` then `deinit()` — all audio stops, SoLoud shuts down; Sound/Music handles become invalid. (`arc/audio/Audio.java:177-182`)
- `Sound.createStream()`/`Music.create()` require the file to remain accessible for the lifetime of the stream. (`arc/audio/Sound.java:48`, `arc/audio/Music.java:44`)
- `Music` caches to `java.io.tmpdir` on desktop — temp files may be cleaned by the OS while in use. (`arc/audio/Music.java:188-203`)
- `Audio.setPitch()` clamps to `Math.max(pitch, 0.001f)` — pitch 0 is silence, not pause. (`arc/audio/Audio.java:137`)
- `Sound.play()` clamps pitch to `[0.0001f, 10f]` and pan to `[-1f, 1f]` — out-of-range values silently clamped. (`arc/audio/Sound.java:154`)
- `Soloud.sourcePlay(handle, volume, pitch, pan, loop)` parameter order is **volume, pitch, pan, loop** — not volume, pan, pitch, loop as in libGDX. (`arc/audio/Soloud.java:260-264`)

---

## 6. Input / Events / Math / Utils

### Events
- `Events.fire(T type)` dispatches by `type.getClass()` — registering on a superclass won't catch subclass events. Only exact class matches. (`arc/Events.java`)
- `Events.run(Object, Runnable)` wraps the Runnable in a `Cons` that ignores the event payload — the listener receives no argument. (`arc/Events.java`)
- No cancellation mechanism — all listeners fire in registration order; no "consume"/"stopPropagation". (`arc/Events.java`)
- `Events.remove` requires the exact same `Cons` reference — removal after anonymous lambda registration is impossible. (`arc/Events.java`)

### Input & Keybinds
- `KeyBind` constructor calls `load()`, which no-ops if `settings` is null — in headless/pre-init contexts keybinds silently keep defaults; reload manually once settings exist. (`arc/input/KeyBind.java`)
- `KeyBind.Axis` has two mutually exclusive states: `key != null` (single-key) vs `key == null` (dual-key min/max). A dual-key axis returns 0 when both are pressed. (`arc/input/KeyBind.java`, `arc/Input.java`)
- `KeyboardDevice.touchUp` only fires `keyUp` for pointer 0 — multi-touch releases on pointers 1+ are dropped, leaving phantom pressed keys. (`arc/input/KeyboardDevice.java`)
- `KeyboardDevice.scrolled` stores `-amountY` — scroll-up (positive delta) yields a negative axis value, contrary to most frameworks. (`arc/input/KeyboardDevice.java`)
- `InputMultiplexer` uses `SnapshotSeq` — structural modifications during dispatch are deferred to the next frame; a processor returning `true` stops delegation for that event only. (`arc/input/InputMultiplexer.java`)
- `KeyCode.byOrdinal` throws `ArrayIndexOutOfBoundsException` on invalid ordinals rather than returning a sentinel. (`arc/input/KeyCode.java`)
- `GestureDetector` only tracks 2 pointers — `touchDown`/`touchDragged`/`touchUp` return early for `pointer > 1`, silently dropping multi-touch beyond 2. (`arc/input/GestureDetector.java`)

### Mathf & Math
- **`Mathf.atan2(x, y)` has swapped parameter names** — first param named `x` represents the traditional `y`. `Mathf.atan2(1, 0)` computes 0, not π/2. (`arc/math/Mathf.java`)
- **`Mathf.angle(x, y)` and `Vec2.angle()` measure from the y-axis, not the x-axis** — `(1,0)` yields 90°, not 0° (deliberate game-dev convention: 0° = up). Breaks standard math expectations. (`arc/math/Mathf.java`, `arc/math/geom/Vec2.java`)
- `Mathf.floor/ceil/round(float)` use a `BIG_ENOUGH_INT` trick valid only in `-(2^14)` to `Float.MAX_VALUE - 2^14` — wrong outside that range. (`arc/math/Mathf.java`)
- `Mathf.sign(float)` returns 1 for zero, not 0. (`arc/math/Mathf.java`)
- `Mathf.sin/cos` use a 14-bit lookup table — fast but approximate; use `Math.sin/cos` for exact values. (`arc/math/Mathf.java`)
- `Mathf.lerpDelta`/`slerpDelta` multiply progress by `Time.delta` internally — passing 1.0 doesn't guarantee reaching the target in one frame. (`arc/math/Mathf.java`)
- `Mathf.mod` handles negatives correctly (unlike Java `%`) — result always in `[0, n)`. (`arc/math/Mathf.java`)

### Vec2 / geometry / matrices
- All Vec2 mutation methods return `this` — chaining is by design, but forgetting `cpy()` before mutating leads to aliased data corruption. (`arc/math/geom/Vec2.java`)
- `Vec2.setLength`/`nor()` silently no-op on zero-length vectors — no exception, no NaN; can mask bugs. (`arc/math/geom/Vec2.java`)
- `Rect.contains(Rect)` uses mixed strict/non-strict inequality — a rect does not contain itself; edge-touching rects are not contained. (`arc/math/geom/Rect.java`)
- `Rect.tmp`/`Rect.tmp2` and `Intersector`'s static Vec2/Vec3 fields are shared globals — nested/reentrant geometry corrupts each other. (`arc/math/geom/Rect.java`, `arc/math/geom/Intersector.java`)
- `Scaling.apply()` always returns the same `temp` Vec2 instance — caching the result across calls silently produces wrong values. (`arc/util/Scaling.java`)
- `Mat` is **column-major** (`M01 = 3`, `M10 = 1`) — row-major assumptions produce transposed transforms. (`arc/math/Mat.java`)
- `Mat.translate/rotate/scale` post-multiply — new transform applies in local space (OpenGL convention). (`arc/math/Mat.java`)

### Strings / Log
- `Log.format` and `Strings.format` use **`@` as the placeholder**, not `{}` or `%s`. Multiple `@` consume arguments sequentially. (`arc/util/Log.java`, `arc/util/Strings.java`)
- `Log` prepends color codes by default (`useColors = true`) — stripped at the `ColorCodes` level on Windows (non-WT) and Android, but raw on Linux/Mac. (`arc/util/Log.java`, `arc/util/ColorCodes.java`)
- `Strings.join` crashes on empty input — unconditionally truncates by `separator.length()`, causing `StringIndexOutOfBoundsException`. (`arc/util/Strings.java`)
- `Strings.parseInt` returns `Integer.MIN_VALUE` on failure — a valid integer; use `canParseInt` to distinguish. (`arc/util/Strings.java`)
- `Strings.fixedBuilder` reuses static `tmp1`/`tmp2` StringBuilders — not thread-safe, unsafe for nested/reentrant calls. (`arc/util/Strings.java`)

### Time / Timer
- `Time.delta` initializes to `1f`, not `0f` — the first frame's delta is 1 tick, which can cause a visible jump. (`arc/util/Time.java`)
- `Time.runTask(delay, ...)` takes **ticks, not seconds** (converts internally via `delay / 60f`) — misleading naming. (`arc/util/Time.java`)
- `Timer.Task` cannot be scheduled twice — throws `IllegalArgumentException`; must `cancel()` first. (`arc/util/Timer.java`)
- `Timer.Task` constructor requires `Core.app != null` — creating tasks before app init throws `IllegalStateException`. (`arc/util/Timer.java`)

### Pools
- `Pool.free()` resets the object after adding it to the free list — if `reset()` throws, the pool is left corrupted with a partially-reset object. (`arc/util/pooling/Pool.java`)
- `Pool` doesn't detect double-free — freeing the same object twice silently corrupts the pool's list. (`arc/util/pooling/Pool.java`)
- `Pools.get` ignores the `max` parameter for existing pools — subsequent calls with a different `max` return the original unchanged. (`arc/util/pooling/Pools.java`)

### Misc utils
- `Structs.indexOf(T[], T)` uses `==` identity, not `equals()` — differs from `List.indexOf()`; can fail for value objects. (`arc/util/Structs.java`)
- `Reflect.get(Object, String)` uses `getDeclaredField` — inherited fields are not found; walk the class hierarchy manually. (`arc/util/Reflect.java`)
- `Align.center = 1` is the same as no flags — `isCenterVertical` returns true when neither `top` nor `bottom` is set, making `Align.center` a no-op in most layout code. (`arc/util/Align.java`)

---

## Cross-cutting conventions (from mod AGENTS.md)

- No `java.util.function`, `java.awt`, `java.lang.Objects`, or streams — use `arc.func`, `arc.struct` equivalents.
- Prefer `Seq`/`ObjectMap`/`IntSeq`/`IntMap` over `List`/`HashMap`/`HashSet`/`Seq<Integer>`.
- Never allocate in the main loop — use `Pools` or `Tmp`/`static` reuse.
- Inline `//` comments are sentences (`// Like this`); log messages carry the `[MU] ` prefix where applicable.
- Target Mindustry `v9` branch APIs; `old-src/` is reference-only.
