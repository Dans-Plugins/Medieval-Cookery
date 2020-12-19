package com.gmail.medievalcookery;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Calendar.MINUTE;

public class TimeStampSubsystem {

    public static String pattern = "MM/dd/yyyy HH:mm:ss";

    public static ItemStack assignTimeStamp(ItemStack item, int minutesUntilSpoilage) {
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setLore(asList(
                    "",
                    ChatColor.WHITE + StorageSubsystem.createdText,
                    ChatColor.WHITE + getDateString(),
                    "",
                    ChatColor.WHITE + StorageSubsystem.expiryDateText,
                    ChatColor.WHITE + getDateStringPlusTime(minutesUntilSpoilage)
            ));

            item.setItemMeta(meta);
        }

        return item;
    }

    private static String getDateString() {
        String dateString = new SimpleDateFormat(pattern).format(getDate());
        return dateString;
    }

    private static Date getDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    private static String getDateStringPlusTime(int minutes) {
        return new SimpleDateFormat(pattern).format(getDatePlusTime(minutes));
    }

    private static Date getDatePlusTime(int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(MINUTE, minutes);
        return calendar.getTime();
    }

    public static boolean timeStampAssigned(ItemStack item) {
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                List<String> lore = meta.getLore();

                if (lore != null) {
                    // System.out.println("Debug] Time stamp is already assigned to this item!");
                    return lore.toString().contains(StorageSubsystem.expiryDateText);
                }
            }
        }

        // System.out.println("[Debug] Time stamp is not yet applied to this item!");
        return false;
    }

    public static boolean timeReached(ItemStack item) {
        String timestamp = getTimeStamp(item);

        if (timestamp != null) {

            DateFormat df = new SimpleDateFormat(pattern);

            timestamp = timestamp.substring(2);

            Date date = null;
            try {
                date = df.parse(timestamp);
            } catch (Exception e) {
                //System.out.println("Something went wrong parsing timestamp " + timestamp + " with pattern " + pattern);
            }

            if (date != null) {
                return getDate().after(date);
            }
        }
        return false;
    }

    private static String getTimeStamp(ItemStack item) {
        if (timeStampAssigned(item)) {
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                List<String> lore = meta.getLore();

                if (lore != null && lore.size() > 5) {
                    return lore.get(5);
                }
            }
        }
        return null;
    }

}