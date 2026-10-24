package dansplugins.medievalcookery.listeners;

import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers which placements are refused. Whether an item is a food is read from its tag, which
 * needs a running server, so the check is supplied here, and the plugin's own is left to the
 * manual validation recorded on the pull request, as it is in {@code CustomFoodItemTest}.
 */
class PlaceListenerTest {

    private final ItemStack food = new ItemStack(Material.PLAYER_HEAD);
    private final PlaceListener listener = new PlaceListener(item -> item == food);

    /** The off hand is the route {@link EatListener} never sees, so it matters most here. */
    @ParameterizedTest
    @EnumSource(names = {"HAND", "OFF_HAND"})
    void aFoodIsNotPlacedFromEitherHand(EquipmentSlot hand) {
        BlockPlaceEvent event = placing(food, hand);
        listener.onBlockPlaceEvent(event);
        assertTrue(event.isCancelled());
    }

    @Test
    void anOrdinaryHeadIsStillPlaced() {
        BlockPlaceEvent event = placing(new ItemStack(Material.PLAYER_HEAD), EquipmentSlot.OFF_HAND);
        listener.onBlockPlaceEvent(event);
        assertFalse(event.isCancelled());
    }

    private static BlockPlaceEvent placing(ItemStack item, EquipmentSlot hand) {
        return new BlockPlaceEvent(null, null, null, item, null, true, hand);
    }
}
