package com.spawnchunk.auctionhouse.util;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryUtil {
    public static int availableSlots(PlayerInventory inventory) {
        int count = 0;
        for (int i = 0; i < 45; ++i) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().equals((Object)Material.AIR)) continue;
            ++count;
        }
        return count;
    }

    public static int getFirstAvailableSlot(PlayerInventory inventory) {
        for (int i = 0; i < 45; ++i) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().equals((Object)Material.AIR)) continue;
            return i;
        }
        return -1;
    }

    public static String getTitle(HumanEntity he) {
        InventoryView view = he.getOpenInventory();
        try {
            return view.getTitle();
        }
        catch (IllegalStateException illegalStateException) {
            return "";
        }
    }
}

