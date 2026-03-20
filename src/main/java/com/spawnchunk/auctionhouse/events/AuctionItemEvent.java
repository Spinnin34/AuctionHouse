package com.spawnchunk.auctionhouse.events;

import com.spawnchunk.auctionhouse.events.ItemAction;
import com.spawnchunk.auctionhouse.modules.ListingType;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AuctionItemEvent
extends Event {
    private final ItemAction action;
    private final String world;
    private final String seller_uuid;
    private final String buyer_uuid;
    private final String bidder_uuid;
    private final float price;
    private final float reserve;
    private final float bid;
    private final ListingType type;
    private final ItemStack item;
    private static final HandlerList handlers = new HandlerList();

    public AuctionItemEvent(@NotNull ItemAction action, String world, @NotNull String seller_uuid, String buyer_uuid, String bidder_uuid, float price, float reserve, float bid, ListingType type, @NotNull ItemStack item) {
        this.action = action;
        this.world = world;
        this.seller_uuid = seller_uuid;
        this.buyer_uuid = buyer_uuid;
        this.bidder_uuid = bidder_uuid;
        this.price = price;
        this.reserve = reserve;
        this.bid = bid;
        this.type = type;
        this.item = item;
    }

    public ItemAction getItemAction() {
        return this.action;
    }

    public String getWorld() {
        return this.world;
    }

    public OfflinePlayer getSeller() {
        try {
            UUID uuid = UUID.fromString(this.seller_uuid);
            return Bukkit.getOfflinePlayer((UUID)uuid);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    public OfflinePlayer getBuyer() {
        UUID uuid = this.buyer_uuid.isEmpty() ? null : UUID.fromString(this.buyer_uuid);
        return uuid != null ? Bukkit.getOfflinePlayer((UUID)uuid) : null;
    }

    public OfflinePlayer getBidder() {
        UUID uuid = this.bidder_uuid.isEmpty() ? null : UUID.fromString(this.bidder_uuid);
        return uuid != null ? Bukkit.getOfflinePlayer((UUID)uuid) : null;
    }

    public String getSeller_UUID() {
        return this.seller_uuid;
    }

    public String getBuyer_UUID() {
        return this.buyer_uuid;
    }

    public String getBidder_UUID() {
        return this.bidder_uuid;
    }

    public float getPrice() {
        return this.price;
    }

    public float getReserve() {
        return this.reserve;
    }

    public float getBid() {
        return this.bid;
    }

    public ListingType getType() {
        return this.type;
    }

    public ItemStack getItem() {
        return this.item;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

