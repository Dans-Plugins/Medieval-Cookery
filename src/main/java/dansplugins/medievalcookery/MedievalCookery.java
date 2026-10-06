package dansplugins.medievalcookery;

import dansplugins.medievalcookery.listeners.EatListener;
import dansplugins.medievalcookery.listeners.JoinListener;
import dansplugins.medievalcookery.services.ConfigService;
import dansplugins.medievalcookery.trace.TraceClient;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URL;
import java.util.*;

public class MedievalCookery extends JavaPlugin {
    private final String metadataPrefix = "Cookery";
    private final String metadataKeyIsEating = "IsEating";
    private final String metadataKeyItemName = "ItemName";
    private List<CustomFoodRecipe> recipes = new ArrayList<>();
    private NamespacedKey foodTagKey;

    private final ConfigService configService = new ConfigService(this);

    // A no-op until the config has been read, so anything reporting before
    // onEnable() finishes has something safe to report to.
    private TraceClient trace = TraceClient.disabled();

    @Override
    public void onEnable() {
        // The plugin had no config.yml before usage reporting; recipes live in recipes.yml.
        // The bundled config.yml is written out on first start, and is only read after that.
        configService.saveDefaultConfig();

        // Needed by every recipe as it is registered, to tag the food it produces.
        foodTagKey = new NamespacedKey(this, "recipe");

        // loadRecipes returns null when recipes.yml could not be read at all. The field keeps its
        // empty list in that case, because it is now consulted on every interaction and iterating
        // a null there would throw once per right-click rather than once at startup.
        List<CustomFoodRecipe> loadedRecipes = configService.loadRecipes();
        if (loadedRecipes != null) {
            recipes = loadedRecipes;
        }

        for (Player player : getServer().getOnlinePlayers()) {
            endPlayerEating(player);
        }

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new EatListener(this), this);

        // usage reporting: one event on enable; see config.yml. The plugin has no commands,
        // so there is nothing else to report. The usage-reporting block is on disk for every
        // server: config.yml did not exist before usage reporting, and saveDefaultConfig()
        // above writes the bundled file whenever it is absent.
        trace = TraceClient.builder(configService.getUsageReportingEndpoint(), getName(), getDescription().getVersion())
                .key(configService.getUsageReportingKey())
                .enabled(configService.isUsageReportingEnabled())
                .serverWideConfig(getDataFolder().getParentFile())
                .logger(getLogger())
                .build();
        logUsageReportingState();
        trace.report("startup");
    }

    // Said on every startup so an operator can see reporting is on, and why it is off, from
    // the console alone. The wording is shared by every plugin that reports to trace.
    private void logUsageReportingState() {
        if (trace.isEnabled()) {
            getLogger().info("Usage reporting is on: " + getName() + " sends its name and version to "
                    + configService.getUsageReportingEndpoint()
                    + ", plus a random server ID (server-id in plugins/trace/config.yml) - nothing about players. Turn it off with usage-reporting.enabled: false"
                    + " in this plugin's config.yml, or for every plugin with enabled: false in"
                    + " plugins/trace/config.yml. Details: https://danielstephenson.dev/usage-reporting");
        } else {
            getLogger().info("Usage reporting is off (" + trace.disabledReason() + ").");
        }
    }

    @Override
    public void onDisable() {
        trace.close();

        System.out.println(("--- Disabling Medieval-Cookery --------"));
    }

    public void startPlayerEating(Player player, String recipeId) {
        player.setMetadata(metadataPrefix + metadataKeyIsEating, new FixedMetadataValue(this, true));
        player.setMetadata(metadataPrefix + metadataKeyItemName, new FixedMetadataValue(this, recipeId));
    }
    
    public void endPlayerEating(Player player) {
        player.setMetadata(metadataPrefix + metadataKeyIsEating, new FixedMetadataValue(this, false));
    }

    public String getPlayerEatingRecipeId(Player player) {
        if (player.hasMetadata(metadataPrefix + metadataKeyItemName))
        {
            List<MetadataValue> values = player.getMetadata(metadataPrefix + metadataKeyItemName);
            for (MetadataValue v : values) {
                if (v.getOwningPlugin().getName().equalsIgnoreCase(getName())) {
                    try {
                        return v.asString();
                    } catch(Exception e) { }
                }
            }
        }
        return "";
    }

    public boolean isPlayerEating(Player player) {
        if (player.hasMetadata(metadataPrefix + metadataKeyIsEating))
        {
            List<MetadataValue> values = player.getMetadata(metadataPrefix + metadataKeyIsEating);
            for (MetadataValue v : values) {
                if (v.getOwningPlugin().getName().equalsIgnoreCase(getName())) {
                    if (v.asBoolean() == true) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public NamespacedKey getFoodTagKey() {
        return foodTagKey;
    }

    /**
     * Returns the recipe a stack is a food of, or null when it is not one of the loaded recipes'
     * foods. A tagged food is identified by its tag alone, so a food whose recipe has since been
     * removed is not eaten as anything else. An untagged head is accepted only as a food crafted
     * before foods were tagged (see {@link CustomFoodItem#isUntaggedFood}).
     */
    public CustomFoodRecipe recipeOf(ItemStack item) {
        String recipeId = CustomFoodItem.recipeIdOf(item, foodTagKey);
        if (recipeId != null) {
            return getRecipeById(recipeId);
        }
        String name = CustomFoodItem.nameOf(item);
        if (name == null) {
            return null;
        }
        URL headSkin = CustomFoodItem.skinOf(item);
        for (CustomFoodRecipe recipe : recipes) {
            if (CustomFoodItem.isUntaggedFood(name, headSkin, recipe.name, recipe.skin())) {
                return recipe;
            }
        }
        return null;
    }

    public CustomFoodRecipe getRecipeById(String recipeId) {
        for (CustomFoodRecipe recipe : recipes) {
            if (recipe.key.equals(recipeId)) {
                return recipe;
            }
        }
        return null;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public String getMetadataKeyIsEating() {
        return metadataKeyIsEating;
    }

    public String getMetadataKeyItemName() {
        return metadataKeyItemName;
    }

}
