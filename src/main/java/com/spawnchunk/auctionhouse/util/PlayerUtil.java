package com.spawnchunk.auctionhouse.util;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.modules.Economy;
import com.spawnchunk.auctionhouse.util.SoundUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerUtil {
    private static final Map<UUID, Long> lastclick = new HashMap<UUID, Long>();
    private static final Map<UUID, Integer> lastcount = new HashMap<UUID, Integer>();

    public static OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer[] ops;
        for (OfflinePlayer op : ops = AuctionHouse.plugin.getServer().getOfflinePlayers()) {
            String n = op.getName();
            if (n == null || !n.equals(name)) continue;
            return op;
        }
        return null;
    }

    public static ItemStack getPlayerHead(Player player) {
        if (player != null) {
            OfflinePlayer op = PlayerUtil.getOfflinePlayer(player.getName());
            ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta)meta;
                skullMeta.setOwningPlayer(op);
                item.setItemMeta(meta);
                return item;
            }
        }
        return new ItemStack(Material.PLAYER_HEAD, 1);
    }

    public static Player getPlayer(UUID uuid) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId() != uuid) continue;
            return p;
        }
        return null;
    }

    public static Player getPlayer(String name) {
        OfflinePlayer op = PlayerUtil.getOfflinePlayer(name);
        if (op != null) {
            return PlayerUtil.getPlayer(op.getUniqueId());
        }
        return null;
    }

    public static double getPlayerBalance(Player player, String world) {
        return Economy.getBalance((OfflinePlayer)player, world);
    }

    public static boolean spamCheck(Player player) {
        if (Config.spam_check) {
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            long duration = now - lastclick.getOrDefault(uuid, 0L);
            int count = lastcount.getOrDefault(uuid, 0);
            lastclick.put(uuid, now);
            if (duration < 1000L) {
                if (count > 5) {
                    SoundUtil.failSound(player);
                    return true;
                }
                lastcount.put(uuid, count + 1);
            } else {
                lastcount.put(uuid, 0);
            }
        }
        return false;
    }
}

