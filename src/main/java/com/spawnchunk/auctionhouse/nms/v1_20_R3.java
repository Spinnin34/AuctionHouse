package com.spawnchunk.auctionhouse.nms;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.modules.PublicBukkitValues;
import com.spawnchunk.auctionhouse.nms.NMS;
import com.spawnchunk.auctionhouse.util.ItemUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class v1_20_R3
implements NMS {
    @Override
    public String getNBTString(ItemStack itemstack) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy((ItemStack)itemstack).v();
        return tag != null ? tag.toString() : null;
    }

    @Override
    public ItemStack setNBTString(ItemStack itemstack, String nbt) {
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)itemstack);
        try {
            NBTTagCompound tag = MojangsonParser.a((String)nbt);
            is.c(tag);
        }
        catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack)is);
    }

    @Override
    public ItemStack addNBTLocator(ItemStack itemstack) {
        net.minecraft.world.item.ItemStack is;
        Material material;
        if (itemstack != null && (material = itemstack.getType()) != Material.AIR && (is = CraftItemStack.asNMSCopy((ItemStack)itemstack)) != null) {
            NBTTagCompound tag = is.v();
            if (tag != null) {
                if (tag.b("AuctionHouse", 1)) {
                    return itemstack;
                }
                tag.a("AuctionHouse", (byte)1);
                is.c(tag);
            }
            return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack)is);
        }
        return itemstack;
    }

    @Override
    public boolean hasNBTLocator(ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        }
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)itemstack);
        if (is != null && is.u()) {
            NBTTagCompound tag = is.v();
            return tag != null && tag.b("AuctionHouse", 1);
        }
        return false;
    }

    private int[] uuid2IntArray(UUID uuid) {
        int[] i = new int[4];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        i[0] = (int)(msb >> 32);
        i[1] = (int)(msb & 0xFFFFFFL);
        i[2] = (int)(lsb >> 32);
        i[3] = (int)(lsb & 0xFFFFFFL);
        return i;
    }

    @Override
    public ItemStack getCustomSkull(String texture) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        UUID uuid = UUID.randomUUID();
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)item);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound skullOwner = new NBTTagCompound();
        skullOwner.a("Id", this.uuid2IntArray(uuid));
        NBTTagCompound properties = new NBTTagCompound();
        NBTTagList textures = new NBTTagList();
        NBTTagCompound entry = new NBTTagCompound();
        entry.a("Value", texture);
        textures.add((Object)entry);
        properties.a("textures", (NBTBase)textures);
        skullOwner.a("Properties", (NBTBase)properties);
        tag.a("SkullOwner", (NBTBase)skullOwner);
        is.c(tag);
        item = CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack)is);
        return item;
    }

    @Override
    public boolean isDye(ItemStack itemstack) {
        Material material = itemstack.getType();
        NamespacedKey namespacedKey = material.getKey();
        String key = namespacedKey.getKey();
        if (!key.isEmpty()) {
            return key.contains("dye");
        }
        return false;
    }

    @Override
    public boolean isContainer(ItemStack itemstack) {
        NBTTagCompound tag;
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)itemstack);
        if (is != null && (tag = is.v()) != null && tag.b("BlockEntityTag", 10)) {
            NBTTagCompound blockEntityTag = tag.p("BlockEntityTag");
            return blockEntityTag.b("Items", 9);
        }
        return false;
    }

    @Override
    public Map<Integer, ItemStack> getContainerItems(ItemStack itemstack) {
        NBTTagCompound blockEntityTag;
        NBTTagCompound isTag;
        HashMap<Integer, ItemStack> containerItems = new HashMap<Integer, ItemStack>();
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)itemstack);
        if (is != null && (isTag = is.v()) != null && isTag.b("BlockEntityTag", 10) && (blockEntityTag = isTag.p("BlockEntityTag")).b("Items", 9)) {
            NBTTagList items = blockEntityTag.c("Items", 10);
            int size = items.size();
            for (int i = 0; i < size; ++i) {
                Material material;
                byte count;
                String key;
                NBTTagCompound item = items.a(i);
                if (!item.b("Slot", 1)) continue;
                byte slot = item.f("Slot");
                if (!item.b("id", 8) || (key = item.l("id")) == null || !item.b("Count", 1) || (count = item.f("Count")) <= 0 || (material = Material.matchMaterial((String)key)) == null) continue;
                ItemStack itemStack = new ItemStack(material, (int)count);
                if (item.b("tag", 10)) {
                    NBTTagCompound tag = item.p("tag");
                    net.minecraft.world.item.ItemStack is2 = CraftItemStack.asNMSCopy((ItemStack)itemStack);
                    is2.c(tag);
                    itemStack = CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack)is2);
                }
                containerItems.put(Integer.valueOf(slot), itemStack);
            }
        }
        return containerItems;
    }

    @Override
    public List<String> getMobs(ItemStack itemstack) {
        ArrayList<String> mobs = new ArrayList<String>();
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)itemstack);
        if (is != null) {
            NBTTagCompound isTag = is.v();
            if (isTag != null) {
                NBTTagCompound silkSpawners;
                NBTTagCompound spawnData;
                String mob;
                if (isTag.b("BlockEntityTag", 10)) {
                    NBTTagCompound spawnData2;
                    NBTTagList spawnPotentials;
                    int size;
                    NBTTagCompound blockEntityTag = isTag.p("BlockEntityTag");
                    if (blockEntityTag.b("SpawnPotentials", 9) && (size = (spawnPotentials = blockEntityTag.c("SpawnPotentials", 10)).size()) > 0) {
                        int count = 0;
                        for (int i = 0; i < size; ++i) {
                            NBTTagCompound entity;
                            NBTTagCompound entry = spawnPotentials.a(i);
                            if (!entry.b("Entity", 10) || !(entity = entry.p("Entity")).b("id", 8)) continue;
                            String mob2 = entity.l("id");
                            mobs.add(mob2);
                            ++count;
                        }
                        if (count > 0) {
                            return mobs;
                        }
                    }
                    if (blockEntityTag.b("SpawnData", 10) && (spawnData2 = blockEntityTag.p("SpawnData")).b("id", 8)) {
                        String mob3 = spawnData2.l("id");
                        mobs.add(mob3);
                        return mobs;
                    }
                    if (blockEntityTag.b("id", 8)) {
                        mob = blockEntityTag.l("id");
                        mobs.add(mob);
                        return mobs;
                    }
                }
                if (isTag.b("SpawnData", 10) && (spawnData = isTag.p("SpawnData")).b("id", 8)) {
                    mob = spawnData.l("id");
                    mobs.add(mob);
                    return mobs;
                }
                if (isTag.b("SilkSpawners", 10) && (silkSpawners = isTag.p("SilkSpawners")).b("EntityID", 2)) {
                    short entityID = silkSpawners.g("EntityID");
                    EntityType entity = EntityType.fromId((int)entityID);
                    if (entity != null) {
                        mobs.add(entity.name());
                    }
                    return mobs;
                }
                if (isTag.b("ms_mob", 8)) {
                    String mob4 = isTag.l("ms_mob");
                    EntityType entity = EntityType.fromName((String)mob4);
                    if (entity != null) {
                        mobs.add(entity.name());
                    }
                    return mobs;
                }
                if (isTag.b("PublicBukkitValues", 10)) {
                    NBTTagCompound publicBukkitValues = isTag.p("PublicBukkitValues");
                    Set<String> keys = (Set<String>)(Set<?>)publicBukkitValues.e();
                    for (String key : keys) {
                        if (!PublicBukkitValues.customKeys.contains(key) || !publicBukkitValues.b(key, 8)) continue;
                        String mob5 = publicBukkitValues.l(key);
                        mobs.add(mob5);
                        return mobs;
                    }
                }
                if (this.isValidNBT(isTag)) {
                    mobs.add("minecraft:pig");
                }
            } else {
                mobs.add("minecraft:pig");
            }
        }
        return mobs;
    }

    private boolean isValidNBT(NBTTagCompound isTag) {
        Set<String> keys = (Set<String>)(Set<?>)isTag.e();
        Iterator iterator = keys.iterator();
        block26: while (iterator.hasNext()) {
            String key;
            switch (key = (String)iterator.next()) {
                case "BlockEntityTag": {
                    if (!isTag.b("BlockEntityTag", 10)) continue block26;
                    NBTTagCompound blockEntityTag = isTag.p("BlockEntityTag");
                    Set<String> bkeys = (Set<String>)(Set<?>)blockEntityTag.e();
                    Iterator iterator2 = bkeys.iterator();
                    block27: while (iterator2.hasNext()) {
                        String bkey;
                        switch (bkey = (String)iterator2.next()) {
                            case "id": 
                            case "SpawnData": 
                            case "SpawnPotentials": 
                            case "SpawnCount": 
                            case "SpawnRange": 
                            case "Delay": 
                            case "MinSpawnDelay": 
                            case "MaxSpawnDelay": 
                            case "MaxNearbyEntities": 
                            case "RequiredPlayerRange": {
                                continue block27;
                            }
                        }
                        return false;
                    }
                    continue block26;
                }
                case "display": 
                case "SpawnData": 
                case "SilkSpawners": 
                case "ms_mob": {
                    continue block26;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public int getCustomModelData(ItemStack itemstack) {
        NBTTagCompound tag;
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)itemstack);
        if (is != null && (tag = is.v()) != null && tag.b("CustomModelData", 3)) {
            return tag.h("CustomModelData");
        }
        return 0;
    }

    @Override
    public ItemStack setCustomModelData(ItemStack itemstack, int customModelData) {
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)itemstack);
        if (is != null) {
            NBTTagCompound tag = is.v();
            if (tag != null) {
                tag.a("CustomModelData", customModelData);
            }
            is.c(tag);
            return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack)is);
        }
        return itemstack;
    }

    @Override
    public ItemStack setLore(ItemStack itemstack, List<String> lore) {
        NBTTagCompound tag;
        net.minecraft.world.item.ItemStack is;
        ItemMeta meta;
        if (itemstack.getType() != Material.PLAYER_HEAD) {
            ItemMeta meta2;
            ItemMeta itemMeta = meta2 = itemstack.hasItemMeta() ? itemstack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemstack.getType());
            if (meta2 == null) {
                return itemstack;
            }
            meta2.setLore(lore);
            itemstack.setItemMeta(meta2);
            return itemstack;
        }
        ItemStack clone = new ItemStack(Material.STONE);
        ItemMeta itemMeta = meta = clone.hasItemMeta() ? clone.getItemMeta() : Bukkit.getItemFactory().getItemMeta(Material.STONE);
        if (meta == null) {
            return itemstack;
        }
        meta.setLore(lore);
        clone.setItemMeta(meta);
        ArrayList<NBTTagString> temp_lore = new ArrayList<NBTTagString>();
        net.minecraft.world.item.ItemStack isc = CraftItemStack.asNMSCopy((ItemStack)clone);
        if (isc != null) {
            NBTTagCompound tag2;
            NBTTagCompound nBTTagCompound = tag2 = isc.u() ? isc.v() : new NBTTagCompound();
            if (tag2 != null) {
                NBTTagCompound display;
                NBTTagCompound nBTTagCompound2 = display = tag2.b("display", 10) ? tag2.p("display") : new NBTTagCompound();
                if (display != null && display.b("Lore", 9)) {
                    NBTTagList list = display.c("Lore", 8);
                    for (Object o : list) {
                        if (!(o instanceof NBTTagString)) continue;
                        temp_lore.add((NBTTagString)o);
                    }
                }
            }
        }
        if ((is = CraftItemStack.asNMSCopy((ItemStack)itemstack)) != null && (tag = is.w()) != null) {
            NBTTagCompound display;
            if (!tag.b("display", 10)) {
                tag.a("display", (NBTBase)new NBTTagCompound());
            }
            if ((display = tag.p("display")) == null) {
                display = new NBTTagCompound();
            }
            NBTTagList list = new NBTTagList();
            list.addAll(temp_lore);
            display.a("Lore", (NBTBase)list);
            tag.a("display", (NBTBase)display);
            is.c(tag);
            return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack)is);
        }
        return itemstack;
    }

    @Override
    public ItemStack setDisplayName(ItemStack itemstack, String name) {
        net.minecraft.world.item.ItemStack is;
        ItemMeta meta;
        if (itemstack.getType() != Material.PLAYER_HEAD) {
            ItemMeta meta2;
            ItemMeta itemMeta = meta2 = itemstack.hasItemMeta() ? itemstack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemstack.getType());
            if (meta2 == null) {
                return itemstack;
            }
            meta2.setDisplayName(name);
            itemstack.setItemMeta(meta2);
            return itemstack;
        }
        ItemStack clone = new ItemStack(Material.STONE);
        ItemMeta itemMeta = meta = clone.hasItemMeta() ? clone.getItemMeta() : Bukkit.getItemFactory().getItemMeta(Material.STONE);
        if (meta == null) {
            return itemstack;
        }
        meta.setDisplayName(name);
        clone.setItemMeta(meta);
        net.minecraft.world.item.ItemStack isc = CraftItemStack.asNMSCopy((ItemStack)clone);
        String temp_name = "";
        if (isc != null) {
            NBTTagCompound tag;
            NBTTagCompound nBTTagCompound = tag = isc.u() ? isc.v() : new NBTTagCompound();
            if (tag != null) {
                NBTTagCompound display;
                NBTTagCompound nBTTagCompound2 = display = tag.b("display", 10) ? tag.p("display") : new NBTTagCompound();
                if (display != null && display.b("Name", 8)) {
                    temp_name = display.l("Name");
                }
            }
        }
        if ((is = CraftItemStack.asNMSCopy((ItemStack)itemstack)) != null) {
            NBTTagCompound display;
            NBTTagCompound tag;
            NBTTagCompound nBTTagCompound = tag = is.u() && is.v() != null ? is.v() : new NBTTagCompound();
            if (!tag.b("display", 10)) {
                tag.a("display", (NBTBase)new NBTTagCompound());
            }
            if ((display = tag.p("display")) == null) {
                display = new NBTTagCompound();
            }
            display.a("Name", temp_name);
            tag.a("display", (NBTBase)display);
            is.c(tag);
            return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack)is);
        }
        return itemstack;
    }

    @Override
    public boolean hasPersistentDataKey(ItemStack itemstack, String key) {
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)itemstack);
        if (is != null) {
            NBTTagCompound pbv;
            NBTTagCompound tag;
            NBTTagCompound nBTTagCompound = tag = is.u() && is.v() != null ? is.v() : new NBTTagCompound();
            if (tag.b("PublicBukkitValues", 10) && (pbv = tag.p("PublicBukkitValues")) != null) {
                return pbv.e(key);
            }
        }
        return false;
    }

    @Override
    public Object getPersistentDataKey(ItemStack itemstack, String key) {
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)itemstack);
        if (is != null) {
            NBTTagCompound pbv;
            NBTTagCompound tag;
            NBTTagCompound nBTTagCompound = tag = is.u() && is.v() != null ? is.v() : new NBTTagCompound();
            if (tag.b("PublicBukkitValues", 10) && (pbv = tag.p("PublicBukkitValues")) != null && pbv.e(key)) {
                NBTBase value = pbv.c(key);
                if (value instanceof NBTTagString) {
                    return ((NBTTagString)value).t_();
                }
                if (value instanceof NBTTagByte) {
                    return ((NBTTagByte)value).i();
                }
                if (value instanceof NBTTagShort) {
                    return ((NBTTagShort)value).h();
                }
                if (value instanceof NBTTagInt) {
                    return ((NBTTagInt)value).g();
                }
                if (value instanceof NBTTagLong) {
                    return ((NBTTagLong)value).f();
                }
                if (value instanceof NBTTagFloat) {
                    return Float.valueOf(((NBTTagFloat)value).k());
                }
                if (value instanceof NBTTagDouble) {
                    return ((NBTTagDouble)value).j();
                }
                if (value instanceof NBTTagByteArray) {
                    return ((NBTTagByteArray)value).e();
                }
                if (value instanceof NBTTagIntArray) {
                    return ((NBTTagIntArray)value).g();
                }
                if (value instanceof NBTTagLongArray) {
                    return ((NBTTagLongArray)value).g();
                }
                AuctionHouse.logger.info("Unsupported persistent data key type!");
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> getPersistentData(ItemStack itemstack) {
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)itemstack);
        HashMap<String, Object> persistentDataKeys = new HashMap<String, Object>();
        if (is != null) {
            NBTTagCompound pbv;
            NBTTagCompound tag;
            NBTTagCompound nBTTagCompound = tag = is.u() && is.v() != null ? is.v() : new NBTTagCompound();
            if (tag.b("PublicBukkitValues", 10) && (pbv = tag.p("PublicBukkitValues")) != null) {
                Set<String> keys = (Set<String>)(Set<?>)pbv.e();
                for (String key : keys) {
                    Object v;
                    NBTBase value = pbv.c(key);
                    if (value instanceof NBTTagString) {
                        v = ((NBTTagString)value).t_();
                        persistentDataKeys.put(key, v);
                        continue;
                    }
                    if (value instanceof NBTTagByte) {
                        v = ((NBTTagByte)value).i();
                        persistentDataKeys.put(key, v);
                        continue;
                    }
                    if (value instanceof NBTTagShort) {
                        v = ((NBTTagShort)value).h();
                        persistentDataKeys.put(key, v);
                        continue;
                    }
                    if (value instanceof NBTTagInt) {
                        v = ((NBTTagInt)value).g();
                        persistentDataKeys.put(key, v);
                        continue;
                    }
                    if (value instanceof NBTTagLong) {
                        v = ((NBTTagLong)value).f();
                        persistentDataKeys.put(key, v);
                        continue;
                    }
                    if (value instanceof NBTTagFloat) {
                        v = Float.valueOf(((NBTTagFloat)value).k());
                        persistentDataKeys.put(key, v);
                        continue;
                    }
                    if (value instanceof NBTTagDouble) {
                        v = ((NBTTagDouble)value).j();
                        persistentDataKeys.put(key, v);
                        continue;
                    }
                    if (value instanceof NBTTagByteArray) {
                        v = ((NBTTagByteArray)value).e();
                        persistentDataKeys.put(key, v);
                        continue;
                    }
                    if (value instanceof NBTTagIntArray) {
                        v = ((NBTTagIntArray)value).g();
                        persistentDataKeys.put(key, v);
                        continue;
                    }
                    if (value instanceof NBTTagLongArray) {
                        v = ((NBTTagLongArray)value).g();
                        persistentDataKeys.put(key, v);
                        continue;
                    }
                    AuctionHouse.logger.info("Unsupported persistent data key type!");
                }
            }
        }
        return persistentDataKeys;
    }

    @Override
    public ItemStack setPersistentDataKey(ItemStack itemstack, String key, Object value) {
        NBTTagCompound pbv;
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)itemstack);
        if (is == null) {
            return itemstack;
        }
        NBTTagCompound tag = is.u() && is.v() != null ? is.v() : new NBTTagCompound();
        NBTTagCompound nBTTagCompound = pbv = tag.b("PublicBukkitValues", 10) ? tag.p("PublicBukkitValues") : new NBTTagCompound();
        if (value instanceof String) {
            String v = (String)value;
            pbv.a(key, v);
        } else if (value instanceof Boolean || value instanceof Byte) {
            byte v = (Byte)value;
            pbv.a(key, v);
        } else if (value instanceof Short) {
            short v = (Short)value;
            pbv.a(key, v);
        } else if (value instanceof Integer) {
            int v = (Integer)value;
            pbv.a(key, v);
        } else if (value instanceof Long) {
            long v = (Long)value;
            pbv.a(key, v);
        } else if (value instanceof Float) {
            float v = ((Float)value).floatValue();
            pbv.a(key, v);
        } else if (value instanceof Double) {
            double v = (Double)value;
            pbv.a(key, v);
        } else if (value instanceof byte[]) {
            byte[] v = (byte[])value;
            pbv.a(key, v);
        } else if (value instanceof int[]) {
            int[] v = (int[])value;
            pbv.a(key, v);
        } else if (value instanceof long[]) {
            long[] v = (long[])value;
            pbv.a(key, v);
        } else {
            AuctionHouse.logger.info("Unsupported persistent data key type!");
            return itemstack;
        }
        tag.a("PublicBukkitValues", (NBTBase)pbv);
        is.c(tag);
        return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack)is);
    }

    @Override
    public ItemStack deserialize(String item) {
        if (item != null) {
            byte[] bytes = Base64.getDecoder().decode(item);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            try {
                NBTTagCompound nbt = NBTCompressedStreamTools.a((InputStream)inputStream, null);
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("nbt = %s", nbt));
                }
                NBTTagCompound tag = this.updateNBT(nbt);
                net.minecraft.world.item.ItemStack is = net.minecraft.world.item.ItemStack.a((NBTTagCompound)tag);
                return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack)is);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public ItemStack updateItem(ItemStack item) {
        if (item != null) {
            net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy((ItemStack)item);
            if (is != null) {
                NBTTagCompound nbt_old = is.b(new NBTTagCompound());
                NBTTagCompound nbt = this.updateNBT(nbt_old);
                is = net.minecraft.world.item.ItemStack.a((NBTTagCompound)nbt);
                return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack)is);
            }
            return item;
        }
        return null;
    }

    private NBTTagCompound updateNBT(NBTTagCompound nbt) {
        short damage;
        String id;
        String result;
        if (nbt.b("id", 8) && nbt.b("Damage", 2) && !(result = ItemUtil.flatten(id = nbt.l("id"), damage = nbt.g("Damage"))).equals(id)) {
            nbt.a("id", result);
            nbt.a("Damage", (short)0);
        }
        if (nbt.b("tag", 10)) {
            NBTTagCompound tag = nbt.p("tag");
            if (tag.b("display", 10)) {
                NBTTagCompound display = tag.p("display");
                if (display.b("Name", 8)) {
                    String name = display.l("Name");
                    String json = String.format("{\"text\":\"%s\"}", name);
                    display.a("Name", json);
                }
                if (display.b("Lore", 9)) {
                    NBTTagList lore = display.c("Lore", 8);
                    for (Object base : lore) {
                        if (!(base instanceof NBTTagString)) continue;
                        NBTTagString line = (NBTTagString)base;
                        String json = String.format("{\"text\":\"%s\"}", line.t_());
                        lore.d(lore.indexOf(base), (NBTBase)NBTTagString.a((String)json));
                    }
                }
                tag.a("display", (NBTBase)display);
            }
            if (tag.b("ench", 9)) {
                NBTTagList enchantments;
                NBTTagList ench = tag.c("ench", 10);
                HashMap<String, Short> listEnchants = new HashMap<String, Short>();
                for (Object base : ench) {
                    NBTTagCompound enchantment;
                    if (!(base instanceof NBTTagCompound) || !(enchantment = (NBTTagCompound)base).b("id", 2) || !enchantment.b("lvl", 2)) continue;
                    short id2 = enchantment.g("id");
                    short lvl = enchantment.g("lvl");
                    Enchantment en = ItemUtil.getEnchantmentById(id2);
                    if (en == null) continue;
                    listEnchants.put(en.getKey().toString(), lvl);
                }
                tag.r("ench");
                if (tag.b("Enchantments", 9)) {
                    enchantments = tag.c("Enchantments", 10);
                    for (Object base : enchantments) {
                        NBTTagCompound enchantment;
                        if (!(base instanceof NBTTagCompound) || !(enchantment = (NBTTagCompound)base).b("id", 8) || !enchantment.b("lvl", 2)) continue;
                        String id3 = enchantment.l("id");
                        short lvl = enchantment.g("lvl");
                        if (listEnchants.containsKey(id3)) continue;
                        listEnchants.put(id3, lvl);
                    }
                }
                tag.r("Enchantments");
                enchantments = new NBTTagList();
                for (String key : listEnchants.keySet()) {
                    short lvl = (Short)listEnchants.get(key);
                    NBTTagCompound entry = new NBTTagCompound();
                    entry.a("id", key);
                    entry.a("lvl", lvl);
                    enchantments.add((Object)entry);
                }
                tag.a("Enchantments", (NBTBase)enchantments);
            }
            nbt.a("tag", (NBTBase)tag);
        }
        return nbt;
    }

    @Override
    public String parseInternal(String internal) {
        if (internal != null) {
            ByteArrayInputStream buf = new ByteArrayInputStream(Base64.getDecoder().decode(internal));
            try {
                NBTTagCompound internalTag = NBTCompressedStreamTools.a((InputStream)buf, null);
                return internalTag.toString();
            }
            catch (IOException ex) {
                AuctionHouse.logger.log(Level.SEVERE, null, ex);
            }
        }
        return internal;
    }
}

