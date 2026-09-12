# Medieval Cookery Configuration

Recipes are configured in `recipes.yml`. The plugin's only other setting, usage reporting,
lives in `config.yml`; both files are written to the plugin's data folder on first startup.

## config.yml

| Key | Default | Description |
|-----|---------|-------------|
| `usage-reporting.enabled` | `true` | Whether the plugin reports usage events (see below). Set to `false` to turn it off. |
| `usage-reporting.endpoint` | `https://trace.danielstephenson.dev` | The trace server events are sent to. |
| `usage-reporting.key` | the plugin's key | Identifies this plugin to the trace server so reports are attributed to it. Not a secret: it ships in the default config and can only report as MedievalCookery. Empty means reporting is off regardless of `enabled`. |

An existing `config.yml` in the data folder is never overwritten by a plugin update. A server
upgraded from a version before the `usage-reporting` block existed keeps its old file, and the
plugin reads the bundled defaults for any key that file lacks, so reporting is active there too
unless the block is added and `enabled` set to `false`.

### Usage reporting

When the plugin is enabled, a small event is sent to the author's
[trace](https://github.com/Stephenson-Software/trace-client-java) server so it is known which
plugins are actually in use. An event carries the plugin's name, the event name (`startup`), and
the plugin version — nothing about players, the world, or the server. Medieval Cookery has no
commands, so `startup` is the only event it sends. Sending happens off the main thread, never
delays a tick, and is dropped silently if the server cannot be reached. Set
`usage-reporting.enabled` to `false` to turn it off.

## recipes.yml

## Where recipes.yml Lives

On first startup the plugin writes its bundled default `recipes.yml` into its own data
folder, and it is that copy which is read on every subsequent startup. The data folder is
`plugins/MedievalCookery/` unless the server has been started with its plugins directory
somewhere else, in which case it is `MedievalCookery/` under that directory. Server owners
should edit the copy in the data folder; the file at `src/main/resources/recipes.yml` in
this repository is only the default that gets written out when no copy exists yet.

An existing `recipes.yml` in the data folder is never overwritten by a plugin update,
so recipes added to the bundled default in a later version are not picked up until the
server copy is removed or updated by hand.

## recipes.yml Fields

| Field | Required | Description |
|-------|----------|-------------|
| `pluginVersion` | No | Version marker at the top of the file. Present in the bundled default but not currently read by the plugin. |
| `recipes.<id>.name` | Yes | Display name of the food item. Recipe lookup matches on this name case-insensitively and returns the first match, so names should be unique. |
| `recipes.<id>.recipe` | Yes | 3×3 crafting grid pattern, as a list of exactly 3 strings. A space means "empty slot". |
| `recipes.<id>.symbols` | Yes | Maps single pattern characters to Bukkit material names (for example `W: "WHEAT"`). |
| `recipes.<id>.hungerDecrease` | No | Duration, in ticks, of the Saturation potion effect applied after the food is eaten. Defaults to `1` when omitted. |
| `recipes.<id>.afterEatItem` | No | Bukkit material returned to the player after eating (for example `BOWL`). No item is returned when omitted. |
| `recipes.<id>.textureBase64` | No | Base64-encoded skin texture used for the custom player-head item. |

A recipe entry that cannot be used as configured is skipped, and the reason is logged with
the entry's id. The remaining recipes still load, and the plugin still enables. An entry is
skipped when:

- it has no `name`;
- it has no `symbols` section, or that section declares no symbols;
- one of its symbols is longer than a single character;
- one of its symbols names no material, or names a material Bukkit does not recognise;
- its `recipe` pattern is not exactly 3 rows of exactly 3 characters;
- its pattern uses a character the `symbols` section does not declare, or declares a symbol
  the pattern never uses.

Two other values are tolerated rather than skipping the recipe, because neither prevents it
from being crafted:

- an `afterEatItem` naming a material Bukkit does not recognise is logged, and the recipe
  loads with no after-eat behaviour;
- a `textureBase64` value that is absent or shorter than 20 characters is not logged, and the
  food is a default-skinned player head carrying the configured display name.
