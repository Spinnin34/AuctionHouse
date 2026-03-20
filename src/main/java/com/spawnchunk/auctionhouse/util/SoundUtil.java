package com.spawnchunk.auctionhouse.util;

import com.spawnchunk.auctionhouse.config.Config;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {
    public static void clickSound(Player player) {
        try {
            Sound sound = Sound.valueOf((String)Config.click_sound.toUpperCase().replace(".", "_"));
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void failSound(Player player) {
        try {
            Sound sound = Sound.valueOf((String)Config.fail_sound.toUpperCase().replace(".", "_"));
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void dropSound(Player player) {
        try {
            Sound sound = Sound.valueOf((String)Config.drop_sound.toUpperCase().replace(".", "_"));
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void soldSound(Player player) {
        try {
            Sound sound = Sound.valueOf((String)Config.sold_sound.toUpperCase().replace(".", "_"));
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

