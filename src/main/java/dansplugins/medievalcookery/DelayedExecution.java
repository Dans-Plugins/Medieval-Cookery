package dansplugins.medievalcookery;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class DelayedExecution {
    private final MedievalCookery medievalCookery;

    public DelayedExecution(MedievalCookery medievalCookery) {
        this.medievalCookery = medievalCookery;
    }

    public BukkitTask PlayEatingSound(final Player eventPlayer) {
        return Bukkit.getScheduler().runTaskTimer(medievalCookery,
                new Runnable() {
                    @Override
                    public void run() {
                        medievalCookery.getServer().getWorld(eventPlayer.getWorld().getName())
                                .playSound(eventPlayer.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
                    }
                }, 0, 5);
    }

    public void ConsumeItemInMainHand(final Player eventPlayer, final long duration, final BukkitTask task) {
        Bukkit.getScheduler().runTaskLater(medievalCookery,
                new Runnable() {
                    @Override
                    public void run() {
                        // TODO Measure quality somehow (maybe using % time to best before date?)
                        // TODO Variable hunger bars based on quality added to hungerDecrease.
                        // TODO Optional "seasoning" or "spice" ingredients included in the recipe
                        // which would be read out after you consume the food item (instead of just "it was delicious"
                        // it would say "it tastes like ..." and list some (if not all) of the ingredients.
                        try {
                            finishEating(eventPlayer);
                        } finally {
                            // The eating sound repeats until it is stopped, so it is stopped even
                            // if finishing the meal failed.
                            task.cancel();
                        }
                    }
                }, duration);
    }

    /**
     * Completes one eating session: the food leaves the player's hand and its effects are applied.
     *
     * Nothing is applied if the meal was interrupted — a player who logs out and back in, or who
     * moves the food out of their main hand before the last bite, does not get to keep it. The
     * eating state is cleared either way, so an interrupted meal cannot block the next one.
     */
    void finishEating(Player player) {
        if (!medievalCookery.isPlayerEating(player)) {
            return;
        }
        String itemName = medievalCookery.getPlayerEatingItemName(player);
        medievalCookery.endPlayerEating(player);

        CustomFoodRecipe recipe = medievalCookery.getRecipeByName(itemName);
        if (recipe == null) {
            return;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (!itemName.equals(CustomFoodItem.nameOf(inHand))) {
            return;
        }
        if (inHand.getAmount() > 1) {
            inHand.setAmount(inHand.getAmount() - 1);
            player.getInventory().setItemInMainHand(inHand);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, recipe.hungerDecrease, 0));
        player.sendMessage(ChatColor.GRAY + "You ate a " + itemName + ", it was delicious.");
        if (recipe.afterEatItem != null) {
            player.getInventory().addItem(new ItemStack(recipe.afterEatItem, 1));
        }
    }
}