package com.spawnchunk.auctionhouse.nms;

import java.util.List;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

public interface NMS {
    public String getNBTString(ItemStack var1);

    public ItemStack setNBTString(ItemStack var1, String var2);

    public ItemStack addNBTLocator(ItemStack var1);

    public boolean hasNBTLocator(ItemStack var1);

    public ItemStack getCustomSkull(String var1);

    public boolean isDye(ItemStack var1);

    public boolean isContainer(ItemStack var1);

    public Map<Integer, ItemStack> getContainerItems(ItemStack var1);

    public List<String> getMobs(ItemStack var1);

    public int getCustomModelData(ItemStack var1);

    public ItemStack setCustomModelData(ItemStack var1, int var2);

    public ItemStack setLore(ItemStack var1, List<String> var2);

    public ItemStack setDisplayName(ItemStack var1, String var2);

    public boolean hasPersistentDataKey(ItemStack var1, String var2);

    public Object getPersistentDataKey(ItemStack var1, String var2);

    public Map<String, Object> getPersistentData(ItemStack var1);

    public ItemStack setPersistentDataKey(ItemStack var1, String var2, Object var3);

    public ItemStack deserialize(String var1);

    public ItemStack updateItem(ItemStack var1);

    public String parseInternal(String var1);
}

