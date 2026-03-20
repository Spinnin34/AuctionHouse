package com.spawnchunk.auctionhouse.nms;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.modules.PublicBukkitValues;
import com.spawnchunk.auctionhouse.util.ItemUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

/**
 * NMS implementation for Paper 1.21.4+.
 * Uses Bukkit/Paper API instead of direct NMS access where possible.
 */
public class v1_21_R3 implements NMS {

    private static final NamespacedKey AH_KEY = new NamespacedKey(AuctionHouse.plugin, "AuctionHouse");

    @Override
    public String getNBTString(ItemStack itemstack) {
        if (itemstack == null || itemstack.getType() == Material.AIR) return null;
        try {
            String type = itemstack.getType().getKey().toString();
            ItemMeta meta = itemstack.getItemMeta();
            if (meta == null) return type;
            String metaSnbt = meta.getAsString();
            if (metaSnbt == null || metaSnbt.isEmpty() || metaSnbt.equals("{}")) return type;
            // getAsString() returns SNBT compound format: {"comp":value,...}
            // createItemStack() expects component format: [comp=value,...]
            // Convert the top-level compound to component bracket format
            return convertSnbtToComponentFormat(type, metaSnbt);
        } catch (Exception e) {
            return itemstack.getType().getKey().toString();
        }
    }

    @Override
    public ItemStack setNBTString(ItemStack itemstack, String nbt) {
        if (nbt == null || nbt.isEmpty()) return itemstack;
        try {
            String formatted = nbt;
            // Detect old SNBT compound format: type{"key":value,...}
            // and convert to component format: type[key=value,...]
            int braceIdx = nbt.indexOf('{');
            int bracketIdx = nbt.indexOf('[');
            if (braceIdx > 0 && (bracketIdx < 0 || bracketIdx > braceIdx)) {
                String typeKey = nbt.substring(0, braceIdx);
                String metaSnbt = nbt.substring(braceIdx);
                if (metaSnbt.equals("{}")) {
                    formatted = typeKey;
                } else {
                    formatted = convertSnbtToComponentFormat(typeKey, metaSnbt);
                }
            }
            return Bukkit.getItemFactory().createItemStack(formatted);
        } catch (Exception e) {
            AuctionHouse.logger.warning("Failed to parse item NBT: " + e.getMessage());
            if (Config.debug) {
                e.printStackTrace();
            }
            return itemstack;
        }
    }

    /**
     * Converts SNBT compound format to Minecraft component bracket format.
     * <p>
     * Input metaSnbt:  {"minecraft:custom_data":{...},"minecraft:lore":[...]}
     * Output:          typeKey[minecraft:custom_data={...},minecraft:lore=[...]]
     * <p>
     * The top-level keys have their quotes removed, and ':' separators become '='.
     * Nested values are copied verbatim.
     */
    private static String convertSnbtToComponentFormat(String typeKey, String metaSnbt) {
        if (metaSnbt == null || metaSnbt.length() < 2) return typeKey;
        // Remove outer braces of the compound
        String inner = metaSnbt.substring(1, metaSnbt.length() - 1).trim();
        if (inner.isEmpty()) return typeKey;

        StringBuilder result = new StringBuilder(typeKey);
        result.append('[');

        int pos = 0;
        int len = inner.length();
        boolean first = true;

        while (pos < len) {
            // Skip whitespace and commas between entries
            while (pos < len && (inner.charAt(pos) == ' ' || inner.charAt(pos) == ',')) pos++;
            if (pos >= len) break;

            if (!first) result.append(',');
            first = false;

            // Read key (may be quoted with double-quotes or unquoted)
            if (inner.charAt(pos) == '"') {
                pos++; // skip opening quote
                int keyStart = pos;
                while (pos < len && inner.charAt(pos) != '"') pos++;
                result.append(inner, keyStart, pos);
                pos++; // skip closing quote
            } else {
                int keyStart = pos;
                while (pos < len && inner.charAt(pos) != ':') pos++;
                result.append(inner, keyStart, pos);
            }

            // Skip the ':' separator between key and value
            if (pos < len && inner.charAt(pos) == ':') pos++;

            // Append '=' instead of ':'
            result.append('=');

            // Copy the value verbatim, respecting nested braces, brackets, and quotes
            int depth = 0;
            boolean inDoubleQuote = false;
            boolean inSingleQuote = false;

            while (pos < len) {
                char c = inner.charAt(pos);

                if (inDoubleQuote) {
                    result.append(c);
                    if (c == '\\' && pos + 1 < len) {
                        pos++;
                        result.append(inner.charAt(pos));
                    } else if (c == '"') {
                        inDoubleQuote = false;
                    }
                    pos++;
                    continue;
                }

                if (inSingleQuote) {
                    result.append(c);
                    if (c == '\\' && pos + 1 < len) {
                        pos++;
                        result.append(inner.charAt(pos));
                    } else if (c == '\'') {
                        inSingleQuote = false;
                    }
                    pos++;
                    continue;
                }

                if (c == '"') {
                    inDoubleQuote = true;
                    result.append(c);
                    pos++;
                } else if (c == '\'') {
                    inSingleQuote = true;
                    result.append(c);
                    pos++;
                } else if (c == '{' || c == '[') {
                    depth++;
                    result.append(c);
                    pos++;
                } else if (c == '}' || c == ']') {
                    depth--;
                    result.append(c);
                    pos++;
                } else if (c == ',' && depth == 0) {
                    // Top-level comma: end of this value
                    break;
                } else {
                    result.append(c);
                    pos++;
                }
            }
        }

        result.append(']');
        return result.toString();
    }

    @Override
    public ItemStack addNBTLocator(ItemStack itemstack) {
        if (itemstack == null || itemstack.getType() == Material.AIR) return itemstack;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta == null) return itemstack;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(AH_KEY, PersistentDataType.BYTE)) return itemstack;
        pdc.set(AH_KEY, PersistentDataType.BYTE, (byte) 1);
        itemstack.setItemMeta(meta);
        return itemstack;
    }

    @Override
    public boolean hasNBTLocator(ItemStack itemstack) {
        if (itemstack == null || itemstack.getType() == Material.AIR) return false;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(AH_KEY, PersistentDataType.BYTE);
    }

    @Override
    public ItemStack getCustomSkull(String texture) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return item;
        UUID uuid = UUID.randomUUID();
        PlayerProfile profile = Bukkit.createPlayerProfile(uuid, "");
        PlayerTextures textures = profile.getTextures();
        try {
            // The texture is a Base64-encoded JSON containing the skin URL
            String decoded = new String(Base64.getDecoder().decode(texture));
            // Extract URL from the JSON: {"textures":{"SKIN":{"url":"..."}}}
            int urlStart = decoded.indexOf("\"url\"");
            if (urlStart != -1) {
                int httpStart = decoded.indexOf("http", urlStart);
                int httpEnd = decoded.indexOf("\"", httpStart);
                String url = decoded.substring(httpStart, httpEnd);
                textures.setSkin(URI.create(url).toURL());
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            }
        } catch (Exception e) {
            AuctionHouse.logger.log(Level.WARNING, "Could not set skull texture", e);
        }
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public boolean isDye(ItemStack itemstack) {
        Material material = itemstack.getType();
        String key = material.getKey().getKey();
        return !key.isEmpty() && key.contains("dye");
    }

    @Override
    public boolean isContainer(ItemStack itemstack) {
        if (itemstack == null) return false;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta instanceof BlockStateMeta bsm) {
            BlockState state = bsm.getBlockState();
            return state instanceof Container;
        }
        // Also check for bundles in 1.21+
        if (meta instanceof BundleMeta) return true;
        return false;
    }

    @Override
    public Map<Integer, ItemStack> getContainerItems(ItemStack itemstack) {
        HashMap<Integer, ItemStack> containerItems = new HashMap<>();
        if (itemstack == null) return containerItems;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta instanceof BlockStateMeta bsm) {
            BlockState state = bsm.getBlockState();
            if (state instanceof Container container) {
                org.bukkit.inventory.Inventory inv = container.getInventory();
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack is = inv.getItem(i);
                    if (is != null && is.getType() != Material.AIR) {
                        containerItems.put(i, is);
                    }
                }
            }
        }
        if (meta instanceof BundleMeta bundleMeta) {
            List<ItemStack> items = bundleMeta.getItems();
            for (int i = 0; i < items.size(); i++) {
                containerItems.put(i, items.get(i));
            }
        }
        return containerItems;
    }

    @Override
    public List<String> getMobs(ItemStack itemstack) {
        ArrayList<String> mobs = new ArrayList<>();
        if (itemstack == null) return mobs;

        // Check if it's a spawn egg
        Material mat = itemstack.getType();
        String matName = mat.getKey().getKey();
        if (matName.endsWith("_spawn_egg")) {
            String mobName = matName.replace("_spawn_egg", "");
            mobs.add("minecraft:" + mobName);
            return mobs;
        }

        // Check if it's a spawner block
        ItemMeta meta = itemstack.getItemMeta();
        if (meta instanceof BlockStateMeta bsm) {
            try {
                BlockState state = bsm.getBlockState();
                if (state instanceof CreatureSpawner spawner) {
                    EntityType spawnedType = spawner.getSpawnedType();
                    if (spawnedType != null) {
                        mobs.add("minecraft:" + spawnedType.getKey().getKey());
                        return mobs;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Check PersistentDataContainer for custom spawner plugins
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            for (String customKey : PublicBukkitValues.customKeys) {
                NamespacedKey nk;
                try {
                    String[] parts = customKey.split(":");
                    if (parts.length == 2) {
                        nk = new NamespacedKey(parts[0], parts[1]);
                    } else {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
                if (pdc.has(nk, PersistentDataType.STRING)) {
                    String mob = pdc.get(nk, PersistentDataType.STRING);
                    if (mob != null && !mob.isEmpty()) {
                        mobs.add(mob);
                        return mobs;
                    }
                }
            }
        }

        // Default for spawner blocks
        if (mat == Material.SPAWNER) {
            mobs.add("minecraft:pig");
        }

        return mobs;
    }

    @Override
    public int getCustomModelData(ItemStack itemstack) {
        if (itemstack == null) return 0;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta != null && meta.hasCustomModelData()) {
            return meta.getCustomModelData();
        }
        return 0;
    }

    @Override
    public ItemStack setCustomModelData(ItemStack itemstack, int customModelData) {
        if (itemstack == null) return null;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            itemstack.setItemMeta(meta);
        }
        return itemstack;
    }

    @Override
    public ItemStack setLore(ItemStack itemstack, List<String> lore) {
        if (itemstack == null) return null;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta == null) return itemstack;
        meta.setLore(lore);
        itemstack.setItemMeta(meta);
        return itemstack;
    }

    @Override
    public ItemStack setDisplayName(ItemStack itemstack, String name) {
        if (itemstack == null) return null;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta == null) return itemstack;
        meta.setDisplayName(name);
        itemstack.setItemMeta(meta);
        return itemstack;
    }

    @Override
    public boolean hasPersistentDataKey(ItemStack itemstack, String key) {
        if (itemstack == null) return false;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey nk = parseNamespacedKey(key);
        if (nk == null) return false;
        return pdc.has(nk);
    }

    @Override
    public Object getPersistentDataKey(ItemStack itemstack, String key) {
        if (itemstack == null) return null;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey nk = parseNamespacedKey(key);
        if (nk == null) return null;
        // Try different types
        if (pdc.has(nk, PersistentDataType.STRING)) return pdc.get(nk, PersistentDataType.STRING);
        if (pdc.has(nk, PersistentDataType.BYTE)) return pdc.get(nk, PersistentDataType.BYTE);
        if (pdc.has(nk, PersistentDataType.SHORT)) return pdc.get(nk, PersistentDataType.SHORT);
        if (pdc.has(nk, PersistentDataType.INTEGER)) return pdc.get(nk, PersistentDataType.INTEGER);
        if (pdc.has(nk, PersistentDataType.LONG)) return pdc.get(nk, PersistentDataType.LONG);
        if (pdc.has(nk, PersistentDataType.FLOAT)) return pdc.get(nk, PersistentDataType.FLOAT);
        if (pdc.has(nk, PersistentDataType.DOUBLE)) return pdc.get(nk, PersistentDataType.DOUBLE);
        if (pdc.has(nk, PersistentDataType.BYTE_ARRAY)) return pdc.get(nk, PersistentDataType.BYTE_ARRAY);
        if (pdc.has(nk, PersistentDataType.INTEGER_ARRAY)) return pdc.get(nk, PersistentDataType.INTEGER_ARRAY);
        if (pdc.has(nk, PersistentDataType.LONG_ARRAY)) return pdc.get(nk, PersistentDataType.LONG_ARRAY);
        return null;
    }

    @Override
    public Map<String, Object> getPersistentData(ItemStack itemstack) {
        HashMap<String, Object> data = new HashMap<>();
        if (itemstack == null) return data;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta == null) return data;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        for (NamespacedKey nk : pdc.getKeys()) {
            String key = nk.toString();
            Object value = getPersistentDataKey(itemstack, key);
            if (value != null) {
                data.put(key, value);
            }
        }
        return data;
    }

    @Override
    public ItemStack setPersistentDataKey(ItemStack itemstack, String key, Object value) {
        if (itemstack == null) return null;
        ItemMeta meta = itemstack.getItemMeta();
        if (meta == null) return itemstack;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey nk = parseNamespacedKey(key);
        if (nk == null) return itemstack;

        if (value instanceof String v) pdc.set(nk, PersistentDataType.STRING, v);
        else if (value instanceof Byte v) pdc.set(nk, PersistentDataType.BYTE, v);
        else if (value instanceof Boolean) pdc.set(nk, PersistentDataType.BYTE, (byte) ((Boolean) value ? 1 : 0));
        else if (value instanceof Short v) pdc.set(nk, PersistentDataType.SHORT, v);
        else if (value instanceof Integer v) pdc.set(nk, PersistentDataType.INTEGER, v);
        else if (value instanceof Long v) pdc.set(nk, PersistentDataType.LONG, v);
        else if (value instanceof Float v) pdc.set(nk, PersistentDataType.FLOAT, v);
        else if (value instanceof Double v) pdc.set(nk, PersistentDataType.DOUBLE, v);
        else if (value instanceof byte[] v) pdc.set(nk, PersistentDataType.BYTE_ARRAY, v);
        else if (value instanceof int[] v) pdc.set(nk, PersistentDataType.INTEGER_ARRAY, v);
        else if (value instanceof long[] v) pdc.set(nk, PersistentDataType.LONG_ARRAY, v);
        else {
            AuctionHouse.logger.info("Unsupported persistent data key type!");
            return itemstack;
        }
        itemstack.setItemMeta(meta);
        return itemstack;
    }

    @Override
    public ItemStack deserialize(String item) {
        if (item == null) return null;
        try {
            // Detect and convert old SNBT compound format if needed
            String formatted = item;
            int braceIdx = item.indexOf('{');
            int bracketIdx = item.indexOf('[');
            if (braceIdx > 0 && (bracketIdx < 0 || bracketIdx > braceIdx)) {
                String typeKey = item.substring(0, braceIdx);
                String metaSnbt = item.substring(braceIdx);
                if (metaSnbt.equals("{}")) {
                    formatted = typeKey;
                } else {
                    formatted = convertSnbtToComponentFormat(typeKey, metaSnbt);
                }
            }
            return Bukkit.getItemFactory().createItemStack(formatted);
        } catch (Exception e1) {
            if (Config.debug) {
                AuctionHouse.logger.warning("Could not deserialize item: " + e1.getMessage());
            }
        }
        return null;
    }

    @Override
    public ItemStack updateItem(ItemStack item) {
        // In 1.21.4, items are automatically updated by the server
        return item;
    }

    @Override
    public String parseInternal(String internal) {
        // In 1.21.4, just return the string as-is since we don't use old NBT format
        return internal;
    }

    /**
     * Parse a namespaced key string like "namespace:key" into a NamespacedKey.
     */
    private NamespacedKey parseNamespacedKey(String key) {
        if (key == null || key.isEmpty()) return null;
        try {
            if (key.contains(":")) {
                String[] parts = key.split(":", 2);
                return new NamespacedKey(parts[0], parts[1]);
            }
            return NamespacedKey.minecraft(key);
        } catch (Exception e) {
            return null;
        }
    }
}

