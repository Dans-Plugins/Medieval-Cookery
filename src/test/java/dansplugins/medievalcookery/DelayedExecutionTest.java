package dansplugins.medievalcookery;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the message that confirms a finished meal, and how the after-eat item is handed back.
 * Finishing the meal itself needs a live player and so is left to the manual validation recorded
 * on the pull request.
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

    /** With no room left, the bundled Beet Salad's Bowl lands at the player's feet instead of vanishing. */
    @Test
    void anAfterEatItemThatDoesNotFitIsDropped() {
        ItemStack bowl = bundledAfterEatItem("beet_salad");
        List<ItemStack> added = new ArrayList<>();
        List<Object[]> dropped = new ArrayList<>();
        Location feet = new Location(null, 1, 64, 2);

        DelayedExecution.giveOrDrop(inventoryWithNoRoom(added), bowl, worldRecordingDrops(dropped), feet);

        assertEquals(List.of(bowl), added);
        assertEquals(1, dropped.size(), "the item that did not fit was lost");
        assertEquals(feet, dropped.get(0)[0]);
        assertEquals(bowl, dropped.get(0)[1]);
    }

    @Test
    void anAfterEatItemThatFitsIsNotDropped() {
        ItemStack bowl = bundledAfterEatItem("rice_bowl");
        List<ItemStack> added = new ArrayList<>();
        List<Object[]> dropped = new ArrayList<>();

        DelayedExecution.giveOrDrop(inventoryWithRoom(added), bowl, worldRecordingDrops(dropped),
                new Location(null, 0, 64, 0));

        assertEquals(List.of(bowl), added);
        assertTrue(dropped.isEmpty(), "an item that fit was dropped as well");
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

    /** A world that records the location and item of each drop made in it. */
    private static World worldRecordingDrops(List<Object[]> dropped) {
        return (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if (!method.getName().equals("dropItem") || args.length != 2) {
                        throw new UnsupportedOperationException(method.getName());
                    }
                    dropped.add(args);
                    return null;
                });
    }

    private static YamlConfiguration bundledRecipes() {
        InputStream in = DelayedExecutionTest.class.getClassLoader().getResourceAsStream("recipes.yml");
        assertNotNull(in, "recipes.yml is not on the test classpath");
        return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
    }
}
