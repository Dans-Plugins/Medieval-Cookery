package dansplugins.medievalcookery.services;

import dansplugins.medievalcookery.CustomFoodRecipe;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    // ConfigService reports a configuration mistake on standard output, so a test that checks
    // the report has to capture it.
    private static String printedBy(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return new String(captured.toByteArray(), StandardCharsets.UTF_8);
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
    void readSymbolsRejectsAMaterialThatExistsOnlyAsABlock() {
        // CARROTS is the planted crop; the item a player holds is CARROT.
        assertNull(ConfigService.readSymbols(symbolsFrom("symbols:\n  W: CARROTS\n"), "stew"));
    }

    @Test
    void readSymbolsRejectsAir() {
        assertNull(ConfigService.readSymbols(symbolsFrom("symbols:\n  W: AIR\n"), "stew"));
    }

    // --- item materials --------------------------------------------------------------------

    @Test
    void anItemMaterialIsHoldable() {
        assertTrue(ConfigService.isHoldableItem(Material.CARROT));
        assertTrue(ConfigService.isHoldableItem(Material.BOWL));
    }

    @Test
    void aBlockOnlyMaterialOrAirIsNotHoldable() {
        assertFalse(ConfigService.isHoldableItem(Material.CARROTS));
        assertFalse(ConfigService.isHoldableItem(Material.WATER));
        assertFalse(ConfigService.isHoldableItem(Material.AIR));
    }

    @Test
    void afterEatItemNamingAnItemIsKept() {
        assertEquals(Material.BOWL, ConfigService.readAfterEatItem("BOWL", "Beet Salad"));
    }

    @Test
    void afterEatItemLeftEmptyGivesNothing() {
        assertNull(ConfigService.readAfterEatItem("", "Beet Salad"));
    }

    @Test
    void afterEatItemNamingAnUnknownMaterialGivesNothing() {
        assertNull(ConfigService.readAfterEatItem("NOT_A_REAL_MATERIAL", "Beet Salad"));
    }

    @Test
    void afterEatItemNamingABlockOnlyMaterialGivesNothing() {
        assertNull(ConfigService.readAfterEatItem("WATER", "Beet Salad"));
    }

    @Test
    void aPositiveHungerDecreaseIsKept() {
        assertEquals(7, ConfigService.readHungerDecrease(7, "Steak Sandwich"));
    }

    @Test
    void aZeroHungerDecreaseFallsBackToTheDefault() {
        assertEquals(1, ConfigService.readHungerDecrease(0, "Steak Sandwich"));
    }

    // -1 is Bukkit's infinite potion duration, so it must not reach the Saturation effect.
    @Test
    void aNegativeHungerDecreaseFallsBackToTheDefault() {
        assertEquals(1, ConfigService.readHungerDecrease(-1, "Steak Sandwich"));
        assertEquals(1, ConfigService.readHungerDecrease(-40, "Steak Sandwich"));
    }

    // An omitted hungerDecrease is optional, so it gets the default without an error.
    @Test
    void anOmittedHungerDecreaseFallsBackToTheDefaultSilently() {
        int[] ticks = new int[1];
        String printed = printedBy(() -> ticks[0] = ConfigService.readHungerDecrease(null, "Steak Sandwich"));
        assertEquals(1, ticks[0]);
        assertEquals("", printed);
    }

    // getInt already returned the default for these, so what the fix adds is the error. The
    // values are read from YAML rather than passed as literals, so that they are whatever types
    // the real parser produces for these spellings.
    @Test
    void aNonNumericHungerDecreaseIsReportedAndFallsBackToTheDefault() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader("a: lots\nb: \"7\"\nc: true\n"));
        for (String key : new String[] {"a", "b", "c"}) {
            int[] ticks = new int[1];
            String printed = printedBy(() -> ticks[0] = ConfigService.readHungerDecrease(config.get(key), "Steak Sandwich"));
            assertEquals(1, ticks[0], key);
            assertTrue(printed.contains("hungerDecrease '" + config.get(key) + "' of recipe 'Steak Sandwich'")
                    && printed.contains("is not a number"), key + " was not reported: " + printed);
        }
    }

    @Test
    void aDecimalHungerDecreaseIsTruncatedAsGetIntWouldTruncateIt() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader("a: 7.9\nb: 0.5\n"));
        assertEquals(7, ConfigService.readHungerDecrease(config.get("a"), "Steak Sandwich"));
        assertEquals(1, ConfigService.readHungerDecrease(config.get("b"), "Steak Sandwich"));
    }

    // --- renamed materials -----------------------------------------------------------------
    //
    // The API this project compiles against is from after GRASS became SHORT_GRASS, so the
    // name the test enum lacks is the old one. On a server from before the rename the roles
    // are the other way round, and the same two-way table serves both.

    @Test
    void aMaterialIsResolvedByItsCurrentName() {
        assertEquals(Material.SHORT_GRASS, ConfigService.resolveMaterial("SHORT_GRASS"));
        assertEquals(Material.BOWL, ConfigService.resolveMaterial("BOWL"));
    }

    @Test
    void aMaterialIsResolvedByTheNameItHadBeforeARename() {
        assertNull(Material.getMaterial("GRASS"),
                "GRASS is expected to be unknown to this API; if it is known, the rename table is no longer exercised");
        assertEquals(Material.SHORT_GRASS, ConfigService.resolveMaterial("GRASS"));
    }

    @Test
    void aNameOnNeitherSideOfARenameStaysUnknown() {
        assertNull(ConfigService.resolveMaterial("NOT_A_REAL_MATERIAL"));
    }

    /** What an install whose recipes.yml predates the rename reads on a current server. */
    @Test
    void readSymbolsAcceptsTheOldNameOfARenamedMaterial() {
        Map<String, Material> ingredients = ConfigService.readSymbols(
                symbolsFrom("symbols:\n  G: GRASS\n"), "beet_salad");

        assertNotNull(ingredients);
        assertEquals(Material.SHORT_GRASS, ingredients.get("G"));
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

    // --- recipe ids ------------------------------------------------------------------------

    private static final String[] USABLE_IDS = {"salmon_roll", "fish.stew-2", "stews/fish", "2"};
    private static final String[] UNUSABLE_IDS = {"Fish_Stew", "fish stew", "fish:stew", "fishstew!", "pâté"};

    @Test
    void anIdOfLowercaseLettersDigitsAndPunctuationBukkitAllowsIsUsable() {
        for (String id : USABLE_IDS) {
            assertTrue(ConfigService.isRecipeIdUsable(id), id);
        }
    }

    @Test
    void anIdWithAnUppercaseLetterSpaceOrOtherCharacterIsNotUsable() {
        for (String id : UNUSABLE_IDS) {
            assertFalse(ConfigService.isRecipeIdUsable(id), id);
        }
    }

    /** The check stands in for NamespacedKey's own, so the two must agree on every case above. */
    @Test
    void theIdCheckAgreesWithNamespacedKey() {
        for (String id : USABLE_IDS) {
            assertEquals("medievalcookery:" + id, new NamespacedKey("medievalcookery", id).toString());
        }
        for (String id : UNUSABLE_IDS) {
            assertThrows(IllegalArgumentException.class, () -> new NamespacedKey("medievalcookery", id), id);
        }
    }

    /**
     * Everything about this entry is usable except its id, so it reaches the id check rather than
     * being skipped for some other reason first, and without the check would go on to be
     * constructed — which fails, and before the fix took the whole plugin down with it.
     */
    @Test
    void loadRecipesSkipsAnEntryWhoseIdBukkitWouldRejectInsteadOfThrowing(@TempDir Path dataFolder) throws IOException {
        ConfigService configService = serviceReading(dataFolder,
                "recipes:\n"
                        + "  Fish_Stew:\n"
                        + "    name: Fish Stew\n"
                        + "    recipe: [' S ', ' C ', ' B ']\n"
                        + "    symbols:\n      S: SALMON\n      C: CARROT\n      B: BOWL\n"
                        + "    hungerDecrease: 6\n");

        List<CustomFoodRecipe> recipes = configService.loadRecipes();

        assertNotNull(recipes);
        assertTrue(recipes.isEmpty());
    }

    // --- usage reporting -------------------------------------------------------------------

    /** The bundled config.yml, read from the classpath the way JavaPlugin registers it as defaults. */
    private static YamlConfiguration bundledConfig() {
        InputStream stream = ConfigServiceTest.class.getResourceAsStream("/config.yml");
        assertNotNull(stream, "config.yml is missing from the packaged resources");
        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /**
     * What getConfig() returns on a server whose on-disk config.yml predates the usage-reporting
     * block: the file as written, with the jar's config.yml registered as its defaults.
     * saveDefaultConfig() never rewrites an existing file, so this is every upgraded installation.
     */
    private static Configuration onDiskConfigLacking(String yamlOnDisk) {
        YamlConfiguration onDisk = YamlConfiguration.loadConfiguration(new StringReader(yamlOnDisk));
        onDisk.setDefaults(bundledConfig());
        return onDisk;
    }

    @Test
    void bundledConfigCarriesTheUsageReportingBlock() {
        YamlConfiguration bundled = bundledConfig();
        assertTrue(bundled.getBoolean("usage-reporting.enabled"));
        assertEquals("https://trace.danielstephenson.dev", bundled.getString("usage-reporting.endpoint"));
        assertFalse(bundled.getString("usage-reporting.key", "").isEmpty(),
                "an empty bundled key would turn reporting off on every server");
    }

    @Test
    void usageReportingSettingsFallThroughToTheBundledDefaultsOnAnUpgradedInstallation() {
        // A config.yml written before this block existed. Nothing here mentions usage reporting.
        Configuration config = onDiskConfigLacking("some-older-setting: 3\n");
        YamlConfiguration bundled = bundledConfig();

        assertTrue(ConfigService.isUsageReportingEnabled(config));
        assertEquals(bundled.getString("usage-reporting.endpoint"), ConfigService.getUsageReportingEndpoint(config));
        assertEquals(bundled.getString("usage-reporting.key"), ConfigService.getUsageReportingKey(config));

        // The reason the one-argument getters are used: the two-argument form would have
        // returned this fallback instead of the bundled key, and turned reporting off.
        assertEquals("", config.getString("usage-reporting.key", ""),
                "getString(path, def) is expected to ignore the defaults; if this ever changes the "
                        + "comment in ConfigService is stale");
    }

    @Test
    void usageReportingSettingsOnDiskWinOverTheBundledDefaults() {
        Configuration config = onDiskConfigLacking(
                "usage-reporting:\n  enabled: false\n  endpoint: http://localhost:1\n  key: ''\n");

        assertFalse(ConfigService.isUsageReportingEnabled(config));
        assertEquals("http://localhost:1", ConfigService.getUsageReportingEndpoint(config));
        assertEquals("", ConfigService.getUsageReportingKey(config));
    }

    @Test
    void usageReportingStringsAreNeverNullEvenWithNoDefaultsAtAll() {
        Configuration config = YamlConfiguration.loadConfiguration(new StringReader(""));

        assertFalse(ConfigService.isUsageReportingEnabled(config));
        assertEquals("https://trace.danielstephenson.dev", ConfigService.getUsageReportingEndpoint(config));
        assertEquals("", ConfigService.getUsageReportingKey(config));
    }
}
