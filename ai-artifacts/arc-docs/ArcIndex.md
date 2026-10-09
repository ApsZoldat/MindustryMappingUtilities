# Arc index

Arc is the gamedev framework Mindustry is built on (a libGDX fork, included in the
Mindustry repo under `arc/`). Source: `../Mindustry/arc/arc-core/src/arc/`.

This file is the curated class tree for `arc-docs/` - which Arc files matter here and how
they relate. The file map with read-when notes lives in `AGENTS.md`.

## Class tree

<!-- Curated, ~100-150 lines max - NOT a full dump. Arc is 464 files / 100k LOC.
     Full trees are derivable with find/glob and rot fast; a stale entry makes agents
     skip the source that would have corrected them.
     Record only what source cannot give:
       curation     - which files actually matter for mapping-utilities work
       relationships - what extends / patches / reflects what
       purpose      - one line per entry, why it exists -->

### arc/ root

- `Core.java` - static service access for the running app (`Core.scene`, `Core.input`, ...);
  `Core.scene` is assigned by the application, see Scene.md
- `Events.java` - global event bus; mods subscribe at startup
- `Application*.java` lifecycle + the platform services (`Input`, `Graphics`, `Files`,
  `Settings`) exposed as `Core.*` statics

### scene/ - the UI system (this mod's main surface)

- `scene/Element.java` - base UI element (libGDX's Actor) - see Element.md
- `scene/Group.java` - child container: hit-test order, `find`/`findVisible`, scene
  propagation to children
- `scene/Scene.java` - libGDX's Stage equivalent - see Scene.md
- `scene/Action.java` + `scene/actions/` - action base + `Actions` factory/library - see
  Actions.md
- `scene/event/` - event types, Touchable, and the listener classes - see Events.md
- `scene/style/` - Drawable family + Style - see Drawable.md
- `scene/flabel/` - `FLabel` + `effects/` per-glyph animated rich-text label
- `scene/ui/` - widgets: Dialog, ScrollPane, TextField/TextArea, Label, Button family
  (TextButton, ImageButton, CheckBox), Dropdown, Slider, ProgressBar, Touchpad, Tooltip,
  ButtonGroup, Image/ColorImage, TreeElement
- `scene/ui/layout/` - Table/Cell layout plus Stack, Collapser, Spacer, WrapTable,
  WidgetGroup, and `Scl` (UI scaling) - see Table.md, Coordinates.md

### supporting packages

- `func/` - functional interfaces used instead of `java.util.function`
- `struct/` - collections; `DelayedRemovalSeq` backs listener lists (safe removal during
  dispatch)
- `input/` - `KeyCode` (libGDX's `Input.Keys` equivalent), InputProcessor, InputMultiplexer
- `math/`, `math/geom/` - math and geometry
- `graphics/` - Color/Texture, batch drawing (`Draw`/`Fill`/`Lines`, ScissorStack), fonts
- `util/` - Log, Tmp, Reflect, Align; `util/viewport/` (Scene's default is
  ScreenViewport); `util/pooling/`
- Not listed - rarely relevant here: assets/, audio/, net/, fx/, mock/, packer/, files/
