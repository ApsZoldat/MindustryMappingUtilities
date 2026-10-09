<!-- verified: v9 @ 2026-10-09, sources: build.gradle.kts, build.sh, mod.hjson, arc/util/OS.java, arc/Settings.java, mindustry/Vars.java, mindustry/ui/dialogs/ModsDialog.java, mindustry/mod/Mods.java -->

# Running the mod

How to build, install, and launch the mod for manual testing. Source paths above are in
`../Mindustry` unless noted; repo paths are relative to this repository root.

## Build

- `./gradlew jar` - the mod jar -> `build/libs/MindustryMappingUtilitiesDesktop.jar`.
  It already contains `mod.hjson` + `icon.png` at the root, so the jar installs as-is.
- `./gradlew build` - desktop compile gate (jar + checks). No Android SDK needed; use
  this to verify code changes locally.
- `./gradlew deploy` - desktop + Android package. Needs `ANDROID_HOME`; CI runs it on
  every push (`.github/workflows/commitTest.yml`) - not needed locally.
- Requires `libs/Mindustry.jar` (compileOnly). Present in this checkout; CI downloads
  the BE jar fresh on each run.

## Install

Copy the jar into the game's mods folder (`Vars.modDirectory = dataDirectory/mods/`):

| OS | Mods folder |
|---|---|
| Windows | `%AppData%\Mindustry\mods` |
| Linux | `~/.local/share/Mindustry/mods` (or `$XDG_DATA_HOME/Mindustry/mods/`) |
| macOS | `~/Library/Application Support/Mindustry/mods` |

Easiest from inside the game: **Mods dialog -> open folder** button opens exactly this
directory (`ModsDialog`, `Vars.modDirectory`).

`build.sh` automates build + copy for this machine: `./gradlew jar` then pushes the jar
from WSL to the Windows host's mods folder.

## Launch and verify

- Game build must satisfy `minGameVersion: 159` (`mod.hjson`) - a recent BE/v9 build.
- In the Mods dialog the mod is listed as **mapping-utilities**. It is `hidden: true`,
  which only means: no mod *content* is loaded (`Mods.loadContent` skips hidden mods) and
  its dialog state shows as multiplayer-compatible - the `mu.MUMain` code still runs. So
  verify by the **editor behaving differently**, not by content appearing.
- After changing the jar, restart the game (mods load at startup).
- Remove/disable: delete the jar from the mods folder, or uncheck it in the Mods dialog
  (takes effect on restart).
