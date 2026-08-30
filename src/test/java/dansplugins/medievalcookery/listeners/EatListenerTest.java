package dansplugins.medievalcookery.listeners;

import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers which interactions begin a meal. The listener method itself needs a PlayerInteractEvent
 * over a live player and so is left to the manual validation recorded on the pull request; the
 * gesture check is the part that decides whether an ordinary click costs a player their food.
 */
class EatListenerTest {

    @ParameterizedTest
    @EnumSource(names = {"RIGHT_CLICK_AIR", "RIGHT_CLICK_BLOCK"})
    void aRightClickBeginsAMeal(Action action) {
        assertTrue(EatListener.isEatingGesture(action));
    }

    @ParameterizedTest
    @EnumSource(names = {"LEFT_CLICK_AIR", "LEFT_CLICK_BLOCK", "PHYSICAL"})
    void nothingElseBeginsAMeal(Action action) {
        assertFalse(EatListener.isEatingGesture(action));
    }

    /**
     * A player carrying a food must still be able to use the world around them: were the click
     * taken for a meal, every container, door and workstation would stop responding while a food
     * was held.
     */
    @ParameterizedTest
    @EnumSource(names = {"CHEST", "OAK_DOOR", "CRAFTING_TABLE", "FURNACE", "LEVER"})
    void anInteractableBlockKeepsItsOwnRightClick(Material material) {
        assertTrue(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false));
    }

    @ParameterizedTest
    @EnumSource(names = {"STONE", "DIRT", "OAK_PLANKS"})
    void anOrdinaryBlockDoesNotClaimTheRightClick(Material material) {
        assertFalse(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false));
    }

    @Test
    void sneakingSuppressesTheBlockAndAllowsTheMeal() {
        assertFalse(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, Material.CHEST, true));
    }

    @Test
    void clickingAirIsNeverABlockInteraction() {
        assertFalse(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_AIR, null, false));
    }
}
