package dansplugins.medievalcookery;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class CustomFoodRecipe {
    private final MedievalCookery medievalCookery;

    public String name = "";
    public String key = "";
    public String texture = "";
    public List<String> recipeShape = new ArrayList<String>();
    public int hungerDecrease = 2;
    public Material afterEatItem = null;

    public ItemStack itemWithBase64(ItemStack item, String base64) {

        if (!(item.getItemMeta() instanceof SkullMeta)) {
            return null;
        }
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        if (base64.length() < 20) {
            // textureBase64 is optional, and the profile below reads the last 20 characters of it.
            // Without usable texture data the head keeps its default skin and is only named.
            meta.setDisplayName(name);
            item.setItemMeta(meta);
            return item;
        }

        Method metaSetProfileMethod = null;
        try {
            metaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            metaSetProfileMethod.setAccessible(true);
            UUID id = new UUID(
                    base64.substring(base64.length() - 20).hashCode(),
                    base64.substring(base64.length() - 10).hashCode()
            );
            GameProfile profile = new GameProfile(id, name);
            profile.getProperties().put("textures", new Property("textures", base64));
            metaSetProfileMethod.invoke(meta, profile);
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return item;
    }

    public CustomFoodRecipe(String recipeKey, String recipeName,
                            String[] shape,
                            Map<String, Material> ingredients,
                            String texture, MedievalCookery medievalCookery, int hungerAmt, Material afterEatItemMaterial
    ) {
        this.medievalCookery = medievalCookery;
        key = recipeKey;
        name = recipeName;

        afterEatItem = afterEatItemMaterial;
        ItemStack item = itemWithBase64(new ItemStack(Material.PLAYER_HEAD, 1), texture);
        NamespacedKey nskey = new NamespacedKey(this.medievalCookery, key);
        ShapedRecipe recipe = new ShapedRecipe(nskey, item);
        recipe.shape(shape[0], shape[1], shape[2]);
        hungerDecrease = hungerAmt;
        // Every symbol was resolved to a known material, and checked against the pattern, by
        // ConfigService before this constructor is reached.
        for (Map.Entry<String, Material> ingredient : ingredients.entrySet()) {
            recipe.setIngredient(ingredient.getKey().charAt(0), ingredient.getValue());
        }
        this.medievalCookery.getServer().addRecipe(recipe);
        System.out.println("Registered custom recipe " + recipeKey + " with Bukkit");
    }
}
