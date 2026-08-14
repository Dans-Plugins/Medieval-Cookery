package dansplugins.medievalcookery;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the bundled recipes.yml against the schema CONTRIBUTING.md documents and
 * ConfigService expects. The resource is read from the classpath, so it is the packaged
 * copy that is checked rather than a hand-built fixture.
 */
class RecipesResourceTest {

    private static final YamlConfiguration RECIPES = load();

    private static YamlConfiguration load() {
        InputStream stream = RecipesResourceTest.class.getResourceAsStream("/recipes.yml");
        assertNotNull(stream, "recipes.yml is missing from the packaged resources");
        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    private static ConfigurationSection recipe(String key) {
        return RECIPES.getConfigurationSection("recipes." + key);
    }

    static Stream<String> recipeKeys() {
        ConfigurationSection recipes = RECIPES.getConfigurationSection("recipes");
        assertNotNull(recipes, "ConfigService aborts loading when the 'recipes' section is absent");
        return recipes.getKeys(false).stream();
    }

    @Test
    void bundledFileDefinesAtLeastOneRecipe() {
        assertTrue(recipeKeys().count() > 0, "recipes.yml defines no recipes");
    }

    @ParameterizedTest
    @MethodSource("recipeKeys")
    void recipeHasDisplayName(String key) {
        String name = recipe(key).getString("name", "");
        assertFalse(name.isEmpty(), key + " has no 'name'; ConfigService skips such a recipe");
    }

    @ParameterizedTest
    @MethodSource("recipeKeys")
    void recipeHasThreeRowsOfThreeColumns(String key) {
        List<String> shape = recipe(key).getStringList("recipe");
        assertEquals(3, shape.size(), key + " must declare exactly 3 pattern rows");
        for (String row : shape) {
            assertEquals(3, row.length(), key + " pattern row '" + row + "' must be 3 characters wide");
        }
    }

    @ParameterizedTest
    @MethodSource("recipeKeys")
    void recipeMapsEverySymbolToAKnownMaterial(String key) {
        ConfigurationSection symbols = recipe(key).getConfigurationSection("symbols");
        assertNotNull(symbols, key + " has no 'symbols' section; ConfigService throws without one");
        assertFalse(symbols.getKeys(false).isEmpty(), key + " declares no symbols");
        for (String symbol : symbols.getKeys(false)) {
            assertEquals(1, symbol.length(), key + " symbol '" + symbol + "' must be a single character");
            String material = symbols.getString(symbol, "");
            assertNotNull(Material.getMaterial(material), key + " symbol '" + symbol
                    + "' references unknown material '" + material + "'");
        }
    }

    @ParameterizedTest
    @MethodSource("recipeKeys")
    void recipePatternAndSymbolsAgree(String key) {
        Set<String> declared = recipe(key).getConfigurationSection("symbols").getKeys(false);
        Set<String> used = new HashSet<>();
        for (String row : recipe(key).getStringList("recipe")) {
            for (char c : row.toCharArray()) {
                if (c != ' ') {
                    used.add(String.valueOf(c));
                }
            }
        }
        Set<String> undeclared = new HashSet<>(used);
        undeclared.removeAll(declared);
        assertTrue(undeclared.isEmpty(), key + " pattern uses symbols that are not declared: " + undeclared);

        Set<String> unused = new HashSet<>(declared);
        unused.removeAll(used);
        assertTrue(unused.isEmpty(), key + " declares symbols its pattern never uses: " + unused);
    }

    @ParameterizedTest
    @MethodSource("recipeKeys")
    void recipeDeclaresAPositiveHungerDecrease(String key) {
        assertTrue(recipe(key).isInt("hungerDecrease"),
                key + " has no integer 'hungerDecrease'; ConfigService would silently default it to 1");
        assertTrue(recipe(key).getInt("hungerDecrease") > 0,
                key + " must declare a positive 'hungerDecrease' tick duration");
    }

    @ParameterizedTest
    @MethodSource("recipeKeys")
    void optionalAfterEatItemNamesAKnownMaterial(String key) {
        String afterEatItem = recipe(key).getString("afterEatItem", "");
        if (!afterEatItem.isEmpty()) {
            assertNotNull(Material.getMaterial(afterEatItem),
                    key + " 'afterEatItem' references unknown material '" + afterEatItem + "'");
        }
    }
}
