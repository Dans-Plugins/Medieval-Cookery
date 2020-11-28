package com.gmail.medievalcookery;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;

public class StorageSubsystem {

    private static final Map<String, Integer> SPOIL_TIMES = new HashMap<String, Integer>() {{
        put("Salmon Roll", 10);
    }};

    public static String createdText = "Created:";
    public static String expiryDateText = "Expiry Date:";
    public static String valuesLoadedText = "Values Loaded!";
    public static String noPermsText = "Sorry! In order to use this command, you need the following permission: 'fs.reload'";
    public static String spoiledFoodName = "Spoiled Food";
    public static String spoiledFoodLore = "This food has gone bad.";

    public static int getTime(String type) {
        Integer time = SPOIL_TIMES.get(type);
        return time != null ? time : 0;
    }


}