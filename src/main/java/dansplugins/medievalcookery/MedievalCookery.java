package dansplugins.medievalcookery;

import dansplugins.medievalcookery.listeners.JoinListener;
import dansplugins.medievalcookery.services.ConfigService;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MedievalCookery extends JavaPlugin {
    private final String metadataPrefix = "Cookery";
    private final String metadataKeyIsEating = "IsEating";
    private final String metadataKeyItemName = "ItemName";
    private List<CustomFoodRecipe> recipes = new ArrayList<>();

    private final ConfigService configService = new ConfigService(this);

    @Override
    public void onEnable() {
        recipes = configService.loadRecipes();

        for (Player player : getServer().getWorld("world").getPlayers()) {
            endPlayerEating(player);
        }

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

    }

    @Override
    public void onDisable() {
        System.out.println(("--- Disabling Medieval-Cookery --------"));
    }

    public void startPlayerEating(Player player, String itemName) {
        player.setMetadata(metadataPrefix + metadataKeyIsEating, new FixedMetadataValue(this, true));
        player.setMetadata(metadataPrefix + metadataKeyItemName, new FixedMetadataValue(this, itemName));
    }
    
    public void endPlayerEating(Player player) {
        player.setMetadata(metadataPrefix + metadataKeyIsEating, new FixedMetadataValue(this, false));
    }

    public String getPlayerEatingItemName(Player player) {
        if (player.hasMetadata(metadataPrefix + metadataKeyItemName))
        {
            List<MetadataValue> values = player.getMetadata(metadataPrefix + metadataKeyItemName);
            for (MetadataValue v : values) {
                if (v.getOwningPlugin().getName().equalsIgnoreCase(getName())) {
                    try {
                        return v.asString();
                    } catch(Exception e) { }
                }
            }
        }
        return "";
    }

    public boolean isPlayerEating(Player player) {
        if (player.hasMetadata(metadataPrefix + metadataKeyIsEating))
        {
            List<MetadataValue> values = player.getMetadata(metadataPrefix + metadataKeyIsEating);
            for (MetadataValue v : values) {
                if (v.getOwningPlugin().getName().equalsIgnoreCase(getName())) {
                    if (v.asBoolean() == true) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasRecipeName(String name) {
        for (CustomFoodRecipe recipe : recipes) {
            if (recipe.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public CustomFoodRecipe getRecipeByName(String name) {
        for (CustomFoodRecipe recipe : recipes) {
            if (recipe.name.equalsIgnoreCase(name)) {
                return recipe;
            }
        }
        return null;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public String getMetadataKeyIsEating() {
        return metadataKeyIsEating;
    }

    public String getMetadataKeyItemName() {
        return metadataKeyItemName;
    }

}
