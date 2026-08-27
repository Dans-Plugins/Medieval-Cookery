package dansplugins.medievalcookery.services;

import dansplugins.medievalcookery.CustomFoodRecipe;
import dansplugins.medievalcookery.MedievalCookery;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigService {
    private final MedievalCookery medievalCookery;

    private final String dataFolder = "./plugins/MedievalCookery/";

    private final File recipesFile = new File(dataFolder, "recipes.yml");
    private final FileConfiguration recipesConfig;

    public ConfigService(MedievalCookery medievalCookery) {
        this.medievalCookery = medievalCookery;
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
            int hunger = recipesConfig.getInt("recipes." + recipeKey + ".hungerDecrease", 1);
            String afterEatItem = recipesConfig.getString("recipes." + recipeKey + ".afterEatItem", "");
            String texture = recipesConfig.getString("recipes." + recipeKey + ".textureBase64", "");

            if (recipeName.isEmpty()) {
                System.out.println("Error loading recipe " + recipeKey + ": no 'name' is configured.");
                continue;
            }

            Map<String, Material> ingredients = readSymbols(
                    recipesConfig.getConfigurationSection("recipes." + recipeKey + ".symbols"), recipeKey);
            if (ingredients == null || !isShapeUsable(recipeShape, ingredients.keySet(), recipeKey)) {
                System.out.println("Error loading recipe " + recipeKey);
                continue;
            }

            Material mat = null;
            if (!afterEatItem.isEmpty()) {
                mat = Material.getMaterial(afterEatItem);
                if (mat == null) {
                    System.out.println("[MedievalCookery] Error: Could not load material '" + afterEatItem + "' defined in the key afterEatItem of recipe '" + recipeName + "' in recipes.yml. Recipe will still load but with no afterEatItem behaviour.");
                }
            }

            recipeList.add(new CustomFoodRecipe(recipeKey, recipeName, recipeShape, ingredients, texture, medievalCookery, hunger, mat));
            System.out.println("Loaded recipe " + recipeName);
        }
        System.out.println(String.format("Loaded %d custom recipes.", recipeList.size()));
        return recipeList;
    }

    /**
     * Reads one recipe's symbol-to-material mapping.
     *
     * Returns null when the mapping cannot be used as configured, so that the caller skips
     * that single recipe rather than letting a malformed entry stop the whole plugin from
     * enabling. Every symbol is resolved to a material here, so a recipe that reaches
     * {@link CustomFoodRecipe} is known to name only materials Bukkit recognises.
     */
    static Map<String, Material> readSymbols(ConfigurationSection symbols, String recipeKey) {
        if (symbols == null) {
            System.out.println("Error in recipe '" + recipeKey + "': no 'symbols' section is configured.");
            return null;
        }
        Map<String, Material> ingredients = new LinkedHashMap<String, Material>();
        for (String symbol : symbols.getKeys(false)) {
            if (symbol.length() != 1) {
                System.out.println("Error in recipe '" + recipeKey + "': the symbol '" + symbol + "' must be a single character.");
                return null;
            }
            String matName = symbols.getString(symbol, "");
            if (matName.isEmpty()) {
                System.out.println("Error in recipe '" + recipeKey + "': the symbol '" + symbol + "' names no material.");
                return null;
            }
            Material material = Material.getMaterial(matName);
            if (material == null) {
                System.out.println("Error in recipe '" + recipeKey + "': the symbol '" + symbol + "' references a material '" + matName + "' which could not be found.");
                return null;
            }
            ingredients.put(symbol, material);
        }
        if (ingredients.isEmpty()) {
            System.out.println("Error in recipe '" + recipeKey + "': its 'symbols' section declares no symbols.");
            return null;
        }
        return ingredients;
    }

    /**
     * Checks a recipe's crafting pattern against the symbols it declares.
     *
     * Bukkit's {@code ShapedRecipe} rejects a pattern that is not rectangular, and rejects an
     * ingredient whose character the pattern never uses, by throwing from the constructor. Both
     * are configuration mistakes rather than programming errors, so they are reported and the
     * recipe is skipped instead.
     */
    static boolean isShapeUsable(String[] shape, Set<String> declaredSymbols, String recipeKey) {
        if (shape.length != 3) {
            System.out.println("Error in recipe '" + recipeKey + "': its 'recipe' pattern must be exactly 3 rows, but " + shape.length + " were configured.");
            return false;
        }
        Set<String> usedSymbols = new HashSet<String>();
        for (String row : shape) {
            if (row.length() != 3) {
                System.out.println("Error in recipe '" + recipeKey + "': the pattern row '" + row + "' must be exactly 3 characters wide.");
                return false;
            }
            for (char slot : row.toCharArray()) {
                if (slot != ' ') {
                    usedSymbols.add(String.valueOf(slot));
                }
            }
        }
        for (String symbol : usedSymbols) {
            if (!declaredSymbols.contains(symbol)) {
                System.out.println("Error in recipe '" + recipeKey + "': the pattern uses the symbol '" + symbol + "', which its 'symbols' section does not declare.");
                return false;
            }
        }
        for (String symbol : declaredSymbols) {
            if (!usedSymbols.contains(symbol)) {
                System.out.println("Error in recipe '" + recipeKey + "': the symbol '" + symbol + "' is declared but never used by the pattern, which Bukkit rejects.");
                return false;
            }
        }
        return true;
    }

    public void saveRecipes(boolean replace) {
        medievalCookery.saveResource("recipes.yml", replace);
    }

}
