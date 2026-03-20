package com.spawnchunk.auctionhouse.modules;

import com.google.gson.stream.MalformedJsonException;
import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.modules.ListingType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Listing {
    private String world;
    private String seller_uuid;
    private String buyer_uuid;
    private String bidder_uuid;
    private float price;
    private float reserve;
    private float bid;
    private ListingType type = ListingType.PLAYER_LISTING;
    private ItemStack item;

    public Listing(String world, @NotNull String seller_uuid, @Nullable String buyer_uuid, @Nullable String bidder_uuid, float price, float reserve, float bid, @NotNull ListingType type, ItemStack item) {
        this.world = world;
        this.seller_uuid = seller_uuid;
        this.buyer_uuid = buyer_uuid != null ? buyer_uuid : "";
        this.bidder_uuid = bidder_uuid != null ? bidder_uuid : "";
        this.price = price;
        this.reserve = reserve;
        this.bid = 0.0f;
        this.type = type;
        this.item = item;
    }

    public Listing(String json) {
        String item = this.parseItem(json);
        String id = this.parseId(item);
        int count = this.parseCount(item);
        String nbt = this.parseNBTTag(item);
        Material material = Material.matchMaterial((String)id);
        if (material != null) {
            String world = this.parseWorld(json);
            String seller_uuid = this.parseSeller(json);
            String buyer_uuid = this.parseBuyer(json);
            String bidder_uuid = this.parseBidder(json);
            float price = this.parsePrice(json);
            float reserve = this.parseReserve(json);
            float bid = this.parseBid(json);
            ListingType type = this.parseType(json);
            ItemStack itemStack = new ItemStack(material, count);
            itemStack = AuctionHouse.nms.setNBTString(itemStack, nbt);
            if (seller_uuid != null) {
                this.world = world;
                this.seller_uuid = seller_uuid;
                this.buyer_uuid = buyer_uuid != null ? buyer_uuid : "";
                this.bidder_uuid = bidder_uuid != null ? bidder_uuid : "";
                this.price = price;
                this.reserve = reserve;
                this.bid = bid;
                this.type = type;
                this.item = itemStack;
            }
        }
    }

    private String getName(UUID uuid) {
        return AuctionHouse.plugin.getServer().getOfflinePlayer(uuid).getName();
    }

    public String getWorld() {
        return this.world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String getSeller_UUID() {
        return this.seller_uuid;
    }

    public void setSeller_UUID(String seller_uuid) {
        this.seller_uuid = seller_uuid != null ? seller_uuid : "";
    }

    public String getBuyer_UUID() {
        return this.buyer_uuid != null ? this.buyer_uuid : "";
    }

    public void setBuyer_UUID(String buyer_uuid) {
        this.buyer_uuid = buyer_uuid != null ? buyer_uuid : "";
    }

    public String getBidder_UUID() {
        return this.bidder_uuid != null ? this.bidder_uuid : "";
    }

    public void setBidder_UUID(String bidder_uuid) {
        this.bidder_uuid = bidder_uuid != null ? bidder_uuid : "";
    }

    public OfflinePlayer getSeller() {
        try {
            UUID uuid = !this.seller_uuid.isEmpty() ? UUID.fromString(this.seller_uuid) : null;
            return uuid != null ? Bukkit.getOfflinePlayer((UUID)uuid) : null;
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    public String getSellerName() {
        String uuid_string = this.seller_uuid;
        if (uuid_string.isEmpty()) {
            return "";
        }
        UUID uuid = null;
        try {
            uuid = UUID.fromString(uuid_string);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        if (uuid == null) {
            return "";
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer((UUID)uuid);
        if (op.hasPlayedBefore()) {
            return op.getName();
        }
        return uuid_string;
    }

    public void setSeller(OfflinePlayer seller) {
        this.seller_uuid = seller.getUniqueId().toString();
    }

    public OfflinePlayer getBuyer() {
        UUID uuid = !this.buyer_uuid.isEmpty() ? UUID.fromString(this.buyer_uuid) : null;
        return uuid != null ? Bukkit.getOfflinePlayer((UUID)uuid) : null;
    }

    public String getBuyerName() {
        String uuid_string = this.buyer_uuid;
        if (uuid_string.isEmpty()) {
            return "";
        }
        UUID uuid = null;
        try {
            uuid = UUID.fromString(uuid_string);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        if (uuid == null) {
            return "";
        }
        OfflinePlayer op = Bukkit.getOfflinePlayer((UUID)uuid);
        if (op.hasPlayedBefore()) {
            return op.getName();
        }
        return uuid_string;
    }

    public void setBuyer(OfflinePlayer buyer) {
        this.buyer_uuid = buyer.getUniqueId().toString();
    }

    public OfflinePlayer getBidder() {
        UUID uuid = !this.bidder_uuid.isEmpty() ? UUID.fromString(this.bidder_uuid) : null;
        return uuid != null ? Bukkit.getOfflinePlayer((UUID)uuid) : null;
    }

    public void setBidder(OfflinePlayer bidder) {
        this.bidder_uuid = bidder.getUniqueId().toString();
    }

    public float getPrice() {
        return this.price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getReserve() {
        return this.reserve;
    }

    public void setReserve(float reserve) {
        this.reserve = reserve;
    }

    public float getBid() {
        return this.bid;
    }

    public void setBid(float bid) {
        this.bid = bid;
    }

    @NotNull
    public ListingType getType() {
        return this.type;
    }

    public void setType(ListingType type) {
        this.type = type;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void setItem(ItemStack itemStack) {
        this.item = itemStack;
    }

    public String toString() {
        ItemStack item = this.item;
        if (item == null) {
            return "";
        }
        Material material = item.getType();
        String id = material.getKey().toString();
        int count = item.getAmount();
        String world = this.world == null ? "" : this.world;
        String seller_uuid = this.seller_uuid != null ? this.seller_uuid : "";
        String buyer_uuid = this.buyer_uuid != null ? this.buyer_uuid : "";
        String bidder_uuid = this.bidder_uuid != null ? this.bidder_uuid : "";
        float price = this.price;
        float reserve = this.reserve;
        float bid = this.bid;
        ListingType type = this.type;
        String nbt = AuctionHouse.nms.getNBTString(this.item);
        if (nbt == null) {
            nbt = "{}";
        }
        return String.format("{World:\"%s\",Seller:\"%s\",Buyer:\"%s\",Bidder:\"%s\",Price:%.2f,Reserve:%.2f,Bid:%.2f,Type:%s,Item:{id:\"%s\",Count:%d,nbt:%s}}", world, seller_uuid, buyer_uuid, bidder_uuid, Float.valueOf(price), Float.valueOf(reserve), Float.valueOf(bid), type.name(), id, count, nbt);
    }

    private String parseNBT(String json) throws MalformedJsonException {
        String s = json;
        if (s.startsWith("{")) {
            s = this.unbracket(s);
        }
        List<String> entries = this.splitEscaped(s);
        for (String entry : entries) {
            if (!entry.startsWith("nbt:")) continue;
            s = entry.replace("nbt:", "");
            return s;
        }
        return "{}";
    }

    private String parseString(String json, String key) throws MalformedJsonException {
        String s = json;
        if (s.startsWith("{")) {
            s = this.unbracket(s);
        }
        List<String> entries = this.splitEscaped(s);
        for (String entry : entries) {
            if (!entry.startsWith(key + ":")) continue;
            s = entry.replace(key + ":", "");
            if (s.startsWith("\"")) {
                s = this.unquote(s);
            }
            return s;
        }
        return "";
    }

    private boolean parseBoolean(String json, String key) throws MalformedJsonException {
        boolean b = false;
        String s = json;
        if (s.startsWith("{")) {
            s = this.unbracket(s);
        }
        List<String> entries = this.splitEscaped(s);
        for (String entry : entries) {
            if (!entry.startsWith(key + ":")) continue;
            s = entry.replace(key + ":", "");
            b = Boolean.parseBoolean(s);
            break;
        }
        return b;
    }

    private int parseInteger(String json, String key) throws MalformedJsonException {
        int n = 0;
        String s = json;
        if (s.startsWith("{")) {
            s = this.unbracket(s);
        }
        List<String> entries = this.splitEscaped(s);
        for (String entry : entries) {
            if (!entry.startsWith(key + ":")) continue;
            s = entry.replace(key + ":", "");
            n = Integer.parseInt(s);
            break;
        }
        return n;
    }

    private float parseFloat(String json, String key) throws MalformedJsonException {
        float f = 0.0f;
        String s = json;
        if (s.startsWith("{")) {
            s = this.unbracket(s);
        }
        List<String> entries = this.splitEscaped(s);
        for (String entry : entries) {
            if (!entry.startsWith(key + ":")) continue;
            s = entry.replace(key + ":", "");
            f = Float.parseFloat(s);
            break;
        }
        return f;
    }

    private String parseItem(String json) {
        String item = null;
        try {
            item = this.parseString(json, "Item");
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing Item - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        return item;
    }

    private String parseId(String json) {
        String id = null;
        try {
            id = this.parseString(json, "id");
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing Id - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        return id;
    }

    private int parseCount(String json) {
        int count = 0;
        try {
            count = this.parseInteger(json, "Count");
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing Count - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        return count;
    }

    private String parseWorld(String json) {
        String world = null;
        try {
            world = this.parseString(json, "World");
            if (world.isEmpty()) {
                world = ((World)Bukkit.getServer().getWorlds().get(0)).getName();
            }
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing World - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        return world;
    }

    private String parseSeller(String json) {
        String seller = null;
        try {
            seller = this.parseString(json, "Seller");
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing Seller - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        return seller;
    }

    private String parseBuyer(String json) {
        String buyer = null;
        try {
            buyer = this.parseString(json, "Buyer");
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing Buyer - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        return buyer;
    }

    private String parseBidder(String json) {
        String bidder = null;
        try {
            bidder = this.parseString(json, "Bidder");
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing Bidder - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        return bidder;
    }

    private float parsePrice(String json) {
        float price = 0.0f;
        try {
            price = this.parseFloat(json, "Price");
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing Price - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        return price;
    }

    private float parseReserve(String json) {
        float reserve = 0.0f;
        try {
            reserve = this.parseFloat(json, "Reserve");
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing Reserve - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        return reserve;
    }

    private float parseBid(String json) {
        float bid = 0.0f;
        try {
            bid = this.parseFloat(json, "Bid");
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing Bid - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        return bid;
    }

    private ListingType parseType(String json) {
        String type = "";
        try {
            type = this.parseString(json, "Type");
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing Type - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        switch (type) {
            case "SERVER_LISTING_UNLIMITED": {
                return ListingType.SERVER_LISTING_UNLIMITED;
            }
            case "SERVER_LISTING": {
                return ListingType.SERVER_LISTING;
            }
            case "PLAYER_AUCTION": {
                return ListingType.PLAYER_AUCTION;
            }
        }
        return ListingType.PLAYER_LISTING;
    }

    private String parseNBTTag(String json) {
        String nbt = null;
        try {
            nbt = this.parseNBT(json);
            if (nbt.isEmpty()) {
                nbt = "{}";
            }
        }
        catch (MalformedJsonException e) {
            AuctionHouse.logger.warning(String.format("MalformedJsonException parsing NBT - %s", e.getMessage()));
            AuctionHouse.logger.info(String.format("json: %s", json));
        }
        return nbt;
    }

    private List<String> splitEscaped(String json) {
        ArrayList<String> strings = new ArrayList<String>();
        ArrayList<Integer> split = new ArrayList<Integer>();
        int escaped = 0;
        for (int i = 0; i < json.length(); ++i) {
            char c = json.charAt(i);
            if (c == '{') {
                ++escaped;
                continue;
            }
            if (c == '}') {
                --escaped;
                continue;
            }
            if (c != ',' || escaped != 0) continue;
            split.add(i);
        }
        if (split.isEmpty()) {
            strings.add(json);
        } else {
            int l = 0;
            for (Integer pos : split) {
                int r = pos;
                String s = json.substring(l, r);
                strings.add(s);
                l = r + 1;
            }
            String s = json.substring(l);
            strings.add(s);
        }
        return strings;
    }

    private String unquote(String json) throws MalformedJsonException {
        int lb = json.indexOf("\"");
        int rb = json.lastIndexOf("\"");
        if (lb != -1 && rb != -1 && lb < rb) {
            return json.substring(lb + 1, rb);
        }
        throw new MalformedJsonException("Missing quotes");
    }

    private String unbracket(String json) throws MalformedJsonException {
        int lb = json.indexOf("{");
        int rb = json.lastIndexOf("}");
        if (lb != -1 && rb != -1 && lb < rb) {
            return json.substring(lb + 1, rb);
        }
        throw new MalformedJsonException("Missing brackets");
    }
}

