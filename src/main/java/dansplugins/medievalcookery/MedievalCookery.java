package dansplugins.medievalcookery;

import dansplugins.medievalcookery.listeners.EatListener;
import dansplugins.medievalcookery.listeners.JoinListener;
import dansplugins.medievalcookery.services.ConfigService;
import dansplugins.medievalcookery.trace.TraceClient;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MedievalCookery extends JavaPlugin {
    private final String metadataPrefix = "Cookery";
    private final String metadataKeyIsEating = "IsEating";
    private final String metadataKeyItemName = "ItemName";
    private List<CustomFoodRecipe> recipes = new ArrayList<>();

    private final ConfigService configService = new ConfigService(this);

    // A no-op until the config has been read, so anything reporting before
    // onEnable() finishes has something safe to report to.
    private TraceClient trace = TraceClient.disabled();

    @Override
    public void onEnable() {
        // The plugin had no config.yml before usage reporting; recipes live in recipes.yml.
        // The bundled config.yml is written out on first start, and is only read after that.
        configService.saveDefaultConfig();

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
        trace = TraceClient.builder(configService.getUsageReportingEndpoint(), getName())
                .key(configService.getUsageReportingKey())
                .enabled(configService.isUsageReportingEnabled())
                .serverWideConfig(getDataFolder().getParentFile())
                .logger(getLogger())
                .build();
        logUsageReportingState();
        trace.report("startup", null, Collections.singletonMap("version", getDescription().getVersion()));
    }

    // Said on every startup so an operator can see reporting is on, and why it is off, from
    // the console alone. The wording is shared by every plugin that reports to trace.
    private void logUsageReportingState() {
        if (trace.isEnabled()) {
            getLogger().info("Usage reporting is on: " + getName() + " sends its name and version to "
                    + configService.getUsageReportingEndpoint()
                    + " - nothing about players or the server. Turn it off with usage-reporting.enabled: false"
                    + " in this plugin's config.yml, or for every plugin with enabled: false in"
                    + " plugins/trace/config.yml. Details: https://github.com/Stephenson-Software/trace#usage-reporting");
        } else {
            getLogger().info("Usage reporting is off (" + trace.disabledReason() + ").");
        }
    }

    @Override
    public void onDisable() {
        trace.close();

        System.out.println(("--- Disabling Medieval-Cookery --------"));
    }

    public void startPlayerEating(Player player, String itemName) {
        player.setMetadata(metadataPrefix + metadataKeyIsEating, new FixedMetadataValue(this, true));
        player.setMetadata(metadataPrefix + metadataKeyItemName, new FixedMetadataValue(this, itemName));
    }
    
    public void endPlayerEating(Player player) {
        player.setMetadata(metadataPrefix + metadataKeyIsEating, new FixedMetadataValue(this, false));
    }

    public String getPlayerEatingItemName(Player player) {
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

    public boolean hasRecipeName(String name) {
        for (CustomFoodRecipe recipe : recipes) {
            if (recipe.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public CustomFoodRecipe getRecipeByName(String name) {
        for (CustomFoodRecipe recipe : recipes) {
            if (recipe.name.equalsIgnoreCase(name)) {
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
