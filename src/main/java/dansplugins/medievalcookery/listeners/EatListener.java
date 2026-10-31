package dansplugins.medievalcookery.listeners;

import dansplugins.medievalcookery.CustomFoodRecipe;
import dansplugins.medievalcookery.DelayedExecution;
import dansplugins.medievalcookery.MedievalCookery;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;

/**
 * Starts an eating session when a player right-clicks while holding one of the plugin's foods.
 *
 * The foods are player heads, which vanilla Minecraft does not treat as edible, so no consumption
 * event is ever fired for them: right-clicking one either does nothing or places it as a block.
 * The interaction is therefore cancelled and replaced with the eating flow, which
 * {@link DelayedExecution} completes once the player has held still long enough.
 */
public class EatListener implements Listener {

    /** Vanilla takes 32 ticks to finish eating a food item, and the custom foods take the same. */
    public static final int EATING_DURATION_TICKS = 32;

    /** A full hunger bar, at which vanilla does not let a player eat an ordinary food. */
    static final int MAX_FOOD_LEVEL = 20;

    private final MedievalCookery medievalCookery;

    public EatListener(MedievalCookery medievalCookery) {
        this.medievalCookery = medievalCookery;
    }

    /**
     * Runs at HIGH priority so that a plugin denying the click at NORMAL or below has done so
     * before a meal is decided on; see {@link #isItemUseDenied}.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!isEatingGesture(event.getAction()) || event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (isItemUseDenied(event)) {
            return;
        }
        if (isBlockInteractionInstead(event.getAction(), clickedType(event), event.getPlayer().isSneaking())) {
            return;
        }

        CustomFoodRecipe recipe = medievalCookery.recipeOf(event.getItem());
        if (recipe == null) {
            return;
        }

        // Cancelled before the in-progress check, so that clicking again mid-meal does not place
        // the head as a block either.
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (medievalCookery.isPlayerEating(player)) {
            return;
        }
        if (!canEat(player.getFoodLevel(), player.getGameMode())) {
            return;
        }

        medievalCookery.startPlayerEating(player, recipe.key);
        DelayedExecution delayedExecution = new DelayedExecution(medievalCookery);
        BukkitTask eatingSound = delayedExecution.PlayEatingSound(player);
        delayedExecution.ConsumeItemInMainHand(player, EATING_DURATION_TICKS, eatingSound);
    }

    /**
     * Only a right-click begins a meal. A left-click is an attack or a block break, and PHYSICAL
     * is a pressure plate or tripwire being stepped on, neither of which should cost the player
     * the food they happen to be carrying.
     */
    static boolean isEatingGesture(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }

    /**
     * Reports whether another plugin has denied the use of the held item for this click.
     *
     * A protection, lobby or jail plugin usually cancels the event, which sets
     * {@code useItemInHand} to DENY, and the server then does not eat a vanilla food either.
     * {@code isCancelled()} cannot be used instead: a right-click in the air reaches every
     * listener already cancelled, because there is no block to use, while the item may still be.
     */
    static boolean isItemUseDenied(PlayerInteractEvent event) {
        return event.useItemInHand() == Event.Result.DENY;
    }

    /**
     * Decides whether a player is hungry enough to begin a meal.
     *
     * Vanilla only starts eating an ordinary food when the hunger bar is below full, or when the
     * player is in creative mode. A full player's right-click would otherwise cost them the food,
     * since the Saturation effect cannot raise a food level that is already at its maximum.
     */
    static boolean canEat(int foodLevel, GameMode gameMode) {
        return foodLevel < MAX_FOOD_LEVEL || gameMode == GameMode.CREATIVE;
    }

    /**
     * Reports whether the clicked block would have claimed this right-click for itself.
     *
     * Vanilla gives a block's own interaction priority over the item being held, so opening a
     * chest while carrying food opens the chest rather than eating. Cancelling the event here
     * regardless would make every container, door and workstation unusable to a player carrying
     * a food. Sneaking suppresses the block's interaction in vanilla, and so does not stop a meal.
     */
    static boolean isBlockInteractionInstead(Action action, Material clickedBlockType, boolean sneaking) {
        return action == Action.RIGHT_CLICK_BLOCK
                && !sneaking
                && clickedBlockType != null
                && clickedBlockType.isInteractable();
    }

    private static Material clickedType(PlayerInteractEvent event) {
        return event.getClickedBlock() == null ? null : event.getClickedBlock().getType();
    }
}
