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
        final Player player = eventPlayer;
        Bukkit.getScheduler().runTaskLater(medievalCookery,
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // TODO Measure quality somehow (maybe using % time to best before date?)
                            // TODO Variable hunger bars based on quality added to hungerDecrease.
                            // TODO Optional "seasoning" or "spice" ingredients included in the recipe
                            // which would be read out after you consume the food item (instead of just "it was delicious"
                            // it would say "it tastes like ..." and list some (if not all) of the ingredients.
                            String itemName = medievalCookery.getPlayerEatingItemName(eventPlayer);
                            CustomFoodRecipe recipe = medievalCookery.getRecipeByName(itemName);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, recipe.hungerDecrease, 0));
                            player.sendMessage(ChatColor.GRAY + "You ate a " + itemName + ", it was delicious.");
                            medievalCookery.endPlayerEating(eventPlayer);
                            player.getInventory().addItem(new ItemStack(recipe.afterEatItem, 1));
                        } catch (Exception e) {

                        }
                        task.cancel();
                    }
                }, duration);
    }
}