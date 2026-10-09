# Modules are atomic, restart-to-apply units

**Status:** active
**Scope:** every feature added to this mod after the v9 rewrite began

## The decision

A mod feature that can meaningfully break on its own - reflection into game
internals, patching UI structure, anything version-fragile - is a `MUModule`
subclass (`java/mu/modules/MUModule.java`). A module is atomic: it is either fully on or
fully off, and one module failing never affects another.

Lifecycle (all of it - there is no runtime enable/disable):

- At startup (`ClientLoadEvent`), `MU` runs each registered module's `init()`,
  skipping modules whose setting is false.
- If `init()` throws, the module is **disabled for this session only**: the error
  (full stack trace) goes to the log, and a startup dialog lists "the following
  modules failed to initialize and have been disabled for this session".
  Failures are never persisted to settings - the module retries on next launch.
- The user disables a module via the settings checkbox. The change applies on
  the next game restart; toggling shows a "restart required" notice. There is
  no revert and no live toggle.

Non-fragile glue (e.g. the settings-menu category itself) is not a module - it
is always enabled and has no toggle.

The setting key that enables a module is defined by the module class itself,
passed to `super(name, def)` in its constructor - one source of truth read by
both the settings checkbox and `MUModule.enabled()`. Every `settings.getBool`
call passes an explicit default; nothing depends on the settings menu having
built its defaults first.

## Why

1. **Fragile integrations fail in the field.** This mod reflects into private
   game fields; a game update can rename any of them at any time. A failure
   must degrade to "feature off, everything else alive", never to a broken
   launch.
2. **Session-only auto-disable keeps users current.** Persisting a failure into
   settings would silently strand users on a disabled feature after the cause
   (game update, conflicting mod) is fixed. Retrying every launch is the honest
   default; the notice explains what happened.
3. **Restart-to-apply removes an entire class of bugs.** Live enable/disable
   (the pre-rewrite `MUMod` model) requires every module to correctly undo its
   hooks - remove listeners, restore fields - and any missed undo corrupts
   state for the rest of the session. Applying only at startup means a disabled
   module's hooks were never installed; there is nothing to undo.
4. **Atomicity bounds the blast radius.** Users disable the thing that broke,
   not the whole mod.

## Practical consequences

- New fragile features ship as `MUModule` subclasses registered in
  `MU.modules`; `init()` may throw, the framework handles the rest.
- Do not add runtime enable/disable paths, hook-removal code, or revert
  functionality for module toggles.
- Per-failure granularity inside a module (e.g. one `CustomRulesDialog` that
  cannot be reflected) is the module's own business: log and skip that piece,
  only fail `init()` when the module cannot do anything useful at all.
- Settings-menu additions for modules are generated from `MU.modules`, not
  hand-listed.

See also: `ai-artifacts/tasks/mu-module-framework/task.md`,
`AGENTS.md` key decisions list.
