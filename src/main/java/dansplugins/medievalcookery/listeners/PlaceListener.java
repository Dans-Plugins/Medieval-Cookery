package dansplugins.medievalcookery.listeners;

import dansplugins.medievalcookery.MedievalCookery;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

/**
 * Stops one of the plugin's foods from being placed as a block.
 *
 * {@link EatListener} cancels the right-click that begins a meal, but leaves alone a food held in
 * the off hand, and a click on a block with a use of its own, which can still pass the click on
 * to the item: a fence does unless a lead is involved. Vanilla places the head in either case.
 * A placed head does not keep the recipe tag the food is recognised by, so the head that drops
 * when the block is broken is no longer that food. Every route to placing one ends here.
 */
public class PlaceListener implements Listener {

    private final Predicate<ItemStack> isFood;

    public PlaceListener(MedievalCookery medievalCookery) {
        this(item -> medievalCookery.recipeOf(item) != null);
    }

    /**
     * Takes the food check as a parameter, because the plugin's own needs a running server to
     * read an item's tag.
     */
    PlaceListener(Predicate<ItemStack> isFood) {
        this.isFood = isFood;
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (isFood.test(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }
}
