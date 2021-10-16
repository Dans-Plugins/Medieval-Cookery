package com.gmail.medievalcookery;

import com.gmail.medievalcookery.services.ConfigService;
import com.gmail.medievalcookery.services.StorageService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MedievalCookery extends JavaPlugin {

    private static MedievalCookery instance;
    private ConfigService configService;
    public static MedievalCookery getInstance() {
        return instance;
    }

    private List<CustomFoodRecipe> recipes = new ArrayList();

    public StorageService storage = new StorageService();

    public static String MetadataPrefix = "Cookery";
    public static String MetadataKeyIsEating = "IsEating";
    public static String MetadataKeyItemName = "ItemName";

    public static ItemStack createSpoiledFood(ItemStack item) {
        ItemStack spoiledFood = new ItemStack(Material.ROTTEN_FLESH);

        ItemMeta meta = spoiledFood.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(StorageService.spoiledFoodName);
            meta.setLore(Collections.singletonList(ChatColor.WHITE + StorageService.spoiledFoodLore));
        }

        spoiledFood.setItemMeta(meta);
        spoiledFood.setAmount(item.getAmount());

        return spoiledFood;
    }

    public void startPlayerEating(Player player, String itemName) {
        player.setMetadata(MetadataPrefix + MetadataKeyIsEating, new FixedMetadataValue(MedievalCookery.getInstance(), true));
        player.setMetadata(MetadataPrefix + MetadataKeyItemName, new FixedMetadataValue(MedievalCookery.getInstance(), itemName));
    }
    public void endPlayerEating(Player player) {
        player.setMetadata(MetadataPrefix + MetadataKeyIsEating, new FixedMetadataValue(MedievalCookery.getInstance(), false));
    }

    public String getPlayerEatingItemName(Player player) {
        if (player.hasMetadata(MetadataPrefix + MetadataKeyItemName))
        {
            List<MetadataValue> values = player.getMetadata(MetadataPrefix + MetadataKeyItemName);
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
        if (player.hasMetadata(MetadataPrefix + MetadataKeyIsEating))
        {
            List<MetadataValue> values = player.getMetadata(MetadataPrefix + MetadataKeyIsEating);
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

    @Override
    public void onEnable() {
        System.out.println(("--- Enabling Medieval-Cookery ------ "));

        instance = this;

        configService = new ConfigService();

        recipes = configService.loadRecipes();

        for (Player player : getServer().getWorld("world").getPlayers())
        {
            endPlayerEating(player);
        }

        getServer().getPluginManager().registerEvents(new EventHandlers(), this);

    }

    @Override
    public void onDisable() {
        System.out.println(("--- Disabling Medieval-Cookery --------"));
    }

}
