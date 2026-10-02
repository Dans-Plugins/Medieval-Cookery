package dansplugins.medievalcookery;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the cases in which a stack is rejected as a Medieval Cookery food without its metadata
 * being read, and the decision of whether an untagged head is a food crafted before foods were
 * tagged. Reading the metadata — the tag, the name and the skin — needs Bukkit's item factory,
 * which only a running server provides, so the recognition of an actual crafted food is left to
 * the manual validation recorded on the pull request; what is guarded here is that everything a
 * player might be holding instead is turned away first, and that a renamed ordinary head is never
 * taken for a food.
 */
class CustomFoodItemTest {

    private static final NamespacedKey TAG = new NamespacedKey("medievalcookery", "recipe");

    private static final URL SKIN = url("http://textures.minecraft.net/texture/abc123");
    private static final URL OTHER_SKIN = url("http://textures.minecraft.net/texture/def456");

    private static final YamlConfiguration RECIPES = load();

    private static YamlConfiguration load() {
        InputStream stream = CustomFoodItemTest.class.getResourceAsStream("/recipes.yml");
        assertNotNull(stream, "recipes.yml is missing from the packaged resources");
        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    static Stream<String> texturedRecipeKeys() {
        return CustomFoodRecipeTest.texturedRecipeKeys();
    }

    private static URL url(String spec) {
        try {
            return new URL(spec);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    private static String nameOf(String key) {
        return RECIPES.getString("recipes." + key + ".name");
    }

    private static URL skinOf(String key) {
        return CustomFoodRecipe.skinUrlOf(RECIPES.getString("recipes." + key + ".textureBase64"));
    }

    @Test
    void anEmptyHandIsNotAFood() {
        assertNull(CustomFoodItem.nameOf(null));
    }

    @Test
    void anOrdinaryFoodItemIsNotACustomFood() {
        assertNull(CustomFoodItem.nameOf(new ItemStack(Material.BREAD, 1)));
    }

    @Test
    void aBlockIsNotACustomFood() {
        assertNull(CustomFoodItem.nameOf(new ItemStack(Material.STONE, 1)));
    }

    /**
     * A mob head is the nearest miss: it is a head, but not the PLAYER_HEAD material the recipes
     * produce, so it must not reach the metadata read.
     */
    @ParameterizedTest
    @EnumSource(names = {"ZOMBIE_HEAD", "SKELETON_SKULL", "CREEPER_HEAD", "DRAGON_HEAD"})
    void aMobHeadIsNotACustomFood(Material material) {
        assertNull(CustomFoodItem.nameOf(new ItemStack(material, 1)));
    }

    @Test
    void aStackThatIsNotAHeadCarriesNoTagAndNoSkin() {
        ItemStack bread = new ItemStack(Material.BREAD, 1);

        assertNull(CustomFoodItem.recipeIdOf(null, TAG));
        assertNull(CustomFoodItem.recipeIdOf(bread, TAG));
        assertNull(CustomFoodItem.skinOf(null));
        assertNull(CustomFoodItem.skinOf(bread));
    }

    /**
     * Every bundled recipe's food, crafted before foods were tagged, carries the recipe's name and
     * the recipe's skin, and is still eaten as that recipe — in any letter case, as it always was.
     */
    @ParameterizedTest
    @MethodSource("texturedRecipeKeys")
    void anUntaggedFoodOfABundledRecipeIsStillRecognised(String key) {
        String name = nameOf(key);

        assertTrue(CustomFoodItem.isUntaggedFood(name, skinOf(key), name, skinOf(key)));
        assertTrue(CustomFoodItem.isUntaggedFood(name.toUpperCase(), skinOf(key), name, skinOf(key)));
    }

    /** The case the tag exists for: an ordinary head renamed in an anvil has no skin of its own. */
    @ParameterizedTest
    @MethodSource("texturedRecipeKeys")
    void aPlainHeadRenamedAfterABundledRecipeIsNotAFood(String key) {
        assertFalse(CustomFoodItem.isUntaggedFood(nameOf(key), null, nameOf(key), skinOf(key)));
    }

    @Test
    void aHeadWithAnotherSkinIsNotAFoodEvenUnderTheRecipesName() {
        assertFalse(CustomFoodItem.isUntaggedFood("Steak Sandwich", OTHER_SKIN, "Steak Sandwich", SKIN));
    }

    @Test
    void aHeadWithTheRecipesSkinUnderAnotherNameIsNotAFood() {
        assertFalse(CustomFoodItem.isUntaggedFood("Fish Stew", SKIN, "Steak Sandwich", SKIN));
        assertFalse(CustomFoodItem.isUntaggedFood(null, SKIN, "Steak Sandwich", SKIN));
    }

    /**
     * A recipe with no skin made plain named heads, which cannot be told apart from a renamed
     * ordinary head, so none of its untagged heads is accepted.
     */
    @Test
    void aRecipeWithNoSkinAcceptsNoUntaggedHead() {
        assertFalse(CustomFoodItem.isUntaggedFood("Plain Food", null, "Plain Food", null));
    }

    @Test
    void skinsAreComparedByTextureNotByScheme() {
        assertTrue(CustomFoodItem.isSameSkin(SKIN, url("https://textures.minecraft.net/texture/abc123")));
        assertFalse(CustomFoodItem.isSameSkin(SKIN, OTHER_SKIN));
        assertFalse(CustomFoodItem.isSameSkin(SKIN, null));
        assertFalse(CustomFoodItem.isSameSkin(null, SKIN));
    }

    /** A URL with no texture segment must not match another such URL. */
    @Test
    void aSkinWithNoTextureMatchesNothing() {
        URL bare = url("http://textures.minecraft.net/texture/");

        assertFalse(CustomFoodItem.isSameSkin(bare, bare));
    }
}
