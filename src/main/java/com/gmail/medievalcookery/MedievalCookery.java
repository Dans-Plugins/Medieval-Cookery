package com.gmail.medievalcookery;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MedievalCookery extends JavaPlugin {

    private static MedievalCookery instance;
    private ConfigManager configManager;
    public static MedievalCookery getInstance() {
        return instance;
    }

    private List<CustomFoodRecipe> recipes = new ArrayList();

    public StorageSubsystem storage = new StorageSubsystem();

    public static ItemStack createSpoiledFood(ItemStack item) {
        ItemStack spoiledFood = new ItemStack(Material.ROTTEN_FLESH);

        ItemMeta meta = spoiledFood.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(StorageSubsystem.spoiledFoodName);
            meta.setLore(Collections.singletonList(ChatColor.WHITE + StorageSubsystem.spoiledFoodLore));
        }

        spoiledFood.setItemMeta(meta);
        spoiledFood.setAmount(item.getAmount());

        return spoiledFood;
    }

    public boolean hasRecipeName(String name) {
        System.out.println(name);
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

        configManager = new ConfigManager();

        recipes = configManager.loadRecipes();

        getServer().getPluginManager().registerEvents(new EventHandlers(), this);

    }

    @Override
    public void onDisable() {
        System.out.println(("--- Disabling Medieval-Cookery --------"));
    }

}
