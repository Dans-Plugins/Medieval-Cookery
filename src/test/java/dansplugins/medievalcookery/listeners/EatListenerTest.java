package dansplugins.medievalcookery.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.CaveVines;
import org.bukkit.block.data.type.CaveVinesPlant;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertTrue(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false, false));
    }

    @ParameterizedTest
    @EnumSource(names = {"STONE", "DIRT", "OAK_PLANKS"})
    void anOrdinaryBlockDoesNotClaimTheRightClick(Material material) {
        assertFalse(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false, false));
    }

    /**
     * These report as interactable, but only react to particular items such as a lead or shears.
     * Were they to keep the click, the head would be refused placement and nothing would happen.
     */
    @ParameterizedTest
    @EnumSource(names = {
            "OAK_FENCE", "BAMBOO_FENCE", "NETHER_BRICK_FENCE", "PUMPKIN", "TNT", "CAULDRON",
            "WATER_CAULDRON", "LAVA_CAULDRON", "POWDER_SNOW_CAULDRON", "BEEHIVE", "BEE_NEST",
            "CAMPFIRE", "SOUL_CAMPFIRE", "IRON_DOOR", "IRON_TRAPDOOR", "CANDLE", "WHITE_CANDLE",
            "LIGHT_BLUE_CANDLE", "BLACK_CANDLE"})
    void aBlockWithNoUseForAFoodDoesNotClaimTheRightClick(Material material) {
        assertTrue(material.isInteractable());
        assertFalse(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false, false));
    }

    /** A candle cake is eaten as a cake, and a wooden trapdoor opens by hand, unlike an iron one. */
    @ParameterizedTest
    @EnumSource(names = {"CANDLE_CAKE", "WHITE_CANDLE_CAKE", "OAK_TRAPDOOR"})
    void aBlockBesideThePassingOnesKeepsItsOwnRightClick(Material material) {
        assertTrue(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false, false));
    }

    @ParameterizedTest
    @EnumSource(names = {"CANDLE", "WHITE_CANDLE", "BLACK_CANDLE"})
    void aCandleIsRecognised(Material material) {
        assertTrue(EatListener.isCandle(material));
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(names = {"CANDLE_CAKE", "RED_CANDLE_CAKE", "TORCH", "STONE"})
    void nothingElseIsACandle(Material material) {
        assertFalse(EatListener.isCandle(material));
    }

    /** The name match for fences must not reach a gate, which does open. */
    @ParameterizedTest
    @EnumSource(names = {"OAK_FENCE_GATE", "BAMBOO_FENCE_GATE"})
    void aFenceGateKeepsItsOwnRightClick(Material material) {
        assertTrue(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false, false));
    }

    @ParameterizedTest
    @EnumSource(names = {"OAK_FENCE", "NETHER_BRICK_FENCE"})
    void aFenceIsRecognised(Material material) {
        assertTrue(EatListener.isFence(material));
    }

    /** A click in the air has no block, and the listener asks about the lead only for a fence. */
    @ParameterizedTest
    @NullSource
    @EnumSource(names = {"OAK_FENCE_GATE", "PUMPKIN", "STONE"})
    void nothingElseIsAFence(Material material) {
        assertFalse(EatListener.isFence(material));
    }

    /**
     * Vanilla opens these only for a creative operator and passes anyone else's click on to the
     * held item, which would eat a vanilla food.
     */
    @ParameterizedTest
    @EnumSource(names = {
            "COMMAND_BLOCK", "CHAIN_COMMAND_BLOCK", "REPEATING_COMMAND_BLOCK", "STRUCTURE_BLOCK", "JIGSAW"})
    void aGameMasterBlockPassesTheClickOfAPlayerWhoCannotUseIt(Material material) {
        assertTrue(material.isInteractable());
        assertTrue(EatListener.isGameMasterBlock(material));
        assertFalse(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false, false));
    }

    @ParameterizedTest
    @EnumSource(names = {
            "COMMAND_BLOCK", "CHAIN_COMMAND_BLOCK", "REPEATING_COMMAND_BLOCK", "STRUCTURE_BLOCK", "JIGSAW"})
    void aGameMasterBlockKeepsTheClickOfAPlayerWhoCanUseIt(Material material) {
        assertTrue(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false, true));
    }

    /** Being able to open a command block changes nothing about any other block. */
    @ParameterizedTest
    @EnumSource(names = {"CHEST", "OAK_DOOR", "STONE", "OAK_FENCE", "CANDLE"})
    void gameMasterAccessLeavesOtherBlocksAlone(Material material) {
        assertFalse(EatListener.isGameMasterBlock(material));
        assertEquals(
                EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false, false),
                EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false, true));
    }

    @Test
    void onlyACreativeOperatorCanUseGameMasterBlocks() {
        assertTrue(EatListener.canUseGameMasterBlocks(GameMode.CREATIVE, true));
        assertFalse(EatListener.canUseGameMasterBlocks(GameMode.CREATIVE, false));
        assertFalse(EatListener.canUseGameMasterBlocks(GameMode.SURVIVAL, true));
        assertFalse(EatListener.canUseGameMasterBlocks(GameMode.ADVENTURE, true));
        assertFalse(EatListener.canUseGameMasterBlocks(GameMode.SPECTATOR, true));
    }

    /**
     * These report as interactable in every state, but in the states below they have no use for a
     * food, so vanilla passes the click on to the held item.
     */
    @ParameterizedTest
    @EnumSource(names = {"RESPAWN_ANCHOR", "SWEET_BERRY_BUSH", "CAVE_VINES", "CAVE_VINES_PLANT"})
    void aStateDependentBlockClaimsTheClickByMaterialAlone(Material material) {
        assertTrue(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, material, false, false));
    }

    @Test
    void anUnchargedRespawnAnchorPassesTheClick() {
        assertTrue(EatListener.passesFoodClickInItsState(respawnAnchor(0)));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4})
    void aChargedRespawnAnchorKeepsTheClick(int charges) {
        assertFalse(EatListener.passesFoodClickInItsState(respawnAnchor(charges)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void anUnripeBerryBushPassesTheClick(int age) {
        assertTrue(EatListener.passesFoodClickInItsState(aged(Material.SWEET_BERRY_BUSH, age)));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    void aRipeBerryBushKeepsTheClick(int age) {
        assertFalse(EatListener.passesFoodClickInItsState(aged(Material.SWEET_BERRY_BUSH, age)));
    }

    /** {@code CAVE_VINES} is both ageable and berried; the berries alone decide. */
    @ParameterizedTest
    @ValueSource(classes = {CaveVines.class, CaveVinesPlant.class})
    void caveVinesPassTheClickOnlyWithoutBerries(Class<? extends CaveVinesPlant> type) {
        assertTrue(EatListener.passesFoodClickInItsState(caveVines(type, false)));
        assertFalse(EatListener.passesFoodClickInItsState(caveVines(type, true)));
    }

    /** Only the berry bush is judged by its age; a young crop is no reason to start a meal. */
    @Test
    void anotherAgeableBlockIsNotJudgedByItsAge() {
        assertFalse(EatListener.passesFoodClickInItsState(aged(Material.WHEAT, 0)));
    }

    @Test
    void noBlockPassesNothingOn() {
        assertFalse(EatListener.passesFoodClickInItsState(null));
    }

    @Test
    void sneakingSuppressesTheBlockAndAllowsTheMeal() {
        assertFalse(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_BLOCK, Material.CHEST, true, false));
    }

    @Test
    void clickingAirIsNeverABlockInteraction() {
        assertFalse(EatListener.isBlockInteractionInstead(Action.RIGHT_CLICK_AIR, null, false, false));
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

    /** A spectator keeps their inventory and hunger bar but can use no item in vanilla. */
    @ParameterizedTest
    @ValueSource(ints = {0, 19, 20})
    void aSpectatorCannotEatAtAnyFoodLevel(int foodLevel) {
        assertFalse(EatListener.canEat(foodLevel, GameMode.SPECTATOR));
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

    private static RespawnAnchor respawnAnchor(int charges) {
        return blockData(RespawnAnchor.class, Material.RESPAWN_ANCHOR, "getCharges", charges);
    }

    private static Ageable aged(Material material, int age) {
        return blockData(Ageable.class, material, "getAge", age);
    }

    private static CaveVinesPlant caveVines(Class<? extends CaveVinesPlant> type, boolean berries) {
        return blockData(type, Material.CAVE_VINES, "isBerries", berries);
    }

    /**
     * Block data needs a server to create, so a proxy stands in for it, answering its material and
     * the one property the check reads.
     */
    private static <T extends BlockData> T blockData(Class<T> type, Material material, String property, Object value) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if (method.getName().equals(property)) {
                return value;
            }
            if (method.getName().equals("getMaterial")) {
                return material;
            }
            throw new UnsupportedOperationException(method.getName());
        }));
    }

    /** Events construct without a server; the player and block are not read by the check. */
    private static PlayerInteractEvent clicking(Action action) {
        return new PlayerInteractEvent(null, action, new ItemStack(Material.PLAYER_HEAD), null, null);
    }
}
