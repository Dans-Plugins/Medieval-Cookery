package com.gmail.medievalcookery;

import org.bukkit.Sound;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

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
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        MedievalCookery.getInstance().endPlayerEating(event.getPlayer());
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
    public void onBlockPlace(BlockPlaceEvent event) {

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        TimeStampSubsystem timeStamps = new TimeStampSubsystem();
        ItemStack handItem = event.getPlayer().getInventory().getItemInMainHand();
        if (handItem != null && !handItem.getType().isAir() && !MedievalCookery.getInstance().isPlayerEating(event.getPlayer())) {
            String itemName = handItem.getItemMeta().getDisplayName();
            if (MedievalCookery.getInstance().hasRecipeName(itemName)) {
                if (timeStamps.timeStampAssigned(handItem)) {
                    if (timeStamps.timeReached(handItem)) {
                        ItemStack spoiledFood = MedievalCookery.createSpoiledFood(handItem);
                        event.getPlayer().getInventory().setItemInMainHand(spoiledFood);
                        event.setCancelled(true);
                    } else {
                        event.setCancelled(true);
                        MedievalCookery.getInstance().startPlayerEating(event.getPlayer(), itemName);
                        if (event.getPlayer().getInventory().getItemInMainHand().getAmount() > 1) {
                            event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                        } else {
                            event.getPlayer().getInventory().setItemInMainHand(null);
                        }
                        DelayedExecution.ConsumeItemInMainHand(event.getPlayer(), 40, DelayedExecution.PlayEatingSound(event.getPlayer()));
                    }
                }
            }
        }
    }
}
