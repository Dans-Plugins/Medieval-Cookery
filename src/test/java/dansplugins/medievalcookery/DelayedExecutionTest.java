package dansplugins.medievalcookery;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the message that confirms a finished meal, how the after-eat item is handed back, and
 * which meals are completed at all. Finishing the meal itself needs a live player and so is left
 * to the manual validation recorded on the pull request.
 */
class DelayedExecutionTest {

    @ParameterizedTest
    @ValueSource(strings = {"Apple Pie", "Eel Pie", "Iced Bun", "Onion Soup", "Udon", "apple pie"})
    void aNameStartingWithAVowelTakesAn(String name) {
        assertEquals("You ate an " + name + ", it was delicious.", DelayedExecution.mealMessage(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Salmon Roll", "Beet Salad", "Honey Cake", "Yam Stew"})
    void aNameStartingWithAConsonantTakesA(String name) {
        assertEquals("You ate a " + name + ", it was delicious.", DelayedExecution.mealMessage(name));
    }

    /** A colour code is not shown, so the article follows the letter after it. */
    @Test
    void aColourCodeInFrontOfTheNameIsSkipped() {
        assertEquals("You ate an §6Apple Pie, it was delicious.", DelayedExecution.mealMessage("§6Apple Pie"));
        assertEquals("You ate a §aBeet Salad, it was delicious.", DelayedExecution.mealMessage("§aBeet Salad"));
    }

    /** The bundled foods all start with a consonant, so what their eaters are told is unchanged. */
    @Test
    void theBundledFoodsKeepTheirMessage() {
        YamlConfiguration recipes = bundledRecipes();
        for (String id : recipes.getConfigurationSection("recipes").getKeys(false)) {
            String name = recipes.getString("recipes." + id + ".name");
            assertTrue(DelayedExecution.mealMessage(name).startsWith("You ate a " + name), name);
        }
    }

    /** With no room left, the bundled Beet Salad's Bowl is dropped instead of vanishing. */
    @Test
    void anAfterEatItemThatDoesNotFitIsDropped() {
        ItemStack bowl = bundledAfterEatItem("beet_salad");
        List<ItemStack> added = new ArrayList<>();
        List<ItemStack> dropped = new ArrayList<>();

        DelayedExecution.giveOrDrop(inventoryWithNoRoom(added), bowl, dropped::add);

        assertEquals(List.of(bowl), added);
        assertEquals(List.of(bowl), dropped, "the item that did not fit was lost");
    }

    @Test
    void anAfterEatItemThatFitsIsNotDropped() {
        ItemStack bowl = bundledAfterEatItem("rice_bowl");
        List<ItemStack> added = new ArrayList<>();
        List<ItemStack> dropped = new ArrayList<>();

        DelayedExecution.giveOrDrop(inventoryWithRoom(added), bowl, dropped::add);

        assertEquals(List.of(bowl), added);
        assertTrue(dropped.isEmpty(), "an item that fit was dropped as well");
    }

    /** A player who quits mid-meal is not fed, and nothing is dropped where they logged out. */
    @Test
    void aMealIsNotFinishedForAPlayerWhoHasQuit() {
        assertFalse(DelayedExecution.isMealToFinish(false, true));
    }

    @Test
    void aMealIsFinishedForAPlayerStillEating() {
        assertTrue(DelayedExecution.isMealToFinish(true, true));
    }

    /** A meal already ended, by a rejoin say, is not completed a second time. */
    @Test
    void aMealAlreadyEndedIsNotFinished() {
        assertFalse(DelayedExecution.isMealToFinish(true, false));
        assertFalse(DelayedExecution.isMealToFinish(false, false));
    }

    /** With keepInventory on, the food is still in the hand of a player who died mid-meal. */
    @Test
    void aPlayerWhoDiedMidMealDoesNotEatTheFood() {
        assertFalse(DelayedExecution.isFoodToBeEaten(true, true));
    }

    @Test
    void aLivingPlayerStillHoldingTheFoodEatsIt() {
        assertTrue(DelayedExecution.isFoodToBeEaten(false, true));
    }

    @Test
    void aFoodMovedOutOfTheHandIsNotEaten() {
        assertFalse(DelayedExecution.isFoodToBeEaten(false, false));
        assertFalse(DelayedExecution.isFoodToBeEaten(true, false));
    }

    private static ItemStack bundledAfterEatItem(String recipeId) {
        String material = bundledRecipes().getString("recipes." + recipeId + ".afterEatItem");
        assertNotNull(material, recipeId + " has no afterEatItem in recipes.yml");
        return new ItemStack(Material.getMaterial(material), 1);
    }

    /**
     * Inventories that record what is added to them and hand back the leftovers the way
     * {@code addItem} does. The real ones only exist while a server is running.
     */
    private static Inventory inventoryWithNoRoom(List<ItemStack> added) {
        return inventory(added, false);
    }

    private static Inventory inventoryWithRoom(List<ItemStack> added) {
        return inventory(added, true);
    }

    private static Inventory inventory(List<ItemStack> added, boolean hasRoom) {
        return (Inventory) Proxy.newProxyInstance(Inventory.class.getClassLoader(), new Class<?>[]{Inventory.class},
                (proxy, method, args) -> {
                    if (!method.getName().equals("addItem")) {
                        throw new UnsupportedOperationException(method.getName());
                    }
                    ItemStack[] items = (ItemStack[]) args[0];
                    HashMap<Integer, ItemStack> leftovers = new HashMap<>();
                    for (int i = 0; i < items.length; i++) {
                        added.add(items[i]);
                        if (!hasRoom) {
                            leftovers.put(i, items[i]);
                        }
                    }
                    return leftovers;
                });
    }

    private static YamlConfiguration bundledRecipes() {
        InputStream in = DelayedExecutionTest.class.getClassLoader().getResourceAsStream("recipes.yml");
        assertNotNull(in, "recipes.yml is not on the test classpath");
        return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
    }
}
