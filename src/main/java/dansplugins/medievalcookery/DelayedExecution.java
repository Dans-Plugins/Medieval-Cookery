package dansplugins.medievalcookery;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

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
     * Nothing is applied if the meal was interrupted: a player who moves the food out of their
     * main hand before the last bite does not get to keep it, a player who has died in the
     * meantime does not eat it (see {@link #isFoodToBeEaten}), and a meal already ended by some
     * other route is not completed a second time — {@code JoinListener} ends one on rejoin. The
     * eating state is cleared either way, so an interrupted meal cannot block the next one. The
     * exception is a player who quits before the last bite, who is left alone entirely and whose
     * eating state is cleared when they rejoin; see {@link #isMealToFinish}.
     */
    void finishEating(Player player) {
        if (!isMealToFinish(player.isOnline(), medievalCookery.isPlayerEating(player))) {
            return;
        }
        String recipeId = medievalCookery.getPlayerEatingRecipeId(player);
        medievalCookery.endPlayerEating(player);

        CustomFoodRecipe recipe = medievalCookery.getRecipeById(recipeId);
        if (recipe == null) {
            return;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (!isFoodToBeEaten(player.isDead(), medievalCookery.recipeOf(inHand) == recipe)) {
            return;
        }
        if (inHand.getAmount() > 1) {
            inHand.setAmount(inHand.getAmount() - 1);
            player.getInventory().setItemInMainHand(inHand);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, recipe.hungerDecrease, 0));
        player.sendMessage(ChatColor.GRAY + mealMessage(recipe.name));
        if (recipe.afterEatItem != null) {
            giveOrDrop(player.getInventory(), new ItemStack(recipe.afterEatItem, 1),
                    leftover -> player.getWorld().dropItem(player.getLocation(), leftover));
        }
    }

    /**
     * Decides whether the task that ends a meal should complete it.
     *
     * A meal already ended by some other route is not completed. Neither is one whose player has
     * quit: the task still holds their old {@code Player}, whose data was saved when they left,
     * so taking the food from it would not stick, while an after-eat item that did not fit would
     * be dropped into the world for real. The eating state such a player leaves behind is cleared
     * by {@code JoinListener} when they return.
     */
    static boolean isMealToFinish(boolean online, boolean eating) {
        return online && eating;
    }

    /**
     * Decides whether a meal that is being finished takes the food and applies its effects.
     *
     * The food has to still be in the main hand. The player also has to be alive. With
     * keepInventory on, the food stays in the hand of a player who dies mid-meal, and eating it on
     * the death screen would take the food and give nothing back, because potion effects do not
     * survive the respawn. The eating state has been cleared by this point either way, so the
     * player can eat again after respawning.
     */
    static boolean isFoodToBeEaten(boolean dead, boolean stillHeld) {
        return !dead && stillHeld;
    }

    /**
     * Puts an item in an inventory, handing whatever does not fit to {@code drop}.
     *
     * {@code addItem} hands back what it could not place rather than dropping it, so with a full
     * inventory the item would otherwise be lost. That is the usual case for an
     * {@code afterEatItem} when more than one food is held, since the food's slot stays occupied.
     * Vanilla drops the container in the same situation, as it does a honey bottle's glass bottle.
     */
    static void giveOrDrop(Inventory inventory, ItemStack item, Consumer<ItemStack> drop) {
        for (ItemStack leftover : inventory.addItem(item).values()) {
            drop.accept(leftover);
        }
    }

    /**
     * The message that confirms a finished meal.
     *
     * The article agrees with the first visible letter of the name, so a recipe a server owner
     * names {@code Apple Pie} reads "an Apple Pie". A colour code in front of the name is skipped
     * over, since it is not shown. Going by the letter rather than the sound gets a name such as
     * {@code Unicorn Stew} wrong; getting it right would take a pronunciation dictionary.
     */
    static String mealMessage(String foodName) {
        String visible = ChatColor.stripColor(foodName);
        boolean vowel = !visible.isEmpty() && "AEIOUaeiou".indexOf(visible.charAt(0)) >= 0;
        return "You ate " + (vowel ? "an " : "a ") + foodName + ", it was delicious.";
    }
}