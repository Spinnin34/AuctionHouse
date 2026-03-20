package com.spawnchunk.auctionhouse.util;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

public class ItemUtil {
    private static final HashMap<String, String> VARIANTS = new HashMap();

    public static String getTranslationKey(ItemStack item) {
        Material material = item.getType();
        Object key = material.isBlock() ? "block.minecraft." : "item.minecraft.";
        key = (String)key + material.getKey().getKey();
        return String.format("::{%s}::", key);
    }

    public static boolean hasCustomName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            return meta.hasDisplayName();
        }
        return false;
    }

    public static String getCustomName(ItemStack item) {
        ItemMeta meta;
        if (item.hasItemMeta() && item.getItemMeta() != null && (meta = item.getItemMeta()).hasDisplayName()) {
            return meta.getDisplayName();
        }
        return "";
    }

    public static boolean hasLocalizedName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            return meta.hasLocalizedName();
        }
        return false;
    }

    public static String getLocalizedName(ItemStack item) {
        ItemMeta meta;
        if (item.hasItemMeta() && item.getItemMeta() != null && (meta = item.getItemMeta()).hasDisplayName()) {
            return meta.getLocalizedName();
        }
        return "";
    }

    public static String getTranslatableName(ItemStack item) {
        if (ItemUtil.hasCustomName(item)) {
            String customName = ItemUtil.getCustomName(item);
            if (Config.debug) {
                AuctionHouse.logger.info(String.format("customName = %s", customName));
            }
            return ItemUtil.getCustomName(item);
        }
        return ItemUtil.getTranslationKey(item);
    }

    public static String getName(ItemStack item) {
        if (ItemUtil.hasCustomName(item)) {
            return ItemUtil.getCustomName(item);
        }
        if (ItemUtil.hasLocalizedName(item)) {
            return ItemUtil.getLocalizedName(item);
        }
        Material material = item.getType();
        String key = material.getKey().getKey();
        return (ItemUtil.hasEnchants(item) ? "&b" : "&f") + MessageUtil.readable(key);
    }

    public static String getItemName(ItemStack item) {
        Material material = item.getType();
        return material.getKey().getKey();
    }

    public static String getLoreString(ItemStack item) {
        List<String> lore;        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore() && (lore = meta.getLore()) != null) {
            boolean flag = true;
            StringBuilder sb = new StringBuilder("[");
            for (String line : lore) {
                if (flag) {
                    sb.append(String.format("%s", line));
                    flag = false;
                    continue;
                }
                sb.append(String.format(", %s", line));
            }
            sb.append("]");
            return sb.toString();
        }
        return "";
    }

    public static boolean hasEnchants(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            return meta.hasEnchants();
        }
        return false;
    }

    public static List<String> getEnchants(ItemStack item) {
        ItemMeta meta;
        ArrayList<String> enchants = new ArrayList<String>();
        if (item.hasItemMeta() && item.getItemMeta() != null && (meta = item.getItemMeta()).hasEnchants()) {
            Map<Enchantment, Integer> map = meta.getEnchants();
            Set<Enchantment> keys = map.keySet();
            for (Enchantment ench : keys) {
                int level = (Integer)map.get(ench);
                if (ench.equals(Enchantment.getByKey(NamespacedKey.minecraft("binding_curse"))) || ench.equals(Enchantment.getByKey(NamespacedKey.minecraft("vanishing_curse")))) {
                    enchants.add(String.format("&c%s", ItemUtil.prettyEnchant(ench, level)));
                    continue;
                }
                enchants.add(String.format("&7%s", ItemUtil.prettyEnchant(ench, level)));
            }
        }
        return enchants;
    }

    public static String getEnchantsString(ItemStack item) {
        List<String> enchants = ItemUtil.getEnchants(item);
        StringBuilder sb = new StringBuilder("[");
        boolean flag = true;
        for (String ench : enchants) {
            if (flag) {
                sb.append(String.format("%s", MessageUtil.sectionSymbol(ench)));
                flag = false;
                continue;
            }
            sb.append(String.format(", %s", MessageUtil.sectionSymbol(ench)));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String prettyEnchant(Enchantment enchantment, int level) {
        String enchantment_key;
        String sLevel = switch (level) {
            case 1 -> " I";
            case 2 -> " II";
            case 3 -> " III";
            case 4 -> " IV";
            case 5 -> " V";
            case 6 -> " VI";
            case 7 -> " VII";
            case 8 -> " VIII";
            case 9 -> " IX";
            case 10 -> " X";
            default -> String.format(" %s", level);
        };
        switch (enchantment_key = enchantment.getKey().getKey()) {
            case "protection": {
                return String.format("Protection%s", sLevel);
            }
            case "fire_protection": {
                return String.format("Fire Protection%s", sLevel);
            }
            case "feather_falling": {
                return String.format("Feather Falling%s", sLevel);
            }
            case "blast_protection": {
                return String.format("Blast Protection%s", sLevel);
            }
            case "projectile_protection": {
                return String.format("Projectile Protection%s", sLevel);
            }
            case "respiration": {
                return String.format("Respiration%s", sLevel);
            }
            case "aqua_affinity": {
                return String.format("Aqua Affinity%s", level > 1 ? sLevel : "");
            }
            case "thorns": {
                return String.format("Thorns%s", sLevel);
            }
            case "depth_strider": {
                return String.format("Depth Strider%s", sLevel);
            }
            case "frost_walker": {
                return String.format("Frost Walker%s", sLevel);
            }
            case "binding_curse": {
                return String.format("Curse of Binding%s", level > 1 ? sLevel : "");
            }
            case "sharpness": {
                return String.format("Sharpness%s", sLevel);
            }
            case "smite": {
                return String.format("Smite%s", sLevel);
            }
            case "bane_of_arthropods": {
                return String.format("Bane of Arthropods%s", sLevel);
            }
            case "knockback": {
                return String.format("Knockback%s", sLevel);
            }
            case "fire_aspect": {
                return String.format("Fire Aspect%s", sLevel);
            }
            case "looting": {
                return String.format("Looting%s", sLevel);
            }
            case "sweeping": {
                return String.format("Sweeping Edge%s", sLevel);
            }
            case "efficiency": {
                return String.format("Efficiency%s", sLevel);
            }
            case "silk_touch": {
                return String.format("Silk Touch%s", level > 1 ? sLevel : "");
            }
            case "unbreaking": {
                return String.format("Unbreaking%s", sLevel);
            }
            case "fortune": {
                return String.format("Fortune%s", sLevel);
            }
            case "power": {
                return String.format("Power%s", sLevel);
            }
            case "punch": {
                return String.format("Punch%s", sLevel);
            }
            case "flame": {
                return String.format("Flame%s", level > 1 ? sLevel : "");
            }
            case "infinity": {
                return String.format("Infinity%s", level > 1 ? sLevel : "");
            }
            case "luck": {
                return String.format("Luck of the Sea%s", sLevel);
            }
            case "lure": {
                return String.format("Lure%s", sLevel);
            }
            case "loyalty": {
                return String.format("Loyalty%s", sLevel);
            }
            case "impaling": {
                return String.format("Impaling%s", sLevel);
            }
            case "riptide": {
                return String.format("Riptide%s", sLevel);
            }
            case "channeling": {
                return String.format("Channeling%s", level > 1 ? sLevel : "");
            }
            case "multishot": {
                return String.format("Multishot%s", level > 1 ? sLevel : "");
            }
            case "quick_charge": {
                return String.format("Quick Charge%s", sLevel);
            }
            case "piercing": {
                return String.format("Piercing%s", sLevel);
            }
            case "mending": {
                return String.format("Mending%s", level > 1 ? sLevel : "");
            }
            case "vanishing_curse": {
                return String.format("Curse of Vanishing%s", level > 1 ? sLevel : "");
            }
            case "soul_speed": {
                return String.format("Soul Speed%s", sLevel);
            }
        }
        return "unknown";
    }

    public static Enchantment getEnchantmentById(short id) {
        switch (id) {
            case 0: {
                return Enchantment.getByKey(NamespacedKey.minecraft("protection"));
            }
            case 1: {
                return Enchantment.getByKey(NamespacedKey.minecraft("fire_protection"));
            }
            case 2: {
                return Enchantment.getByKey(NamespacedKey.minecraft("feather_falling"));
            }
            case 3: {
                return Enchantment.getByKey(NamespacedKey.minecraft("blast_protection"));
            }
            case 4: {
                return Enchantment.getByKey(NamespacedKey.minecraft("projectile_protection"));
            }
            case 5: {
                return Enchantment.getByKey(NamespacedKey.minecraft("respiration"));
            }
            case 6: {
                return Enchantment.getByKey(NamespacedKey.minecraft("aqua_affinity"));
            }
            case 7: {
                return Enchantment.getByKey(NamespacedKey.minecraft("thorns"));
            }
            case 8: {
                return Enchantment.getByKey(NamespacedKey.minecraft("depth_strider"));
            }
            case 9: {
                return Enchantment.getByKey(NamespacedKey.minecraft("frost_walker"));
            }
            case 10: {
                return Enchantment.getByKey(NamespacedKey.minecraft("binding_curse"));
            }
            case 16: {
                return Enchantment.getByKey(NamespacedKey.minecraft("sharpness"));
            }
            case 17: {
                return Enchantment.getByKey(NamespacedKey.minecraft("smite"));
            }
            case 18: {
                return Enchantment.getByKey(NamespacedKey.minecraft("bane_of_arthropods"));
            }
            case 19: {
                return Enchantment.getByKey(NamespacedKey.minecraft("knockback"));
            }
            case 20: {
                return Enchantment.getByKey(NamespacedKey.minecraft("fire_aspect"));
            }
            case 21: {
                return Enchantment.getByKey(NamespacedKey.minecraft("looting"));
            }
            case 22: {
                return Enchantment.getByKey(NamespacedKey.minecraft("sweeping_edge"));
            }
            case 32: {
                return Enchantment.getByKey(NamespacedKey.minecraft("efficiency"));
            }
            case 33: {
                return Enchantment.getByKey(NamespacedKey.minecraft("silk_touch"));
            }
            case 34: {
                return Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
            }
            case 35: {
                return Enchantment.getByKey(NamespacedKey.minecraft("fortune"));
            }
            case 48: {
                return Enchantment.getByKey(NamespacedKey.minecraft("power"));
            }
            case 49: {
                return Enchantment.getByKey(NamespacedKey.minecraft("punch"));
            }
            case 50: {
                return Enchantment.getByKey(NamespacedKey.minecraft("flame"));
            }
            case 51: {
                return Enchantment.getByKey(NamespacedKey.minecraft("infinity"));
            }
            case 61: {
                return Enchantment.getByKey(NamespacedKey.minecraft("luck_of_the_sea"));
            }
            case 62: {
                return Enchantment.getByKey(NamespacedKey.minecraft("lure"));
            }
            case 65: {
                return Enchantment.getByKey(NamespacedKey.minecraft("loyalty"));
            }
            case 66: {
                return Enchantment.getByKey(NamespacedKey.minecraft("impaling"));
            }
            case 67: {
                return Enchantment.getByKey(NamespacedKey.minecraft("riptide"));
            }
            case 68: {
                return Enchantment.getByKey(NamespacedKey.minecraft("channeling"));
            }
            case 70: {
                return Enchantment.getByKey(NamespacedKey.minecraft("mending"));
            }
            case 71: {
                return Enchantment.getByKey(NamespacedKey.minecraft("vanishing_curse"));
            }
        }
        return null;
    }

    public static boolean hasDamage(ItemStack item) {
        if (item.getType() == Material.PLAYER_HEAD) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable)meta;
            return damageable.hasDamage();
        }
        return false;
    }

    public static int getRepairCost(ItemStack itemstack) {
        ItemMeta meta;
        if (itemstack.hasItemMeta() && (meta = itemstack.getItemMeta()) instanceof Repairable) {
            return ((Repairable)meta).getRepairCost();
        }
        return -1;
    }

    public static boolean isFilledContainer(ItemStack itemstack) {
        if (AuctionHouse.nms.isContainer(itemstack)) {
            Map<Integer, ItemStack> containerItems = AuctionHouse.nms.getContainerItems(itemstack);
            return !containerItems.isEmpty();
        }
        return false;
    }

    public static ItemStack hidePotionEffects(ItemStack itemstack) {
        ItemMeta meta;
        ItemStack is = itemstack.clone();
        ItemMeta itemMeta = meta = is.hasItemMeta() ? is.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemstack.getType());
        if (meta == null) {
            return itemstack;
        }
        meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ADDITIONAL_TOOLTIP});
        is.setItemMeta(meta);
        return is;
    }

    public static String flatten(String id, short damage) {
        String key = id + "," + damage;
        if (VARIANTS.containsKey(key)) {
            return VARIANTS.get(key);
        }
        return id;
    }

    static {
        VARIANTS.put("minecraft:stone,1", "minecraft:granite");
        VARIANTS.put("minecraft:stone,2", "minecraft:polished_granite");
        VARIANTS.put("minecraft:stone,3", "minecraft:diorite");
        VARIANTS.put("minecraft:stone,4", "minecraft:polished_diorite");
        VARIANTS.put("minecraft:stone,5", "minecraft:andesite");
        VARIANTS.put("minecraft:stone,6", "minecraft:polished_andesite");
        VARIANTS.put("minecraft:grass,0", "minecraft:grass_block");
        VARIANTS.put("minecraft:dirt,1", "minecraft:coarse_dirt");
        VARIANTS.put("minecraft:dirt,2", "minecraft:podzol");
        VARIANTS.put("minecraft:planks,0", "minecraft:oak_planks");
        VARIANTS.put("minecraft:planks,1", "minecraft:spruce_planks");
        VARIANTS.put("minecraft:planks,2", "minecraft:birch_planks");
        VARIANTS.put("minecraft:planks,3", "minecraft:jungle_planks");
        VARIANTS.put("minecraft:planks,4", "minecraft:acacia_planks");
        VARIANTS.put("minecraft:planks,5", "minecraft:dark_oak_planks");
        VARIANTS.put("minecraft:sapling,0", "minecraft:oak_sapling");
        VARIANTS.put("minecraft:sapling,1", "minecraft:spruce_sapling");
        VARIANTS.put("minecraft:sapling,2", "minecraft:birch_sapling");
        VARIANTS.put("minecraft:sapling,3", "minecraft:jungle_sapling");
        VARIANTS.put("minecraft:sapling,4", "minecraft:acacia_sapling");
        VARIANTS.put("minecraft:sapling,5", "minecraft:dark_oak_sapling");
        VARIANTS.put("minecraft:sand,1", "minecraft:red_sand");
        VARIANTS.put("minecraft:log,0", "minecraft:oak_log");
        VARIANTS.put("minecraft:log,1", "minecraft:spruce_log");
        VARIANTS.put("minecraft:log,2", "minecraft:birch_log");
        VARIANTS.put("minecraft:log,3", "minecraft:jungle_log");
        VARIANTS.put("minecraft:log,4", "minecraft:oak_wood");
        VARIANTS.put("minecraft:log,5", "minecraft:spruce_wood");
        VARIANTS.put("minecraft:log,6", "minecraft:birch_wood");
        VARIANTS.put("minecraft:log,7", "minecraft:jungle_wood");
        VARIANTS.put("minecraft:log2,0", "minecraft:acacia_log");
        VARIANTS.put("minecraft:log2,1", "minecraft:dark_oak_log");
        VARIANTS.put("minecraft:log2,2", "minecraft:acacia_wood");
        VARIANTS.put("minecraft:log2,3", "minecraft:dark_oak_wood");
        VARIANTS.put("minecraft:leaves,0", "minecraft:oak_leaves");
        VARIANTS.put("minecraft:leaves,1", "minecraft:spruce_leaves");
        VARIANTS.put("minecraft:leaves,2", "minecraft:birch_leaves");
        VARIANTS.put("minecraft:leaves,3", "minecraft:jungle_leaves");
        VARIANTS.put("minecraft:leaves2,0", "minecraft:acacia_leaves");
        VARIANTS.put("minecraft:leaves2,1", "minecraft:dark_oak_leaves");
        VARIANTS.put("minecraft:sponge,1", "minecraft:wet_sponge");
        VARIANTS.put("minecraft:sandstone,1", "minecraft:chiseled_sandstone");
        VARIANTS.put("minecraft:sandstone,2", "minecraft:cut_sandstone");
        VARIANTS.put("minecraft:noteblock,0", "minecraft:note_block");
        VARIANTS.put("minecraft:golden_rail,0", "minecraft:powered_rail");
        VARIANTS.put("minecraft:web,0", "minecraft:cobweb");
        VARIANTS.put("minecraft:tallgrass,0", "minecraft:dead_bush");
        VARIANTS.put("minecraft:tallgrass,1", "minecraft:grass");
        VARIANTS.put("minecraft:tallgrass,2", "minecraft:fern");
        VARIANTS.put("minecraft:deadbush,0", "minecraft:dead_bush");
        VARIANTS.put("minecraft:piston_extension,0", "minecraft:moving_piston");
        VARIANTS.put("minecraft:wool,0", "white_wool");
        VARIANTS.put("minecraft:wool,1", "orange_wool");
        VARIANTS.put("minecraft:wool,2", "magenta_wool");
        VARIANTS.put("minecraft:wool,3", "light_blue_wool");
        VARIANTS.put("minecraft:wool,4", "yellow_wool");
        VARIANTS.put("minecraft:wool,5", "lime_wool");
        VARIANTS.put("minecraft:wool,6", "pink_wool");
        VARIANTS.put("minecraft:wool,7", "gray_wool");
        VARIANTS.put("minecraft:wool,8", "light_gray_wool");
        VARIANTS.put("minecraft:wool,9", "cyan_wool");
        VARIANTS.put("minecraft:wool,10", "purple_wool");
        VARIANTS.put("minecraft:wool,11", "blue_wool");
        VARIANTS.put("minecraft:wool,12", "brown_wool");
        VARIANTS.put("minecraft:wool,13", "green_wool");
        VARIANTS.put("minecraft:wool,14", "red_wool");
        VARIANTS.put("minecraft:wool,15", "black_wool");
        VARIANTS.put("minecraft:yellow_flower,0", "dandelion");
        VARIANTS.put("minecraft:red_flower,0", "poppy");
        VARIANTS.put("minecraft:red_flower,1", "blue_orchid");
        VARIANTS.put("minecraft:red_flower,2", "allium");
        VARIANTS.put("minecraft:red_flower,3", "azure_bluet");
        VARIANTS.put("minecraft:red_flower,4", "red_tulip");
        VARIANTS.put("minecraft:red_flower,5", "orange_tulip");
        VARIANTS.put("minecraft:red_flower,6", "white_tulip");
        VARIANTS.put("minecraft:red_flower,7", "pink_tulip");
        VARIANTS.put("minecraft:red_flower,8", "oxeye_daisy");
        VARIANTS.put("minecraft:double_wooden_slab,0", "oak_slab");
        VARIANTS.put("minecraft:double_wooden_slab,1", "spruce_slab");
        VARIANTS.put("minecraft:double_wooden_slab,2", "birch_slab");
        VARIANTS.put("minecraft:wooden_slab,0", "jungle_slab");
        VARIANTS.put("minecraft:wooden_slab,1", "acacia_slab");
        VARIANTS.put("minecraft:wooden_slab,2", "dark_oak_slab");
        VARIANTS.put("minecraft:double_stone_slab,0", "stone_slab");
        VARIANTS.put("minecraft:double_stone_slab,1", "sandstone_slab");
        VARIANTS.put("minecraft:double_stone_slab,2", "petrified_oak_slab");
        VARIANTS.put("minecraft:double_stone_slab,3", "cobblestone_slab");
        VARIANTS.put("minecraft:double_stone_slab,4", "brick_slab");
        VARIANTS.put("minecraft:stone_slab,0", "stone_brick_slab");
        VARIANTS.put("minecraft:stone_slab,1", "nether_brick_slab");
        VARIANTS.put("minecraft:stone_slab,2", "quartz_slab");
        VARIANTS.put("minecraft:stone_slab,3", "smooth_stone");
        VARIANTS.put("minecraft:stone_slab,4", "smooth_sandstone");
        VARIANTS.put("minecraft:stone_slab,5", "smooth_quartz");
        VARIANTS.put("minecraft:double_stone_slab2,0", "red_sandstone_slab");
        VARIANTS.put("minecraft:stone_slab2,0", "smooth_red_sandstone");
        VARIANTS.put("minecraft:purpur_slab,0", "purpur_slab");
        VARIANTS.put("minecraft:brick_block,0", "bricks");
        VARIANTS.put("minecraft:mob_spawner,0", "spawner");
        VARIANTS.put("minecraft:portal,0", "nether_portal");
        VARIANTS.put("minecraft:stone_stairs,0", "cobblestone_stairs");
        VARIANTS.put("minecraft:wooden_pressure_plate,0", "oak_pressure_plate");
        VARIANTS.put("minecraft:snow_layer,0", "snow");
        VARIANTS.put("minecraft:snow,0", "snow_block");
        VARIANTS.put("minecraft:fence,0", "oak_fence");
        VARIANTS.put("minecraft:pumpkin,0", "carved_pumpkin");
        VARIANTS.put("minecraft:lit_pumpkin,0", "jack_o_lantern");
        VARIANTS.put("minecraft:trapdoor,0", "oak_trapdoor");
        VARIANTS.put("minecraft:monster_egg,0", "infested_stone");
        VARIANTS.put("minecraft:monster_egg,1", "infested_cobblestone");
        VARIANTS.put("minecraft:monster_egg,2", "infested_stone_bricks");
        VARIANTS.put("minecraft:monster_egg,3", "infested_mossy_stone_bricks");
        VARIANTS.put("minecraft:monster_egg,4", "infested_cracked_stone_bricks");
        VARIANTS.put("minecraft:monster_egg,5", "infested_chiseled_stone_bricks");
        VARIANTS.put("minecraft:stonebrick,0", "stone_bricks");
        VARIANTS.put("minecraft:stonebrick,1", "mossy_stone_bricks");
        VARIANTS.put("minecraft:stonebrick,2", "cracked_stone_bricks");
        VARIANTS.put("minecraft:stonebrick,3", "chiseled_stone_bricks");
        VARIANTS.put("minecraft:melon_block,0", "melon");
        VARIANTS.put("minecraft:fence_gate,0", "oak_fence_gate");
        VARIANTS.put("minecraft:waterlily,0", "lily_pad");
        VARIANTS.put("minecraft:nether_brick,0", "nether_bricks");
        VARIANTS.put("minecraft:end_bricks,0", "end_stone_bricks");
        VARIANTS.put("minecraft:cobblestone_wall,0", "cobblestone_wall");
        VARIANTS.put("minecraft:cobblestone_wall,1", "mossy_cobblestone_wall");
        VARIANTS.put("minecraft:wooden_button,0", "oak_button");
        VARIANTS.put("minecraft:anvil,1", "chipped_anvil");
        VARIANTS.put("minecraft:anvil,2", "damaged_anvil");
        VARIANTS.put("minecraft:quartz_ore,0", "nether_quartz_ore");
        VARIANTS.put("minecraft:quartz_block,0", "quartz_block");
        VARIANTS.put("minecraft:quartz_block,1", "chiseled_quartz_block");
        VARIANTS.put("minecraft:quartz_block,2", "quartz_pillar");
        VARIANTS.put("minecraft:stained_hardened_clay,0", "white_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,1", "orange_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,2", "magenta_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,3", "light_blue_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,4", "yellow_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,5", "lime_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,6", "pink_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,7", "gray_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,8", "light_gray_terracotta");
        VARIANTS.put("minecraft:,stained_hardened_clay9", "cyan_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,10", "purple_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,11", "blue_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,12", "brown_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,13", "green_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,14", "red_terracotta");
        VARIANTS.put("minecraft:stained_hardened_clay,15", "black_terracotta");
        VARIANTS.put("minecraft:carpet,0", "white_carpet");
        VARIANTS.put("minecraft:carpet,1", "orange_carpet");
        VARIANTS.put("minecraft:carpet,2", "magenta_carpet");
        VARIANTS.put("minecraft:carpet,3", "light_blue_carpet");
        VARIANTS.put("minecraft:carpet,4", "yellow_carpet");
        VARIANTS.put("minecraft:carpet,5", "lime_carpet");
        VARIANTS.put("minecraft:carpet,6", "pink_carpet");
        VARIANTS.put("minecraft:carpet,7", "gray_carpet");
        VARIANTS.put("minecraft:carpet,8", "light_gray_carpet");
        VARIANTS.put("minecraft:carpet,9", "cyan_carpet");
        VARIANTS.put("minecraft:carpet,10", "purple_carpet");
        VARIANTS.put("minecraft:carpet,11", "blue_carpet");
        VARIANTS.put("minecraft:carpet,12", "brown_carpet");
        VARIANTS.put("minecraft:carpet,13", "green_carpet");
        VARIANTS.put("minecraft:carpet,14", "red_carpet");
        VARIANTS.put("minecraft:carpet,15", "black_carpet");
        VARIANTS.put("minecraft:hardened_clay,0", "terracotta");
        VARIANTS.put("minecraft:slime,0", "slime_block");
        VARIANTS.put("minecraft:double_plant,0", "sunflower");
        VARIANTS.put("minecraft:double_plant,1", "lilac");
        VARIANTS.put("minecraft:double_plant,2", "tall_grass");
        VARIANTS.put("minecraft:double_plant,3", "large_fern");
        VARIANTS.put("minecraft:double_plant,4", "rose_bush");
        VARIANTS.put("minecraft:double_plant,5", "peony");
        VARIANTS.put("minecraft:stained_glass,0", "white_stained_glass");
        VARIANTS.put("minecraft:stained_glass,1", "orange_stained_glass");
        VARIANTS.put("minecraft:stained_glass,2", "magenta_stained_glass");
        VARIANTS.put("minecraft:stained_glass,3", "light_blue_stained_glass");
        VARIANTS.put("minecraft:stained_glass,4", "yellow_stained_glass");
        VARIANTS.put("minecraft:stained_glass,5", "lime_stained_glass");
        VARIANTS.put("minecraft:stained_glass,6", "pink_stained_glass");
        VARIANTS.put("minecraft:stained_glass,7", "gray_stained_glass");
        VARIANTS.put("minecraft:stained_glass,8", "light_gray_stained_glass");
        VARIANTS.put("minecraft:stained_glass,9", "cyan_stained_glass");
        VARIANTS.put("minecraft:stained_glass,10", "purple_stained_glass");
        VARIANTS.put("minecraft:stained_glass,11", "blue_stained_glass");
        VARIANTS.put("minecraft:stained_glass,12", "brown_stained_glass");
        VARIANTS.put("minecraft:stained_glass,13", "green_stained_glass");
        VARIANTS.put("minecraft:stained_glass,14", "red_stained_glass");
        VARIANTS.put("minecraft:stained_glass,15", "black_stained_glass");
        VARIANTS.put("minecraft:stained_glass_pane,0", "white_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,1", "orange_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,2", "magenta_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,3", "light_blue_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,4", "yellow_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,5", "lime_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,6", "pink_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,7", "gray_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,8", "light_gray_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,9", "cyan_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,10", "purple_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,11", "blue_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,12", "brown_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,13", "green_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,14", "red_stained_glass_pane");
        VARIANTS.put("minecraft:stained_glass_pane,15", "black_stained_glass_pane");
        VARIANTS.put("minecraft:prismarine,1", "prismarine_bricks");
        VARIANTS.put("minecraft:prismarine,2", "dark_prismarine");
        VARIANTS.put("minecraft:red_sandstone,1", "chiseled_red_sandstone");
        VARIANTS.put("minecraft:red_sandstone,2", "cut_red_sandstone");
        VARIANTS.put("minecraft:magma,0", "magma_block");
        VARIANTS.put("minecraft:red_nether_brick,0", "red_nether_bricks");
        VARIANTS.put("minecraft:silver_shulker_box,0", "light_gray_shulker_box");
        VARIANTS.put("minecraft:silver_glazed_terracotta,0", "light_gray_glazed_terracotta");
        VARIANTS.put("minecraft:concrete,0", "white_concrete");
        VARIANTS.put("minecraft:concrete,1", "orange_concrete");
        VARIANTS.put("minecraft:concrete,2", "magenta_concrete");
        VARIANTS.put("minecraft:concrete,3", "light_blue_concrete");
        VARIANTS.put("minecraft:concrete,4", "yellow_concrete");
        VARIANTS.put("minecraft:concrete,5", "lime_concrete");
        VARIANTS.put("minecraft:concrete,6", "pink_concrete");
        VARIANTS.put("minecraft:concrete,7", "gray_concrete");
        VARIANTS.put("minecraft:concrete,8", "light_gray_concrete");
        VARIANTS.put("minecraft:concrete,9", "cyan_concrete");
        VARIANTS.put("minecraft:concrete,10", "purple_concrete");
        VARIANTS.put("minecraft:concrete,11", "blue_concrete");
        VARIANTS.put("minecraft:concrete,12", "brown_concrete");
        VARIANTS.put("minecraft:concrete,13", "green_concrete");
        VARIANTS.put("minecraft:concrete,14", "red_concrete");
        VARIANTS.put("minecraft:concrete,15", "black_concrete");
        VARIANTS.put("minecraft:concrete_powder,0", "white_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,1", "orange_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,2", "magenta_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,3", "light_blue_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,4", "yellow_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,5", "lime_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,6", "pink_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,7", "gray_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,8", "light_gray_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,9", "cyan_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,10", "purple_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,11", "blue_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,12", "brown_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,13", "green_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,14", "red_concrete_powder");
        VARIANTS.put("minecraft:concrete_powder,15", "black_concrete_powder");
        VARIANTS.put("minecraft:wooden_door,0", "oak_door");
        VARIANTS.put("minecraft:coal,1", "charcoal");
        VARIANTS.put("minecraft:golden_apple,1", "enchanted_golden_apple");
        VARIANTS.put("minecraft:boat,0", "oak_boat");
        VARIANTS.put("minecraft:reeds,0", "sugar_cane");
        VARIANTS.put("minecraft:fish,0", "cod");
        VARIANTS.put("minecraft:fish,1", "salmon");
        VARIANTS.put("minecraft:fish,2", "tropical_fish");
        VARIANTS.put("minecraft:fish,3", "pufferfish");
        VARIANTS.put("minecraft:cooked_fish,0", "cooked_cod");
        VARIANTS.put("minecraft:cooked_fish,1", "cooked_salmon");
        VARIANTS.put("minecraft:dye,0", "bone_meal");
        VARIANTS.put("minecraft:dye,1", "orange_dye");
        VARIANTS.put("minecraft:dye,2", "magenta_dye");
        VARIANTS.put("minecraft:dye,3", "light_blue_dye");
        VARIANTS.put("minecraft:dye,4", "dandelion_yellow");
        VARIANTS.put("minecraft:dye,5", "lime_dye");
        VARIANTS.put("minecraft:dye,6", "pink_dye");
        VARIANTS.put("minecraft:dye,7", "gray_dye");
        VARIANTS.put("minecraft:dye,8", "light_gray_dye");
        VARIANTS.put("minecraft:dye,9", "cyan_dye");
        VARIANTS.put("minecraft:dye,10", "purple_dye");
        VARIANTS.put("minecraft:dye,11", "lapis_lazuli");
        VARIANTS.put("minecraft:dye,12", "cocoa_beans");
        VARIANTS.put("minecraft:dye,13", "cactus_green");
        VARIANTS.put("minecraft:dye,14", "rose_red");
        VARIANTS.put("minecraft:dye,15", "ink_sac");
        VARIANTS.put("minecraft:bed,0", "white_bed");
        VARIANTS.put("minecraft:bed,1", "orange_bed");
        VARIANTS.put("minecraft:bed,2", "magenta_bed");
        VARIANTS.put("minecraft:bed,3", "light_blue_bed");
        VARIANTS.put("minecraft:bed,4", "yellow_bed");
        VARIANTS.put("minecraft:bed,5", "lime_bed");
        VARIANTS.put("minecraft:bed,6", "pink_bed");
        VARIANTS.put("minecraft:bed,7", "gray_bed");
        VARIANTS.put("minecraft:bed,8", "light_gray_bed");
        VARIANTS.put("minecraft:bed,9", "cyan_bed");
        VARIANTS.put("minecraft:bed,10", "purple_bed");
        VARIANTS.put("minecraft:bed,11", "blue_bed");
        VARIANTS.put("minecraft:bed,12", "brown_bed");
        VARIANTS.put("minecraft:bed,13", "green_bed");
        VARIANTS.put("minecraft:bed,14", "red_bed");
        VARIANTS.put("minecraft:bed,15", "black_bed");
        VARIANTS.put("minecraft:melon,0", "melon_slice");
        VARIANTS.put("minecraft:speckled_melon,0", "glistering_melon_slice");
        VARIANTS.put("minecraft:spawn_egg,65", "bat_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,61", "blaze_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,59", "cave_spider_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,93", "chicken_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,92", "cow_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,50", "creeper_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,31", "donkey_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,4", "elder_guardian_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,58", "enderman_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,67", "endermite_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,34", "evoker_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,56", "ghast_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,68", "guardian_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,100", "horse_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,23", "husk_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,103", "llama_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,62", "magma_cube_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,96", "mooshroom_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,32", "mule_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,98", "ocelot_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,90", "pig_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,102", "polar_bear_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,101", "rabbit_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,91", "sheep_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,69", "shulker_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,60", "silverfish_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,51", "skeleton_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,28", "skeleton_horse_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,55", "slime_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,52", "spider_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,94", "squid_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,6", "stray_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,35", "vex_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,120", "villager_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,36", "vindicator_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,66", "witch_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,5", "wither_skeleton_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,95", "wolf_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,54", "zombie_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,29", "zombie_horse_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,57", "zombie_pigman_spawn_egg");
        VARIANTS.put("minecraft:spawn_egg,27", "zombie_villager_spawn_egg");
        VARIANTS.put("minecraft:skull,0", "skeleton_skull");
        VARIANTS.put("minecraft:skull,1", "wither_skeleton_skull");
        VARIANTS.put("minecraft:skull,2", "zombie_head");
        VARIANTS.put("minecraft:skull,3", "player_head");
        VARIANTS.put("minecraft:skull,4", "creeper_head");
        VARIANTS.put("minecraft:skull,5", "dragon_head");
        VARIANTS.put("minecraft:fireworks,0", "firework_rocket");
        VARIANTS.put("minecraft:firework_charge,0", "firework_star");
        VARIANTS.put("minecraft:netherbrick,0", "nether_brick");
        VARIANTS.put("minecraft:banner,0", "white_banner");
        VARIANTS.put("minecraft:banner,1", "orange_banner");
        VARIANTS.put("minecraft:banner,2", "magenta_banner");
        VARIANTS.put("minecraft:banner,3", "light_blue_banner");
        VARIANTS.put("minecraft:banner,4", "yellow_banner");
        VARIANTS.put("minecraft:banner,5", "lime_banner");
        VARIANTS.put("minecraft:banner,6", "pink_banner");
        VARIANTS.put("minecraft:banner,7", "gray_banner");
        VARIANTS.put("minecraft:banner,8", "light_gray_banner");
        VARIANTS.put("minecraft:banner,9", "cyan_banner");
        VARIANTS.put("minecraft:banner,10", "purple_banner");
        VARIANTS.put("minecraft:banner,11", "blue_banner");
        VARIANTS.put("minecraft:banner,12", "brown_banner");
        VARIANTS.put("minecraft:,banner13", "green_banner");
        VARIANTS.put("minecraft:banner,14", "red_banner");
        VARIANTS.put("minecraft:banner,15", "black_banner");
        VARIANTS.put("minecraft:wall_banner,0", "white_wall_banner");
        VARIANTS.put("minecraft:wall_banner,1", "orange_wall_banner");
        VARIANTS.put("minecraft:wall_banner,2", "magenta_wall_banner");
        VARIANTS.put("minecraft:wall_banner,3", "light_blue_wall_banner");
        VARIANTS.put("minecraft:wall_banner,4", "yellow_wall_banner");
        VARIANTS.put("minecraft:wall_banner,5", "lime_wall_banner");
        VARIANTS.put("minecraft:wall_banner,6", "pink_wall_banner");
        VARIANTS.put("minecraft:wall_banner,7", "gray_wall_banner");
        VARIANTS.put("minecraft:wall_banner,8", "light_gray_wall_banner");
        VARIANTS.put("minecraft:wall_banner,9", "cyan_wall_banner");
        VARIANTS.put("minecraft:wall_banner,10", "purple_wall_banner");
        VARIANTS.put("minecraft:wall_banner,11", "blue_wall_banner");
        VARIANTS.put("minecraft:wall_banner,12", "brown_wall_banner");
        VARIANTS.put("minecraft:wall_banner,13", "green_wall_banner");
        VARIANTS.put("minecraft:wall_banner,14", "red_wall_banner");
        VARIANTS.put("minecraft:wall_banner,15", "black_wall_banner");
        VARIANTS.put("minecraft:chorus_fruit_popped,0", "popped_chorus_fruit");
        VARIANTS.put("minecraft:record_13,0", "music_disc_13");
        VARIANTS.put("minecraft:record_cat,0", "music_disc_cat");
        VARIANTS.put("minecraft:record_blocks,0", "music_disc_blocks");
        VARIANTS.put("minecraft:record_chirp,0", "music_disc_chirp");
        VARIANTS.put("minecraft:record_far,0", "music_disc_far");
        VARIANTS.put("minecraft:record_mall,0", "music_disc_mall");
        VARIANTS.put("minecraft:record_mellohi,0", "music_disc_mellohi");
        VARIANTS.put("minecraft:record_stal,0", "music_disc_stal");
        VARIANTS.put("minecraft:record_strad,0", "music_disc_strad");
        VARIANTS.put("minecraft:record_ward,0", "music_disc_ward");
        VARIANTS.put("minecraft:record_11,0", "music_disc_11");
        VARIANTS.put("minecraft:record_wait,0", "music_disc_wait");
    }
}

