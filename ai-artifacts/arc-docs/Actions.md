<!-- verified: v9 @ 2026-10-09, sources: scene/Action.java, scene/Element.java, scene/Group.java, scene/Scene.java, scene/actions/Actions.java, scene/actions/TemporalAction.java, scene/actions/RelativeTemporalAction.java, scene/actions/SequenceAction.java, scene/actions/ParallelAction.java, scene/actions/RepeatAction.java, scene/actions/DelegateAction.java, scene/actions/DelayAction.java, scene/actions/AfterAction.java, scene/actions/RunnableAction.java, scene/actions/AddListenerAction.java, scene/actions/RemoveAction.java, scene/actions/RemoveActorAction.java; libGDX master: scene2d/actions/SequenceAction.java, scene2d/Actor.java -->

# actions (arc.scene.actions)

libGDX `Actor`/`Stage`/`Actions` map to Arc `Element`/`Scene`/`arc.scene.actions`. Base class is `arc.scene.Action` (`Action#act(float)` returns true when done).

## Lifecycle

- Driven by `Element#act(float)` (from `Scene#act(float)`, which also does pointer enter/exit
  and focus) - never during draw. On `act()` returning true, `Element#act` removes the action
  and calls `setActor(null)` **in the same frame**. Frame ordering: [Element](Element.md).
- `Group#act` runs its own actions first, then each child - **only if the child is `visible`**,
  so a hidden element's actions freeze (libGDX's `Actor#act` has no visibility check).
- Layout is separate: `Element#draw()` calls `validate()` -> `layout()`. Size actions trigger
  `Element#sizeChanged()` -> `invalidate()`, so the new size lays out on the next draw, not when
  the action ticked. See [UI.md](UI.md).
- Removing an element (`Element#remove()` -> `Group#removeChild`) does **not** clear its
  actions; `Group#removeChild`'s javadoc says call `clearActions()` yourself or pooled actions
  never go back, and a detached element gets no `act()` so leftovers just sit there.
  `SequenceAction#act`/`ParallelAction#act` return true once their `actor` is null, so
  composites still unstick if removal happens during their own act.

## Pooling

- Factories use `Actions#action(Class, Prov)` = `Pools.obtain` + `setPool(...)`; freeing happens
  on `setActor(null)` (completion, `removeAction`, `clearActions`), and `reset()` calls
  `restart()`. Completed actions are pooled, but only because `Element#act` detaches them.
- Anything built with `new` has no pool: never auto-freed, never reset. Quirk:
  `Actions.originCenter()` is the one factory returning `new OriginAction()` (unpooled).
- Composites null their pool while executing (`DelegateAction#act`, `TemporalAction#act`,
  `SequenceAction#act`, `ParallelAction#act`, `RunnableAction#run`) so a nested completion can't
  free them mid-execution - and never reuse a finished pooled action, it may be running elsewhere.

## Composition

- `SequenceAction extends ParallelAction` (shared child list, same as libGDX). Runs one
  child at a time, done when the last child is done.
- `ParallelAction`: all children per frame, done when all are done; skips children whose
  `getActor()` is null; aborts if its own `actor` becomes null.
- `DelegateAction` wraps one action (`delegate(float)`), forwarding `setActor`/`setTarget`.
  `RepeatAction` uses it: `Actions.repeat(count, a)` / `forever(a)` (`FOREVER = -1`), child
  restarted each cycle, `finish()` stops early.
- Delay: `Actions.delay(seconds)` bare pause or `Actions.delay(seconds, action)` wrapping - the
  leftover delta past the duration goes to the child (`DelayAction#delegate`). `Actions.after(a)`
  (`AfterAction`) snapshots the target's actions **when its target is set**; later-queued ones are ignored.
- `TemporalAction`: duration, `Interp` (libGDX's `Interpolation`), `begin()/update(percent)/end()`,
  `setReverse()`, `finish()` skips to end; `RelativeTemporalAction` feeds `updateRelative(percentDelta)` per frame.

## Deltas vs libGDX

- **`Actions.remove()` / `remove(Element)` -> `RemoveActorAction`**: renamed from libGDX's
  `removeActor()`. Beware: Arc's `RemoveAction` (from `Actions.removeAction`) removes *an action*
  from an element - no libGDX equivalent.
- Arc-only factories: `addAction` (run an action on a target element), `addListener`/
  `removeListener(listener, capture[, target])`, `translateTo`/`translateBy`, `timeScale`,
  `layout(boolean)`, `originCenter`, generic `action(Class, Prov)`.
- Everything else keeps its libGDX name (`moveTo`, `fadeIn`, `sequence`, `parallel`, ...).
  Arc-only: `Element#actions(Action...)` = `addAction(Actions.sequence(...))` (plus
  `hasActions`/`clearActions`).

## One-shot callbacks

- `Actions.run(runnable)` -> `RunnableAction`: fires once (`ran` guard), reports done
  immediately; `reset()` nulls the runnable. But the lambda holds captures until the action
  frees - if the element is removed without `clearActions()`, action *and* captures linger.
- `AddListenerAction` adds to `target` (defaults to the acting element) with an explicit
  `capture` flag; listeners added this way survive element removal until `removeListener` or
  `clearListeners()`.
