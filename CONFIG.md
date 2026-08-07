# Medieval Cookery Configuration

Medieval Cookery does not use a `config.yml`. Recipes are configured in `recipes.yml`.

## Where recipes.yml Lives

On first startup the plugin writes its bundled default `recipes.yml` to
`plugins/MedievalCookery/recipes.yml`, and it is that copy which is read on every
subsequent startup. Server owners should edit the copy under `plugins/MedievalCookery/`;
the file at `src/main/resources/recipes.yml` in this repository is only the default that
gets written out when no copy exists yet.

An existing `plugins/MedievalCookery/recipes.yml` is never overwritten by a plugin update,
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

Failures are not handled uniformly, so each entry should be checked against the table above
before the server is restarted:

- A recipe with no `name` is skipped, and an error is logged for it. The remaining recipes
  still load.
- A recipe with no `symbols` section, or with a `recipe` pattern of fewer than 3 rows,
  aborts recipe loading entirely and stops the plugin from enabling.
- A symbol pointing at a material name that Bukkit does not recognise is logged, and that
  recipe is not registered with Bukkit, so it cannot be crafted.
