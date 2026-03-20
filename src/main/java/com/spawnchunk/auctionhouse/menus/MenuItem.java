package com.spawnchunk.auctionhouse.menus;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.util.ItemUtil;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import com.spawnchunk.auctionhouse.util.PlayerUtil;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class MenuItem {
    private final ItemStack item;

    public MenuItem(String name, String key, int amount, List<String> lore) {
        this(null, name, key, amount, lore, null);
    }

    public MenuItem(String name, String key, int amount, List<String> lore, String nbt) {
        this(null, name, key, amount, lore, nbt);
    }

    public MenuItem(Player player, String name, String key, int amount, List<String> lore) {
        this(player, name, key, amount, lore, null);
    }

    private MenuItem(Player player, String name, String key, int amount, List<String> lore, String nbt) {
        ItemMeta meta;
        ItemStack item;
        if (key.equals("auctionhouse:player_head")) {
            if (player != null) {
                item = PlayerUtil.getPlayerHead(player);
                item.setAmount(amount);
            } else {
                item = new ItemStack(Material.PLAYER_HEAD, amount);
            }
        } else if (key.startsWith("texture:")) {
            String texture = key.replace("texture:", "");
            item = AuctionHouse.nms.getCustomSkull(texture);
            item.setAmount(amount);
        } else if (key.startsWith("hdb:")) {
            if (AuctionHouse.hdb != null) {
                String id = key.replace("hdb:", "");
                if (AuctionHouse.hdb.isHead(id)) {
                    item = AuctionHouse.hdb.getItemHead(id);
                    item.setAmount(amount);
                } else {
                    item = new ItemStack(Material.AIR, 1);
                }
            } else {
                item = new ItemStack(Material.AIR, 1);
            }
        } else {
            Material material = Material.matchMaterial((String)key);
            if (material == null) {
                material = Material.AIR;
            }
            item = new ItemStack(material, amount);
        }
        if (nbt != null && !nbt.isEmpty()) {
            item = AuctionHouse.nms.setNBTString(item, nbt);
        }
        ItemMeta itemMeta = meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        if (meta != null) {
            if (name != null && !name.isEmpty()) {
                item = AuctionHouse.nms.setDisplayName(item, name);
            }
            if (lore != null && !lore.isEmpty()) {
                for (String s : lore) {
                    lore.set(lore.indexOf(s), MessageUtil.sectionSymbol(s));
                }
                item = item.getType() == Material.PLAYER_HEAD ? AuctionHouse.nms.setLore(item, lore) : AuctionHouse.nms.setLore(item, lore);
            }
            if (key.endsWith("shulker_box") || key.endsWith("chest")) {
                item = ItemUtil.hidePotionEffects(item);
            }
        }
        this.item = item = AuctionHouse.nms.addNBTLocator(item);
    }

    public ItemStack getItem() {
        return this.item;
    }
}

