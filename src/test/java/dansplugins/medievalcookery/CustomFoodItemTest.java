package dansplugins.medievalcookery;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Covers the cases in which a stack is rejected as a Medieval Cookery food without its metadata
 * being read. Reading the metadata needs Bukkit's item factory, which only a running server
 * provides, so the recognition of an actual crafted food is left to the manual validation
 * recorded on the pull request; what is guarded here is that everything a player might be
 * holding instead is turned away first.
 */
class CustomFoodItemTest {

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
}
