# Medieval Cookery Configuration

Medieval Cookery does not use a `config.yml`. Recipes are configured in `src/main/resources/recipes.yml`.

## recipes.yml Fields

| Field | Description |
|-------|-------------|
| `pluginVersion` | Version of the recipes file format. |
| `recipes.<id>.name` | Display name of the food item. |
| `recipes.<id>.recipe` | 3×3 crafting grid pattern (array of 3 strings). |
| `recipes.<id>.symbols` | Maps pattern characters to Bukkit material names. |
| `recipes.<id>.hungerDecrease` | Saturation restored when the food is eaten. |
| `recipes.<id>.afterEatItem` | Material returned to the player after eating (e.g. `BOWL`). Optional. |
| `recipes.<id>.textureBase64` | Base64-encoded skin texture for the custom item head. Optional. |
