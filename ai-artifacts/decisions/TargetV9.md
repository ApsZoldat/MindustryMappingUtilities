# Target the v9 branch, ignore stock compatibility

**Status:** active
**Scope:** every coding decision in this mod

## The decision

We target the Mindustry `v9` branch and its APIs. Stock (current release) compatibility is
explicitly **not** a goal right now.

Concretely: if an API refactor exists only in the `v9` branch, use the new API. Do not write
shims, fallbacks, or `if(v9)` branches to keep the old API working.

## Why

1. **Stock compatibility can be added later, cheaply.** v9 support is temporary scaffolding
   until v9 actually releases - it lives for a few months, not forever. Because the window is
   short and bounded, we can afford some stupid stuff in it.

2. **Making clean code worse is easier than making poor code cleaner.** If we contort the
   design now to satisfy both the old and new API, that contortion tends to become permanent -
   cleaning it up later costs more than adding compatibility later. Going the other way,
   writing straightforwardly against the new API and bolting compatibility on afterwards, is
   the cheap direction.

3. **We can PR upstream.** Where the new API is missing something we need, the change can be
   proposed to Mindustry itself against the `v9` API rather than worked around locally. That
   is only possible if we are already written against the new API.

## Practical consequences

- Read and build against `../Mindustry`, branch `v9`.
- New APIs introduced by the v9 refactor are fair game, even if they are unstable.
- Do not spend effort on backwards compatibility, deprecation shims, or supporting older
  Mindustry versions.
- Do not reject a cleaner design on the grounds that it depends on a v9-only API.
- If a required capability is missing in v9, prefer proposing an upstream change over
  building an awkward local workaround.

See also: `AGENTS.md` key decisions list.
