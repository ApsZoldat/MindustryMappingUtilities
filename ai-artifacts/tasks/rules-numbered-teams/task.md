# Numbered teams editor (deferred)

## Goal

Deferred from `rules-dialog-module` (Q4 decision): the old-src numbered-teams
feature - edit any `TeamRule` by team id (0-255), not just the six base teams.

## Scope

- `java/mu/RulesDialogModule.java` (additive; does not exist yet - created by
  `rules-dialog-module`) - or a dedicated helper class if it grows.

## Status

Stub - intentionally deferred, unspecified. The old-src reference is
`RulesDialogMod.updateNumberedEdit` + the `@rules.numberedteam` row (the old
bundle has the key; port it when this is picked up). Open design point: v9
`rules.teams` is `TeamRules` (a `JsonSerializable` map wrapper) - check how
non-base teams round-trip through map save before building UI on top.

## Acceptance criteria

(TBD when this task is specified - run create-tasks on it.)

## Constraints

(TBD)

## Refs

- `old-src/java/mu/mods/RulesDialogMod.java` - `updateNumberedEdit`.
- `old-src/resources/bundles/bundle.properties` - `rules.numberedteam` key.
- v9 `/root/projects/Mindustry/core/src/mindustry/game/Rules.java` -
  `TeamRules`, `TeamRule`.

## Open questions

- Whether arbitrary team ids still serialize in v9 maps.
