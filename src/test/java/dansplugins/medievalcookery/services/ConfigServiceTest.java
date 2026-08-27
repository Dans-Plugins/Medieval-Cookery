package dansplugins.medievalcookery.services;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the two checks ConfigService runs over a recipe entry before it is handed to
 * CustomFoodRecipe. Both are reached with a hand-written configuration rather than the bundled
 * recipes.yml, because what is under test is how a server owner's malformed edit is handled;
 * the bundled file is guarded separately by RecipesResourceTest.
 */
class ConfigServiceTest {

    private static ConfigurationSection symbolsFrom(String yaml) {
        return YamlConfiguration.loadConfiguration(new StringReader(yaml)).getConfigurationSection("symbols");
    }

    private static Set<String> declared(String... symbols) {
        return new HashSet<String>(Arrays.asList(symbols));
    }

    @Test
    void readSymbolsMapsEverySymbolToItsMaterial() {
        Map<String, Material> ingredients = ConfigService.readSymbols(
                symbolsFrom("symbols:\n  W: WHEAT\n  B: BOWL\n"), "stew");

        assertNotNull(ingredients);
        assertEquals(2, ingredients.size());
        assertEquals(Material.WHEAT, ingredients.get("W"));
        assertEquals(Material.BOWL, ingredients.get("B"));
    }

    @Test
    void readSymbolsRejectsAMissingSymbolsSection() {
        assertNull(ConfigService.readSymbols(null, "stew"));
    }

    @Test
    void readSymbolsRejectsASectionThatDeclaresNoSymbols() {
        assertNull(ConfigService.readSymbols(symbolsFrom("symbols: {}\n"), "stew"));
    }

    @Test
    void readSymbolsRejectsASymbolWithAnEmptyMaterialName() {
        assertNull(ConfigService.readSymbols(symbolsFrom("symbols:\n  W: \"\"\n"), "stew"));
    }

    @Test
    void readSymbolsRejectsAnUnknownMaterialName() {
        assertNull(ConfigService.readSymbols(symbolsFrom("symbols:\n  W: NOT_A_REAL_MATERIAL\n"), "stew"));
    }

    @Test
    void readSymbolsRejectsAMultiCharacterSymbol() {
        assertNull(ConfigService.readSymbols(symbolsFrom("symbols:\n  WW: WHEAT\n"), "stew"));
    }

    @Test
    void shapeOfThreeRowsOfThreeIsUsable() {
        assertTrue(ConfigService.isShapeUsable(new String[]{"WWW", "W W", "WWW"}, declared("W"), "stew"));
    }

    @Test
    void spacesAreEmptySlotsRatherThanSymbols() {
        assertTrue(ConfigService.isShapeUsable(new String[]{"   ", " W ", "   "}, declared("W"), "stew"));
    }

    @Test
    void shapeWithFewerThanThreeRowsIsRejected() {
        assertFalse(ConfigService.isShapeUsable(new String[]{"WWW", "WWW"}, declared("W"), "stew"));
    }

    @Test
    void shapeWithNoRowsAtAllIsRejected() {
        assertFalse(ConfigService.isShapeUsable(new String[0], declared("W"), "stew"));
    }

    @Test
    void shapeWithARowThatIsNotThreeWideIsRejected() {
        assertFalse(ConfigService.isShapeUsable(new String[]{"WWWW", "WWW", "WWW"}, declared("W"), "stew"));
    }

    @Test
    void shapeUsingAnUndeclaredSymbolIsRejected() {
        assertFalse(ConfigService.isShapeUsable(new String[]{"WBW", "WWW", "WWW"}, declared("W"), "stew"));
    }

    @Test
    void symbolThePatternNeverUsesIsRejected() {
        assertFalse(ConfigService.isShapeUsable(new String[]{"WWW", "WWW", "WWW"}, declared("W", "B"), "stew"));
    }

    @Test
    void patternOfNothingButSpacesIsRejected() {
        assertFalse(ConfigService.isShapeUsable(new String[]{"   ", "   ", "   "}, declared("W"), "stew"));
    }

    @Test
    void patternOfNothingButSpacesIsLeftForReadSymbolsToReject() {
        Set<String> noSymbols = Collections.emptySet();
        assertTrue(ConfigService.isShapeUsable(new String[]{"   ", "   ", "   "}, noSymbols, "stew"),
                "an empty pattern passes this check; loadRecipes relies on readSymbols to reject "
                        + "a recipe that declares no symbols");
    }
}
