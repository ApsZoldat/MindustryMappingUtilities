<!-- verified: v9 @ 2026-10-09, sources: editor/MapObjectivesDialog.java, editor/MapObjectivesCanvas.java, editor/MapInfoDialog.java -->

# MapObjectivesDialog

Node-graph editor for `MapObjective`s (campaign/sector objectives placed on a
tilemap). `extends BaseDialog`, instantiated in MapInfoDialog, shown via
`show(Seq<MapObjective>, Cons<Seq<MapObjective>>)`.

## Commit model - live, not staged

`rebuildObjectives` puts the caller's list straight into `canvas.objectives`
(`Seq.set`), and `hidden()` calls `out.get(canvas.objectives)` - **edits mutate
the caller's `state.rules.objectives.all` in place while the dialog is open**.
Objectives with `editorX/editorY == -999` get auto-laid-out on first open.

## MapObjectivesCanvas

`extends WidgetGroup`; holds `public Seq<MapObjective> objectives` (authoritative)
plus `public ObjectiveTilemap tilemap` - **two parallel structures kept in sync by
hand** (tiles are added in `placeQuery`/dialog rebuild, removed only via
`ObjectiveTile.remove`; `clearObjectives()` clears tiles only, the dialog re-sets
the Seq).

- `query(obj)` arms placement; `placeQuery()` creates a tile + adds to the Seq.
- `ObjectiveTile` holds `public final MapObjective obj`; `pos(x, y)` writes
  `obj.editorX/editorY`; the pencil button opens the field editor; `remove()`
  splices the objective out of the Seq **and every parent list**.
- `Connector` toggles `obj.parents` add/remove.
- Pan is clamped to `bounds * unitSize`.

## Reflection-based field UI (static registries)

The dialog hosts the objective field-editor registry used across the game UI:
`providers`/`interpreters` static maps, `setInterpreter/getInterpreter/
setProvider/getProvider`, interfaces `FieldInterpreter`/`FieldProvider`,
`TypeInfo`. Field edits go through `Reflect.get/set` on the objective instance.
To add a custom objective type's editor, register via `setInterpreter`/
`setProvider` - the registry is global static and affects every consumer.

Clipboard: ctrl+c/v handled by `doInput()`, wired via
`update(() -> if(hasKeyboard()) doInput())`.
