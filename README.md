# Medieval Cookery
This Minecraft plugin allows server owners to add cooking recipes for an enhanced roleplay experience. 

## Supported Minecraft Versions
This plugin is supported on the Minecraft versions listed in [`minecraft-versions.json`](minecraft-versions.json): currently **1.19.4**, **1.21.11** and **26.2** (Spigot and its forks). Every stable release is booted on a real server of each of these versions before it is published, and every build checks that the plugin only uses Bukkit API that exists on all of them. Other versions from 1.19.4 onwards are expected to work but are not tested. To support another version, add it to the file: both checks pick it up.

## Custom Recipes

Recipes are shaped, and use the symbols you define in the 'symbols' list of your recipe section.

- 'hungerDecrease' is the number of ticks that the Saturation potion effect lasts for once this item has been consumed.  The more filling the item, the longer the effect.

- 'afterEatItem' is the item the player ends up with after consuming the food item (for example: a wooden bowl used in the recipe ingredients can be given back)

- 'textureBase64' is the player head texture to use to represent this food item.  Examples can be found on the player head database under food and drink (https://minecraft-heads.com/player-heads/food-drinks).  Then search the corresponding username to find the uuid, and then paste that uuid at the end of this url: https://sessionserver.mojang.com/session/minecraft/profile/  The string inside the "value" key of the JSON returned from that URL is what you need to paste in this part of the recipe. (TODO: Automate this process with API calls to automatically retrieve this based on a player uuid)


## Works Well With
Medieval Cookery is part of two sets of Dan's Plugins: **medieval roleplay** and **survival flavour**. These are companion plugins that suit the same kind of server and run side by side; Medieval Cookery does not depend on or call into any of them.

Medieval roleplay:

- [Medieval Roleplay Engine](https://github.com/Dans-Plugins/Medieval-Roleplay-Engine) ([SpigotMC](https://www.spigotmc.org/resources/medieval-roleplay-engine.79993/), `/dpm get medievalroleplayengine`): character cards, local, global, whisper and yell chat, emotes, dice and messenger birds.
- [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions) ([SpigotMC](https://www.spigotmc.org/resources/medieval-factions.79941/), `/dpm get medievalfactions`): nation-like factions with land claims, diplomacy and laws. Its add-ons are listed in its [Expansions](https://github.com/Dans-Plugins/Medieval-Factions#expansions) section.
- [Mailboxes](https://github.com/Dans-Plugins/Mailboxes) ([SpigotMC](https://www.spigotmc.org/resources/mailboxes.96611/), `/dpm get mailboxes`): persistent mail between players, with item attachments.
- [Medieval Economy](https://github.com/Dans-Plugins/Medieval-Economy) ([SpigotMC](https://www.spigotmc.org/resources/medieval-economy.81836/), `/dpm get medievaleconomy`): a coinpurse and a physical currency item.
- [PlayerLore](https://github.com/Dans-Plugins/PlayerLore) ([SpigotMC](https://www.spigotmc.org/resources/playerlore.98602/), `/dpm get playerlore`): players write their own lore onto their items.
- [Conquest Recipes](https://github.com/Dans-Plugins/Conquest-Recipes) ([SpigotMC](https://www.spigotmc.org/resources/conquest-recipes.83594/), `/dpm get conquestrecipes`): recipes for historical weapons, armour and shields named to match the Conquest resource pack.

Survival flavour:

- [Food Spoilage](https://github.com/Dans-Plugins/FoodSpoilage) ([SpigotMC](https://www.spigotmc.org/resources/food-spoilage.81507/), `/dpm get foodspoilage`): food goes bad over time.
- [Wild Pets](https://github.com/Dans-Plugins/Wild-Pets) ([SpigotMC](https://www.spigotmc.org/resources/wild-pets.95800/), `/dpm get wildpets`): players tame any entity and keep it as a pet.
- [SimpleSkills](https://github.com/Dans-Plugins/SimpleSkills) ([SpigotMC](https://www.spigotmc.org/resources/simpleskills.98039/), `/dpm get simpleskills`): skills that level up as players play and unlock benefits.
- [More Recipes](https://github.com/Dans-Plugins/More-Recipes) ([SpigotMC](https://www.spigotmc.org/resources/more-recipes.81832/), `/dpm get morerecipes`): recipes for items that cannot be crafted in vanilla.

Every plugin above is listed on [dansplugins.com](https://dansplugins.com). Medieval Cookery is listed at [dansplugins.com/resources/medieval-cookery](https://dansplugins.com/resources/medieval-cookery); it has no stable release yet, so [Dan's Plugin Manager](https://github.com/Dans-Plugins/Dans-Plugin-Manager) cannot install it until one is published.

## Documentation

- [USER_GUIDE.md](USER_GUIDE.md) – installation, the craftable foods, and how to eat them
- [CONFIG.md](CONFIG.md) – `recipes.yml` location and field reference, and the `config.yml` usage-reporting switch
- [COMMANDS.md](COMMANDS.md) – command reference
- [CONTRIBUTING.md](CONTRIBUTING.md) – building the plugin and adding recipes
- [CHANGELOG.md](CHANGELOG.md) – release history

## Usage reporting

Usage reporting is on by default: when the plugin is enabled it sends its name and version (the plugin has no commands, so `startup` is its only event) to https://trace.danielstephenson.dev so it is known which plugins are actually in use. Nothing about players, worlds, IPs or the server is sent. The plugin says on every startup whether reporting is on. To turn it off:

- `usage-reporting.enabled: false` in this plugin's `config.yml`
- for every plugin on the server that reports to trace: `enabled: false` in `plugins/trace/config.yml` (written by the first such plugin to start)
- the environment variable `TRACE_USAGE_REPORTING=off` or `DO_NOT_TRACK=1`

Details: https://github.com/Stephenson-Software/trace#usage-reporting

## Adoption
This project was adopted by the Dan's Plugins Community on June 5th, 2022.
