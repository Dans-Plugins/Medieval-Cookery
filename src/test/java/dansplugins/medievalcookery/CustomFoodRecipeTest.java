package dansplugins.medievalcookery;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers how a recipe's {@code textureBase64} becomes the skin of its head. Applying the skin
 * needs a running server, which creates the profile and validates the URL, so what is guarded
 * here is the reading of the value: every bundled texture must yield the skin URL the server
 * accepts, and a value that cannot yield one must leave the head with its default skin rather
 * than throw out of recipe loading.
 */
class CustomFoodRecipeTest {

    private static final YamlConfiguration RECIPES = load();

    private static YamlConfiguration load() {
        InputStream stream = CustomFoodRecipeTest.class.getResourceAsStream("/recipes.yml");
        assertNotNull(stream, "recipes.yml is missing from the packaged resources");
        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    static Stream<String> texturedRecipeKeys() {
        ConfigurationSection recipes = RECIPES.getConfigurationSection("recipes");
        assertNotNull(recipes);
        return recipes.getKeys(false).stream()
                .filter(key -> recipes.isString(key + ".textureBase64"));
    }

    private static String encode(String json) {
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @MethodSource("texturedRecipeKeys")
    void everyBundledTextureNamesASkinOnMojangsTextureHost(String key) {
        URL skin = CustomFoodRecipe.skinUrlOf(RECIPES.getString("recipes." + key + ".textureBase64"));

        assertNotNull(skin, key + " textureBase64 holds no skin URL; the food would have a default skin");
        // The server refuses any other host, so a bundled texture elsewhere would be a
        // default-skinned head on every server.
        assertEquals("textures.minecraft.net", skin.getHost(), key + " skin is not on Mojang's texture host");
        assertTrue(skin.getPath().startsWith("/texture/"), key + " skin URL is not a texture: " + skin);
    }

    @Test
    void skinUrlIsReadFromTheTexturesProperty() {
        String base64 = encode("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/abc123\"}}}");

        assertEquals("http://textures.minecraft.net/texture/abc123", String.valueOf(CustomFoodRecipe.skinUrlOf(base64)));
    }

    /**
     * The property as Mojang's session server returns it carries more than the skin, and the
     * bundled values were pasted from there.
     */
    @Test
    void otherMembersOfTheTexturesPropertyAreIgnored() {
        String base64 = encode("{\"timestamp\":1,\"profileId\":\"0\",\"profileName\":\"x\",\"textures\":"
                + "{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/abc123\",\"metadata\":{\"model\":\"slim\"}},"
                + "\"CAPE\":{\"url\":\"http://textures.minecraft.net/texture/cape\"}}}");

        assertEquals("http://textures.minecraft.net/texture/abc123", String.valueOf(CustomFoodRecipe.skinUrlOf(base64)));
    }

    @Test
    void anAbsentTextureYieldsNoSkin() {
        assertNull(CustomFoodRecipe.skinUrlOf(null));
        assertNull(CustomFoodRecipe.skinUrlOf(""));
    }

    /** ConfigService reads an absent textureBase64 as "", and CONFIG.md documents this threshold. */
    @Test
    void aTextureShorterThanTwentyCharactersIsTreatedAsAbsent() {
        assertNull(CustomFoodRecipe.skinUrlOf("eyJ0ZXh0dXJlcyI6e319"));
    }

    @Test
    void aValueThatIsNotBase64YieldsNoSkin() {
        assertNull(CustomFoodRecipe.skinUrlOf("this is not base64 at all, but it is long enough"));
    }

    @Test
    void base64OfSomethingOtherThanJsonYieldsNoSkin() {
        assertNull(CustomFoodRecipe.skinUrlOf(encode("http://textures.minecraft.net/texture/abc123")));
    }

    @Test
    void jsonWithoutASkinYieldsNoSkin() {
        assertNull(CustomFoodRecipe.skinUrlOf(encode("{\"textures\":{\"CAPE\":{\"url\":\"http://textures.minecraft.net/texture/cape\"}}}")));
        assertNull(CustomFoodRecipe.skinUrlOf(encode("{\"textures\":{\"SKIN\":{}}}")));
        assertNull(CustomFoodRecipe.skinUrlOf(encode("{\"textures\":\"SKIN\"}")));
        assertNull(CustomFoodRecipe.skinUrlOf(encode("[\"textures\"]")));
    }

    @Test
    void aSkinThatIsNotAUrlYieldsNoSkin() {
        assertNull(CustomFoodRecipe.skinUrlOf(encode("{\"textures\":{\"SKIN\":{\"url\":\"not a url\"}}}")));
        assertNull(CustomFoodRecipe.skinUrlOf(encode("{\"textures\":{\"SKIN\":{\"url\":7}}}")));
    }

    @Test
    void profileIdIsTheSameForTheSameTextureOnEveryStartup() {
        String texture = RECIPES.getString("recipes.salmon_roll.textureBase64");

        assertEquals(CustomFoodRecipe.profileIdOf(texture), CustomFoodRecipe.profileIdOf(texture),
                "a head crafted before a restart would not stack with one crafted after it");
    }

    @Test
    void profileIdsOfDifferentTexturesDiffer() {
        assertNotEquals(CustomFoodRecipe.profileIdOf(RECIPES.getString("recipes.salmon_roll.textureBase64")),
                CustomFoodRecipe.profileIdOf(RECIPES.getString("recipes.beet_salad.textureBase64")));
    }
}
