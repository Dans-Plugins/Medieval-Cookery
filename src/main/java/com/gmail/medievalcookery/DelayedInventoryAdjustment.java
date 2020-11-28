package com.gmail.medievalcookery;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DelayedInventoryAdjustment {
    public static void ConsumeItemInMainHand(Player eventPlayer) {
        final Player player = eventPlayer;
        Bukkit.getScheduler().runTaskLater(MedievalCookery.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    if (player.getInventory().getItemInMainHand() != null) {
                        player.sendMessage(ChatColor.GRAY + "You ate a " + player.getInventory().getItemInMainHand().getItemMeta().getDisplayName() + ", it was delicious.");
                        if (player.getInventory().getItemInMainHand().getAmount() > 1) {
                            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        } else {
                            player.getInventory().setItemInMainHand(null);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Exception while consuming item in main hand: " + e.getMessage());
                }
            }
        }, 20);
    }
}