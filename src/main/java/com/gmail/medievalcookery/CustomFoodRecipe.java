package com.gmail.medievalcookery;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class CustomFoodRecipe {

    public String name = "";
    public String key = "";
    public String texture = "";
    public List<String> recipeShape = new ArrayList<String>();

    public ItemStack itemWithBase64(ItemStack item, String base64) {

        if (!(item.getItemMeta() instanceof SkullMeta)) {
            return null;
        }
        SkullMeta meta = (SkullMeta) item.getItemMeta();

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
            int time = StorageSubsystem.getTime("Salmon Roll");
            if (time > 0) {
                TimeStampSubsystem.assignTimeStamp(item, time);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return item;
    }

    public CustomFoodRecipe(MedievalCookery plugin, String recipeKey, String recipeName,
            String[] shape,
            HashMap<String, Material> ingredients
    ) {
        key = recipeKey;
        name = recipeName;
        ItemStack item = itemWithBase64(new ItemStack(Material.PLAYER_HEAD, 1), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTc5ZTQxYzE0NmI5NTQ5Nzg2YmRiNzEzOGYzNDlmZDdjMzk1MjkwMWY4N2NhOWMyMjU4OWNhMmFlNjQ4OCJ9fX0=");
        NamespacedKey nskey = new NamespacedKey(plugin, key);
        ShapedRecipe recipe = new ShapedRecipe(nskey, item);
        recipe.shape(shape[0], shape[1], shape[2]);

        for (String ingredient : ingredients.keySet()) {
            recipe.setIngredient(ingredient.charAt(0), ingredients.get(ingredient));
        }
        plugin.getServer().addRecipe(recipe);
    }
}
