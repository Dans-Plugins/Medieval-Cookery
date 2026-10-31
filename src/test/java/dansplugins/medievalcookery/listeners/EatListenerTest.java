package dansplugins.medievalcookery.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
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

    @ParameterizedTest
    @EnumSource(names = {"SURVIVAL", "ADVENTURE", "CREATIVE"})
    void aHungryPlayerCanEat(GameMode gameMode) {
        assertTrue(EatListener.canEat(19, gameMode));
        assertTrue(EatListener.canEat(0, gameMode));
    }

    /** The food would be taken while the Saturation effect could not raise the food level. */
    @ParameterizedTest
    @EnumSource(names = {"SURVIVAL", "ADVENTURE"})
    void aFullPlayerCannotEat(GameMode gameMode) {
        assertFalse(EatListener.canEat(20, gameMode));
    }

    @Test
    void aCreativePlayerCanEatWhenFull() {
        assertTrue(EatListener.canEat(20, GameMode.CREATIVE));
    }

    /**
     * A click in the air reaches the listener already cancelled, as there is no block to use.
     * Treating that as a denial would stop every meal begun in the air.
     */
    @Test
    void aClickInTheAirIsNotADenial() {
        PlayerInteractEvent event = clicking(Action.RIGHT_CLICK_AIR);
        assertTrue(event.isCancelled());
        assertFalse(EatListener.isItemUseDenied(event));
    }

    @ParameterizedTest
    @EnumSource(names = {"RIGHT_CLICK_AIR", "RIGHT_CLICK_BLOCK"})
    void aClickAnotherPluginCancelledIsADenial(Action action) {
        PlayerInteractEvent event = clicking(action);
        event.setCancelled(true);
        assertTrue(EatListener.isItemUseDenied(event));
    }

    @Test
    void aDeniedItemUseIsADenialEvenWithTheBlockAllowed() {
        PlayerInteractEvent event = clicking(Action.RIGHT_CLICK_BLOCK);
        event.setUseInteractedBlock(Event.Result.ALLOW);
        event.setUseItemInHand(Event.Result.DENY);
        assertTrue(EatListener.isItemUseDenied(event));
    }

    /** Events construct without a server; the player and block are not read by the check. */
    private static PlayerInteractEvent clicking(Action action) {
        return new PlayerInteractEvent(null, action, new ItemStack(Material.PLAYER_HEAD), null, null);
    }
}
