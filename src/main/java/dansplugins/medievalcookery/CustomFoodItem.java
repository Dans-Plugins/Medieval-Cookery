package dansplugins.medievalcookery;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Recognises a crafted Medieval Cookery food in an inventory slot.
 *
 * The foods have no marker of their own: {@link CustomFoodRecipe} produces a player head whose
 * display name is the recipe's configured name, and that name is the only thing distinguishing a
 * crafted food from any other head. Reading it is shared between the listener that starts an
 * eating session and the delayed task that finishes one, because both have to agree on what the
 * player is holding.
 */
public final class CustomFoodItem {

    private CustomFoodItem() {
    }

    /**
     * Returns the display name of a stack that could be one of the plugin's foods, or null when
     * the stack cannot be one. Whether the name belongs to a loaded recipe is the caller's
     * question — this only reports what the stack claims to be.
     */
    public static String nameOf(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return null;
        }
        return meta.getDisplayName();
    }
}
