# Medieval Cookery User Guide

## What is Medieval Cookery?

Medieval Cookery is a Spigot plugin that adds craftable medieval-themed food items to Minecraft. Each food is a custom player-head item with its own texture, crafted at the standard crafting table from a shaped recipe defined in `recipes.yml`.

## Installation

1. Download the latest `Medieval-Cookery-<version>.jar` from the [Releases](https://github.com/Dans-Plugins/Medieval-Cookery/releases) page.
2. Place the JAR in your server's `plugins/` folder.
3. Restart the server.

On the first startup a default `recipes.yml` is written to `plugins/MedievalCookery/`. Edit that file to change or add recipes, then restart the server for the changes to take effect. See [CONFIG.md](CONFIG.md) for the full field reference.

## Craftable Foods

All recipes use the standard 3×3 crafting table. In the patterns below, each row is one row of the grid and a `.` marks an empty slot.

| Food | Pattern | Ingredients |
|------|---------|-------------|
| Salmon Roll | `KWK` / `WRW` / `KWK` | K = Dried Kelp, W = Wheat, R = Salmon |
| Beet Salad | `BBB` / `GGG` / `.W.` | B = Beetroot, G = Grass, W = Bowl |
| Bowl of Rice | `WWW` / `WWW` / `.B.` | W = Wheat, B = Bowl |
| Steak Sandwich | `.B.` / `.S.` / `.B.` | B = Bread, S = Cooked Beef |
| Salted Herring | `.B.` / `BCB` / `.B.` | B = Bone Meal, C = Cod |

## Known Limitations

Crafting is the only part of the plugin that is currently wired up. The `hungerDecrease` and `afterEatItem` values are read from `recipes.yml` and kept in memory, but no event handler consumes a Medieval Cookery food item, so eating one applies no Saturation effect and returns no `afterEatItem`. The crafted foods are player heads, which vanilla Minecraft does not treat as edible.

## Support

Open a [GitHub issue](https://github.com/Dans-Plugins/Medieval-Cookery/issues) to report bugs or request features.
