package dansplugins.medievalcookery;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;

import java.net.URL;

/**
 * Recognises a crafted Medieval Cookery food in an inventory slot.
 *
 * {@link CustomFoodRecipe} produces a player head tagged with the id of the recipe that made it,
 * and that tag is what identifies a food: a head renamed in an anvil to a recipe's name carries
 * no tag, and a food renamed in an anvil keeps its own. Foods crafted before the tag existed are
 * still recognised, by their display name together with the skin the recipe gives them, which a
 * renamed ordinary head does not have. Reading the stack is shared between the listener that
 * starts an eating session and the delayed task that finishes one, because both have to agree on
 * what the player is holding.
 */
public final class CustomFoodItem {

    private CustomFoodItem() {
    }

    /**
     * Returns the recipe id a stack is tagged with, or null when the stack carries no tag — either
     * because it is not a food at all, or because it was crafted before foods were tagged.
     */
    public static String recipeIdOf(ItemStack item, NamespacedKey tagKey) {
        ItemMeta meta = headMetaOf(item);
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(tagKey, PersistentDataType.STRING);
    }

    /**
     * Returns the display name of a stack that could be one of the plugin's foods, or null when
     * the stack cannot be one. Whether the name belongs to a loaded recipe is the caller's
     * question — this only reports what the stack claims to be.
     */
    public static String nameOf(ItemStack item) {
        ItemMeta meta = headMetaOf(item);
        if (meta == null || !meta.hasDisplayName()) {
            return null;
        }
        return meta.getDisplayName();
    }

    /** Returns the skin a player head is textured with, or null when it has none. */
    public static URL skinOf(ItemStack item) {
        ItemMeta meta = headMetaOf(item);
        if (!(meta instanceof SkullMeta)) {
            return null;
        }
        PlayerProfile profile = ((SkullMeta) meta).getOwnerProfile();
        return profile == null ? null : profile.getTextures().getSkin();
    }

    /**
     * Decides whether a head with no recipe tag is a food crafted, before foods were tagged, by
     * the recipe with the given name and skin.
     *
     * The name alone is not enough, since any head can be renamed in an anvil. The skin is what
     * an ordinary head cannot be given in survival, so both have to agree. A recipe with no skin
     * produced plain named heads, indistinguishable from a renamed one, so it has no untagged
     * foods that can be told apart and none is accepted.
     */
    static boolean isUntaggedFood(String displayName, URL headSkin, String recipeName, URL recipeSkin) {
        return displayName != null
                && displayName.equalsIgnoreCase(recipeName)
                && isSameSkin(headSkin, recipeSkin);
    }

    /**
     * Compares two skins by their texture, which is the last segment of the URL. The scheme and
     * host are left out because the server may rewrite them while keeping the texture itself.
     */
    static boolean isSameSkin(URL a, URL b) {
        if (a == null || b == null) {
            return false;
        }
        String textureA = textureOf(a);
        return !textureA.isEmpty() && textureA.equals(textureOf(b));
    }

    private static String textureOf(URL skin) {
        String path = skin.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private static ItemMeta headMetaOf(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return null;
        }
        return item.getItemMeta();
    }
}
