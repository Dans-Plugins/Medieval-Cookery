package com.gmail.medievalcookery;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Time;

public class EventHandlers implements Listener {

    @EventHandler
    public void onPlayerCraftEvent(CraftItemEvent event) {
        ItemStack item = event.getCurrentItem();
        String itemName = item.getItemMeta().getDisplayName();
        if (MedievalCookery.getInstance().hasRecipeName(itemName)) {
            int time = StorageSubsystem.getSpoilTime(itemName);
            if (time != 0) {
                event.setCurrentItem(TimeStampSubsystem.assignTimeStamp(item, time));
            }
        }
    }

    @EventHandler
    public void onItemSpawnEvent(ItemSpawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        String itemName = item.getItemMeta().getDisplayName();
        if (MedievalCookery.getInstance().hasRecipeName(itemName)) {
            int time = MedievalCookery.getInstance().storage.getSpoilTime(itemName);

            // if timestamp not already assigned
            if (time != 0 && !TimeStampSubsystem.timeStampAssigned(item)) {
                event.getEntity().setItemStack(TimeStampSubsystem.assignTimeStamp(item, time));
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        TimeStampSubsystem timeStamps = new TimeStampSubsystem();
        ItemStack handItem = event.getPlayer().getInventory().getItemInMainHand();
        if (handItem != null && !handItem.getType().isAir()) {
            String itemName = handItem.getItemMeta().getDisplayName();
            if (MedievalCookery.getInstance().hasRecipeName(itemName)) {
                if (timeStamps.timeStampAssigned(handItem)) {
                    if (timeStamps.timeReached(handItem)) {
                        ItemStack spoiledFood = MedievalCookery.createSpoiledFood(handItem);
                        event.getPlayer().getInventory().setItemInMainHand(spoiledFood);
                        event.setCancelled(true);
                    } else {
                        CustomFoodRecipe recipe = MedievalCookery.getInstance().getRecipeByName(itemName);
                        MedievalCookery.getInstance().getServer().getWorld(event.getPlayer().getWorld().getName())
                                .playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                        event.getPlayer().setSaturation(event.getPlayer().getSaturation() + recipe.satiationIncrease);
                        event.getPlayer().setSaturation(event.getPlayer().getFoodLevel() + recipe.hungerDecrease);
                        event.setCancelled(true);
                        DelayedInventoryAdjustment.ConsumeItemInMainHand(event.getPlayer());
                    }
                }
            }
        }
    }
}
