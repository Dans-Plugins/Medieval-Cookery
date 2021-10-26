package com.gmail.medievalcookery;

import com.gmail.medievalcookery.services.StorageService;
import com.gmail.medievalcookery.services.TimeStampService;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
    public int hungerDecrease = 2;
    public Material afterEatItem = null;

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
            int time = StorageService.getSpoilTime(name);
            TimeStampService.assignTimeStamp(item, time);
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
            HashMap<String, Material> ingredients,
            String texture, int hungerAmt, Material afterEatItemMaterial
    ) {
        boolean error = false;
        key = recipeKey;
        name = recipeName;

        afterEatItem = afterEatItemMaterial;
        ItemStack item = itemWithBase64(new ItemStack(Material.PLAYER_HEAD, 1), texture);
        NamespacedKey nskey = new NamespacedKey(MedievalCookery.getInstance(), key);
        ShapedRecipe recipe = new ShapedRecipe(nskey, item);
        recipe.shape(shape[0], shape[1], shape[2]);
        hungerDecrease = hungerAmt;
        for (String ingredient : ingredients.keySet()) {
            if (!ingredients.containsKey(ingredient)) {
                System.out.println("Ingredient key '" + ingredient + "' not found in materials hash.");
                error = true;
            } else {
                if (ingredients.get(ingredient) != null) {
                    recipe.setIngredient(ingredient.charAt(0), ingredients.get(ingredient));
                } else {
                    System.out.println("Something went wrong initializing custom food recipe " + recipeKey + ": Please ensure your symbols definition items are all valid Bukkit material names.");
                    error = true;
                }
            }
        }
        if (!error) {
            MedievalCookery.getInstance().getServer().addRecipe(recipe);
            System.out.println("Registered custom recipe " + recipeKey + " with Bukkit");
        }
    }
}
