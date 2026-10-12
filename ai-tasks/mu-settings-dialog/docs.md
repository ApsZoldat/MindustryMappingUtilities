# Docs for mu-settings-dialog
Mindustry commit: 5d45d9ad95

## Mindustry/Arc symbols used (verified)
| Symbol | Signature | Source path:line | Used for |
|--------|-----------|------------------|----------|
| `BaseDialog` | `class BaseDialog extends Dialog` | `../Mindustry/core/src/mindustry/ui/dialogs/BaseDialog.java:14` | Base class for `MUSettingsDialog` |
| `BaseDialog(String title)` | constructor | `../Mindustry/core/src/mindustry/ui/dialogs/BaseDialog.java:42` | Sets dialog title |
| `BaseDialog.addCloseButton()` | `void addCloseButton()` | `../Mindustry/core/src/mindustry/ui/dialogs/BaseDialog.java:81` | Adds close button to dialog |
| `Dialog.cont` | `public final Table cont` | `../Mindustry/arc/arc-core/src/arc/scene/ui/Dialog.java:58` | Content pane for building UI |
| `Dialog.show()` | `Dialog show()` | `../Mindustry/arc/arc-core/src/arc/scene/ui/Dialog.java:515` | Shows the dialog |
| `Dialog.hide()` | `void hide()` | `../Mindustry/arc/arc-core/src/arc/scene/ui/Dialog.java:552` | Hides the dialog |
| `Dialog.isShown()` | `boolean isShown()` | `../Mindustry/arc/arc-core/src/arc/scene/ui/Dialog.java:472` | Checks if dialog is visible |
| `BaseDialog.onResize(Runnable)` | `void onResize(Runnable run)` | `../Mindustry/core/src/mindustry/ui/dialogs/BaseDialog.java:59` | Resize listener |
| `CheckBox` | `class CheckBox extends TextButton` | `../Mindustry/arc/arc-core/src/arc/scene/ui/CheckBox.java:14` | Checkbox UI element |
| `CheckBox(String text, Boolc listener)` | constructor | `../Mindustry/arc/arc-core/src/arc/scene/ui/CheckBox.java:23` | Creates checkbox with change listener |
| `Settings.getBool(String, boolean)` | `boolean getBool(String name, boolean def)` | `../Mindustry/arc/arc-core/src/arc/Settings.java:364` | Reads boolean setting |
| `Settings.put(String, Object)` | `void put(String name, Object object)` | `../Mindustry/arc/arc-core/src/arc/Settings.java:434` | Writes setting value |
| `Settings.defaults(Object...)` | `void defaults(Object... objects)` | `../Mindustry/arc/arc-core/src/arc/Settings.java:292` | Registers default values |
| `UI.settings` | `public SettingsMenuDialog settings` | `../Mindustry/core/src/mindustry/core/UI.java:64` | Access to settings dialog |
| `SettingsMenuDialog.menu` | `private Table menu` | `../Mindustry/core/src/mindustry/ui/dialogs/SettingsMenuDialog.java:41` | Private field accessed via reflection |
| `Icon.editor` | `public static TextureRegionDrawable editor` | `../Mindustry/core/build/generated/source/kapt/main/mindustry/gen/Icon.java:392` | Button icon |
| `UI.showInfo(String)` | `void showInfo(String info)` | `../Mindustry/core/src/mindustry/core/UI.java:474` | Shows restart message |
| `MU.modules` | `public static Seq<MUModule> modules` | `java/mu/MU.java:13` | List of modules to create toggles for |
| `MUModule` | `class MUModule` | `java/mu/modules/MUModule.java:8` | Module base class |
| `MUModule.name` | `public final String name` | `java/mu/modules/MUModule.java:10` | Setting key for module toggle |
| `MUModule.def` | `public final boolean def` | `java/mu/modules/MUModule.java:12` | Default value for module toggle |

## Constraints and pitfalls
- C1: `SettingsMenuDialog.menu` is private — must use reflection to access it. The field reference remains valid after `rebuildMenu()` because `rebuildMenu()` calls `menu.clearChildren()` but does not reassign the field.
- C2: `Core.settings.put` only accepts `Boolean`, `Integer`, `Long`, `Float`, `String`, or `byte[]` — throws `IllegalArgumentException` for other types.
- C3: `CheckBox` constructor with `Boolc` listener fires `changed()` on every toggle — the listener must be idempotent.
- C4: `BaseDialog` constructor calls `setFillParent(true)` — the dialog fills the parent by default.
- C5: Reflection on `menu` field must handle `NoSuchFieldException` if the field is renamed in a future Mindustry version.

## Unverified
- (empty)
