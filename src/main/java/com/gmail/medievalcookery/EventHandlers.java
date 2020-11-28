package com.gmail.medievalcookery;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Time;

public class EventHandlers implements Listener {



    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        ItemStack handItem = event.getPlayer().getInventory().getItemInMainHand();
        if (handItem != null && !handItem.getType().isAir()) {
            if (TimeStampSubsystem.timeStampAssigned(handItem)) {
                if (TimeStampSubsystem.timeReached(handItem)) {
                    ItemStack spoiledFood = MedievalCookery.createSpoiledFood(handItem);
                    event.getPlayer().getInventory().setItemInMainHand(spoiledFood);
                    event.setCancelled(true);
                } else{
                    String itemName = event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName();
                    if (itemName.equalsIgnoreCase("Salmon Roll")) {
                        MedievalCookery.getInstance().getServer().getWorld(event.getPlayer().getWorld().getName())
                                .playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                        event.getPlayer().setSaturation(event.getPlayer().getSaturation() + 0.1f);
                        event.setCancelled(true);
                        DelayedInventoryAdjustment.ConsumeItemInMainHand(event.getPlayer());
                    }
                }
            }
        }
    }
}
