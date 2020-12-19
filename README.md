# MedievalCookery

Bukkit plugin for Minecraft servers to add cooking recipes for enhanced roleplay experience.

## Custom Recipes

Recipes are shaped, and use the symbols you define in the 'symbols' list of your recipe section.

- 'hungerDecrease' is the number of ticks to increase the hunger bar for before you've finished consuming this item.  The more filling the item, the more time it takes to consume.

- 'afterEatItem' is the item the player ends up with after consuming the food item (for example: a wooden bowl used in the recipe ingredients can be given back)

- 'textureBase64' is the player head texture to use to represent this food item.  Examples can be found on the player head database under food and drink (https://minecraft-heads.com/player-heads/food-drinks).  Then search the corresponding username to find the uuid, and then paste that uuid at the end of this url: https://sessionserver.mojang.com/session/minecraft/profile/  The string inside the "value" key of the JSON returned from that URL is what you need to paste in this part of the recipe. (TODO: Automate this process with API calls to automatically retrieve this based on a player uuid)

