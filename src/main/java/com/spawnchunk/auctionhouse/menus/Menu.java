package com.spawnchunk.auctionhouse.menus;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.menus.ItemTag;
import com.spawnchunk.auctionhouse.menus.MenuItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Menu {
    private final String title;
    private final int size;
    private Inventory inventory;
    private final Map<Integer, ItemTag> tags = new HashMap<Integer, ItemTag>();

    public Menu(String title, int size) {
        this.title = title;
        this.size = size;
        this.inventory = Bukkit.createInventory(null, (int)size, (String)title);
    }

    public String getTitle() {
        return this.title;
    }

    public int getSize() {
        return this.size;
    }

    public List<HumanEntity> getViewers() {
        return this.inventory.getViewers();
    }

    public ItemStack getItem(int slot) {
        return this.inventory.getItem(slot);
    }

    public ItemStack[] getContents() {
        return this.inventory.getContents();
    }

    public ItemStack[] getStorageContents() {
        return this.inventory.getStorageContents();
    }

    public void setMaxStackSize(int size) {
        this.inventory.setMaxStackSize(size);
    }

    public void setItem(int slot, ItemStack item) {
        this.inventory.setItem(slot, item);
    }

    public void setContents(ItemStack[] itemStacks) {
        this.inventory.setContents(itemStacks);
    }

    public void setStorageContents(ItemStack[] itemStacks) {
        this.inventory.setStorageContents(itemStacks);
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setItem(int slot, MenuItem menuItem) {
        this.inventory.setItem(slot, menuItem.getItem());
    }

    public void setItemAmount(int slot, int amount) {
        ItemStack item = this.inventory.getItem(slot);
        if (item == null || item.getType().equals((Object)Material.AIR)) {
            return;
        }
        item.setAmount(amount);
        this.inventory.setItem(slot, item);
    }

    public void setItemName(int slot, String name) {
        ItemStack item = this.inventory.getItem(slot);
        if (item == null || item.getType().equals((Object)Material.AIR)) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(name);
        item.setItemMeta(meta);
        this.inventory.setItem(slot, item);
    }

    public void setItemLore(int slot, List<String> lore) {
        ItemMeta meta;
        ItemStack item = this.inventory.getItem(slot);
        if (item == null || item.getType().equals((Object)Material.AIR) || lore.isEmpty()) {
            return;
        }
        ItemMeta itemMeta = meta = item.getItemMeta() != null ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        if (item.getType() == Material.PLAYER_HEAD) {
            item = AuctionHouse.nms.setLore(item, lore);
        } else if (meta != null) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        this.inventory.setItem(slot, item);
    }

    public void replaceItemLore(int slot, String search, String replace) {
        List<String> lore;
        ItemStack item = this.inventory.getItem(slot);
        if (item == null || item.getType().equals((Object)Material.AIR)) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        List<String> list = lore = meta.getLore() != null ? meta.getLore() : new ArrayList<String>();
        if (lore.isEmpty()) {
            return;
        }
        boolean changed = false;
        for (String line : lore) {
            int index = lore.indexOf(line);
            if (!line.contains(search)) continue;
            lore.set(index, replace);
            changed = true;
        }
        if (changed) {
            if (item.getType() == Material.PLAYER_HEAD) {
                item = AuctionHouse.nms.setLore(item, lore);
            } else {
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            this.inventory.setItem(slot, item);
        }
    }

    public void setTag(int slot, ItemTag tag) {
        this.tags.put(slot, tag);
    }

    public ItemTag getTag(int slot) {
        return this.tags.get(slot);
    }

    public void removeItem(int slot) {
        this.tags.remove(slot);
        this.inventory.setItem(slot, new ItemStack(Material.AIR));
    }

    public void removeTag(int slot) {
        this.tags.remove(slot);
    }
}

