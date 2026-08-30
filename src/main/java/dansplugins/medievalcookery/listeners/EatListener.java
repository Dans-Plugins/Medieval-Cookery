package dansplugins.medievalcookery.listeners;

import dansplugins.medievalcookery.CustomFoodItem;
import dansplugins.medievalcookery.DelayedExecution;
import dansplugins.medievalcookery.MedievalCookery;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

    private final MedievalCookery medievalCookery;

    public EatListener(MedievalCookery medievalCookery) {
        this.medievalCookery = medievalCookery;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!isEatingGesture(event.getAction()) || event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (isBlockInteractionInstead(event.getAction(), clickedType(event), event.getPlayer().isSneaking())) {
            return;
        }

        String foodName = CustomFoodItem.nameOf(event.getItem());
        if (foodName == null || !medievalCookery.hasRecipeName(foodName)) {
            return;
        }

        // Cancelled before the in-progress check, so that clicking again mid-meal does not place
        // the head as a block either.
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (medievalCookery.isPlayerEating(player)) {
            return;
        }

        medievalCookery.startPlayerEating(player, foodName);
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
