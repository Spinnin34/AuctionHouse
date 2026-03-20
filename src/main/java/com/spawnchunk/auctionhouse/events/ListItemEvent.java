package com.spawnchunk.auctionhouse.events;

import com.spawnchunk.auctionhouse.modules.ListingType;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ListItemEvent
extends Event
implements Cancellable {
    private final String world;
    private final String seller_uuid;
    private float price;
    private ItemStack item;
    private ListingType type;
    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    public ListItemEvent(String world, @NotNull String seller_uuid, float price, ListingType type, @NotNull ItemStack item) {
        this.world = world;
        this.seller_uuid = seller_uuid;
        this.price = price;
        this.type = type;
        this.item = item;
    }

    public String getWorld() {
        return this.world;
    }

    public OfflinePlayer getSeller() {
        UUID uuid = UUID.fromString(this.seller_uuid);
        return Bukkit.getOfflinePlayer((UUID)uuid);
    }

    public String getSeller_UUID() {
        return this.seller_uuid;
    }

    public float getPrice() {
        return this.price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public ListingType getType() {
        return this.type;
    }

    public void setType(ListingType type) {
        this.type = type;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void setItem(ItemStack item) {
        this.item = item.clone();
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

