# Medieval Cookery User Guide

## What is Medieval Cookery?

Medieval Cookery is a Spigot plugin that adds craftable medieval-themed food items to Minecraft. Each food is a custom player-head item with its own texture, crafted at the standard crafting table from a shaped recipe defined in `recipes.yml`.

## Installation

1. Download the latest `Medieval-Cookery-<version>.jar` from the [Releases](https://github.com/Dans-Plugins/Medieval-Cookery/releases) page.
2. Place the JAR in your server's `plugins/` folder.
3. Restart the server.

On the first startup a default `recipes.yml` is written to `plugins/MedievalCookery/`. Edit that file to change or add recipes, then restart the server for the changes to take effect. See [CONFIG.md](CONFIG.md) for the full field reference.

## Craftable Foods

All recipes use the standard 3×3 crafting table. In the patterns below, each row is one row of the grid and a `.` marks a slot that must be left empty. The `.` is a reading aid only — inside `recipes.yml` an empty slot is written as a space.

| Food | Pattern | Ingredients |
|------|---------|-------------|
| Salmon Roll | `KWK` / `WRW` / `KWK` | K = Dried Kelp, W = Wheat, R = Salmon |
| Beet Salad | `BBB` / `GGG` / `.W.` | B = Beetroot, G = Grass, W = Bowl |
| Bowl of Rice | `WWW` / `WWW` / `.B.` | W = Wheat, B = Bowl |
| Steak Sandwich | `.B.` / `.S.` / `.B.` | B = Bread, S = Cooked Beef |
| Salted Herring | `.B.` / `BCB` / `.B.` | B = Bone Meal, C = Cod |

## Eating a Food

Hold the food in your main hand and right-click. The foods are player heads, which vanilla Minecraft does not treat as edible, so the plugin handles the right-click itself: instead of placing the head as a block, it starts a meal.

- Eating takes 32 ticks — about 1.6 seconds — the same as a vanilla food, with the eating sound playing throughout.
- One food is then taken from your hand, a Saturation effect is applied for the number of ticks the recipe's `hungerDecrease` sets, and a message confirms the meal.
- If the recipe sets an `afterEatItem`, that item is added to your inventory. Beet Salad and Bowl of Rice return the Bowl used to craft them.
- Moving the food out of your main hand before the meal finishes cancels it, as does logging out and back in. No food is consumed and no effect is applied in that case.
- Right-clicking again while already eating does nothing; one meal has to finish before the next begins.

## Known Limitations

A food eaten from the off-hand is not recognised — only the main hand starts a meal.

## Support

Open a [GitHub issue](https://github.com/Dans-Plugins/Medieval-Cookery/issues) to report bugs or request features.
