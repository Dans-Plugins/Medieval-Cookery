package com.gmail.medievalcookery;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigManager {

    private String dataFolder = "./plugins/MedievalCookery/";

    private File recipesFile = new File(dataFolder, "recipes.yml");
    private FileConfiguration recipesConfig;

    public ConfigManager () {
        if (!recipesFile.exists()) {
            saveRecipes(false);
        }
        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);
        if (recipesConfig == null) {
            System.out.println("Sorry, there was a problem loading recipes config from " + recipesFile);
        } else {
            System.out.println("Config file " + recipesFile + " loaded.");
        }
    }

    public FileConfiguration getRecipeConfig() {
        return recipesConfig;
    }

    public List<CustomFoodRecipe> loadRecipes() {
        if (recipesConfig == null) {
            System.out.println("Sorry, recipes config not yet loaded from " + recipesFile);
            return null;
        }
        if (recipesConfig.getConfigurationSection("recipes") == null) {
            System.out.println("Sorry, could not locate configuration section 'recipes' in recipes config file. Config contains these keys:");
            for (String key : recipesConfig.getKeys(false)) {
                System.out.println(key);
            }
            return null;
        }
        List<CustomFoodRecipe> recipeList = new ArrayList<CustomFoodRecipe>();

        for (String recipeKey : recipesConfig.getConfigurationSection("recipes").getKeys(false)) {

            String recipeName = recipesConfig.getString("recipes." + recipeKey + ".name", "");
            String[] recipeShape = recipesConfig.getStringList("recipes." + recipeKey + ".recipe").toArray(new String[0]);
            List<String> symbolsDef = new ArrayList();
            List<Material> materialsDef = new ArrayList<Material>();
            int hunger = recipesConfig.getInt("recipes." + recipeKey + ".hungerDecrease", 1);
            String afterEatItem = recipesConfig.getString("recipes." + recipeKey + ".afterEatItem", "");
            for (String symbol : recipesConfig.getConfigurationSection("recipes." + recipeKey + ".symbols").getKeys(false)) {
                String matName = recipesConfig.getString("recipes." + recipeKey + ".symbols." + symbol, "");
                Material m = null;
                System.out.println("symbol: " + matName);
                if (matName == "") {
                    System.out.println("Error in recipe '" + recipeKey + ": the symbol '" + symbol + "' references a material '" + matName + "' which could not be found.");
                } else {
                    m = Material.getMaterial(matName);
                    symbolsDef.add(symbol);
                    materialsDef.add(m);
                }
            }
            String texture = recipesConfig.getString("recipes." + recipeKey + ".textureBase64", "");

            if (recipeName != "" && symbolsDef.size() > 0 && materialsDef.size() > 0) {
                Material mat = null;
                if (!afterEatItem.equalsIgnoreCase("")) {
                    try {
                        mat = Material.getMaterial(afterEatItem);
                    } catch (Exception e) {
                        System.out.println("[MedievalCookery] Error: Could not load material '" + afterEatItem + "' defined in the key afterEatItem of recipe '" + recipeName + "' in plugin.yml. Recipe will still load but with no afterEatItem behaviour.");
                    }
                }
                CustomFoodRecipe recipe = new CustomFoodRecipe(recipeKey, recipeName, recipeShape, MaterialMap(symbolsDef.toArray(new String[0])
                        , materialsDef.toArray(new Material[0])), texture, hunger, mat);
                recipeList.add(recipe);
                System.out.println("Loaded recipe " + recipeName);
            } else {
                System.out.println("Error loading recipe " + recipeKey);
            }
        }
        System.out.println(String.format("Loaded %d custom recipes.", recipeList.size()));
        return recipeList;
    }

    private HashMap<String, Material> MaterialMap(String[] keys, Material[] values) {
        HashMap<String, Material> map = new HashMap<String, Material>();
        int i = 0;
        for(String key : keys) {
            map.put(key, values[i]);
            i++;
        }
        return map;
    }

    public void saveRecipes(boolean replace) {
        MedievalCookery.getInstance().saveResource("recipes.yml", replace);
    }

}
