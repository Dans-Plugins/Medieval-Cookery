package com.gmail.medievalcookery;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;

public class MedievalCookery extends JavaPlugin {

    private static JavaPlugin instance;
    public static JavaPlugin getInstance() {
        return instance;
    }

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

    @Override
    public void onEnable() {
        System.out.println(("--- Enabling Medieval-Cookery ------ "));

        instance = this;

        CustomFoodRecipe recipe = new CustomFoodRecipe(this, "salmon_roll", "Salmon Roll",
                new String[] {"KWK", "WRW", "KWK"},
                MaterialMap(new String[] { "K", "R", "W" },
                        new Material[] { Material.DRIED_KELP, Material.SALMON, Material.WHEAT }));

        getServer().getPluginManager().registerEvents(new EventHandlers(), this);
    }

    private HashMap<String, Material> MaterialMap(String[] keys, Material[] values) {
        HashMap<String, Material> map = new HashMap<String, Material>();
        int i = 0;
        for(String key : keys) {
            map.put(key, values[i]);
            i++;
        }
        return map;
    }

    @Override
    public void onDisable() {
        System.out.println(("--- Disabling Medieval-Cookery --------"));
    }

}
