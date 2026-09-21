package dansplugins.medievalcookery;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        // textureBase64 is optional. Without usable texture data the head keeps its default skin
        // and is only named.
        URL skin = skinUrlOf(base64);
        if (skin != null) {
            // The head used to be textured by handing CraftBukkit's CraftMetaSkull a GameProfile
            // through reflection. That private method is gone from current servers, so the head is
            // textured through the PlayerProfile API instead, which every server since 1.18.1 has.
            // The profile carries no name: it is never a real player's, and current versions
            // validate a profile name as they would a player's (no spaces, 16 characters at most),
            // which the recipe names do not satisfy.
            try {
                PlayerProfile profile = Bukkit.createPlayerProfile(profileIdOf(base64));
                profile.getTextures().setSkin(skin);
                meta.setOwnerProfile(profile);
            } catch (IllegalArgumentException e) {
                // The server refuses the skin, which it does for any host other than
                // textures.minecraft.net. The food is still craftable, with a default skin.
                medievalCookery.getLogger().warning("Recipe '" + key + "': its textureBase64 skin "
                        + skin + " was rejected by the server (" + e.getMessage()
                        + "), so the food keeps the default head skin.");
            }
        }
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Reads the skin URL out of a {@code textureBase64} value, or returns null when the value
     * holds no usable one.
     *
     * The value is the Base64 form of the {@code textures} profile property, a JSON document of
     * the shape {@code {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/..."}}}}.
     * Only the URL is kept: the server rebuilds the property from it, so nothing else in the
     * document matters. A value shorter than 20 characters is treated as absent, as it always
     * was, rather than reported.
     */
    static URL skinUrlOf(String base64) {
        if (base64 == null || base64.length() < 20) {
            return null;
        }
        String json;
        try {
            json = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
        JsonElement node;
        try {
            node = JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            return null;
        }
        for (String member : new String[] {"textures", "SKIN", "url"}) {
            if (!node.isJsonObject() || !node.getAsJsonObject().has(member)) {
                return null;
            }
            node = node.getAsJsonObject().get(member);
        }
        if (!node.isJsonPrimitive() || !node.getAsJsonPrimitive().isString()) {
            return null;
        }
        try {
            return new URL(node.getAsString());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * The id of the profile a food head carries, derived from its texture so that it is the same
     * on every startup: a head crafted before a restart still stacks with one crafted after it.
     */
    static UUID profileIdOf(String base64) {
        return UUID.nameUUIDFromBytes(base64.getBytes(StandardCharsets.UTF_8));
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
