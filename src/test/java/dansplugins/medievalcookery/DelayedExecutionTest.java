package dansplugins.medievalcookery;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the message that confirms a finished meal. Finishing the meal itself needs a live
 * player and so is left to the manual validation recorded on the pull request.
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

    private static YamlConfiguration bundledRecipes() {
        InputStream in = DelayedExecutionTest.class.getClassLoader().getResourceAsStream("recipes.yml");
        assertNotNull(in, "recipes.yml is not on the test classpath");
        return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
    }
}
