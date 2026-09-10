package dansplugins.medievalcookery.services;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    /**
     * The plugin instance is only consulted to write the bundled default out, which cannot happen
     * here because the folder already holds a recipes file, so a null stands in for the server-side
     * plugin the tests have no way to construct.
     */
    private static ConfigService serviceReading(Path dataFolder, String recipesYaml) throws IOException {
        Files.write(dataFolder.resolve("recipes.yml"), recipesYaml.getBytes(StandardCharsets.UTF_8));
        return new ConfigService(null, dataFolder.toFile());
    }

    @Test
    void recipesAreReadFromTheDataFolderRatherThanTheWorkingDirectory(@TempDir Path dataFolder) throws IOException {
        // The path the data folder was previously assumed to be. Its absence is what makes the
        // assertions below evidence that the folder the service was given is the one it read.
        assertFalse(new File("./plugins/MedievalCookery/recipes.yml").exists(),
                "the previously assumed path must be absent for this test to distinguish the two");

        ConfigService configService = serviceReading(dataFolder,
                "recipes:\n  stew:\n    name: Hearty Stew\n    hungerDecrease: 7\n");

        assertNotNull(configService.getRecipeConfig().getConfigurationSection("recipes"));
        assertEquals("Hearty Stew", configService.getRecipeConfig().getString("recipes.stew.name"));
        assertEquals(7, configService.getRecipeConfig().getInt("recipes.stew.hungerDecrease"));
    }
}
