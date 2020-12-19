package com.gmail.medievalcookery;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;

public class StorageSubsystem {
    // TODO: Move all of the code in this class somewhere more appropriate
    private static final Map<String, Integer> SPOIL_TIMES = new HashMap<String, Integer>() {{
        put("Salmon Roll", 10);
    }};

    // TODO: Move this to a localization system
    public static String createdText = "Created:";
    public static String expiryDateText = "Expiry Date:";
    public static String valuesLoadedText = "Values Loaded!";
    public static String noPermsText = "Sorry! In order to use this command, you need the following permission: 'fs.reload'";
    public static String spoiledFoodName = "Spoiled Food";
    public static String spoiledFoodLore = "This food has gone bad.";

    public static int getSpoilTime(String type) {
//        Integer time = SPOIL_TIMES.get(type);
//        return time != null ? time : 5; // If no spoil-time is found in the lookup table, just assume 5 minutes.
        return 10; // The above code wasn't working for some reason, I just set all of them to 10 minute spoil time until fixed.
    }


}