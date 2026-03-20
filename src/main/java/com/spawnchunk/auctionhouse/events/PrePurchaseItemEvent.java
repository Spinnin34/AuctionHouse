package com.spawnchunk.auctionhouse.events;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrePurchaseItemEvent
extends Event
implements Cancellable {
    private final String world;
    private final String seller_uuid;
    private final String buyer_uuid;
    private float price;
    private ItemStack item;
    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    public PrePurchaseItemEvent(String world, @NotNull String seller_uuid, @NotNull String buyer_uuid, float price, @Nullable ItemStack item) {
        this.world = world;
        this.seller_uuid = seller_uuid;
        this.buyer_uuid = buyer_uuid;
        this.price = price;
        this.item = item;
    }

    public String getWorld() {
        return this.world;
    }

    public OfflinePlayer getSeller() {
        UUID uuid = UUID.fromString(this.seller_uuid);
        return Bukkit.getOfflinePlayer((UUID)uuid);
    }

    public OfflinePlayer getBuyer() {
        UUID uuid = this.buyer_uuid.isEmpty() ? null : UUID.fromString(this.buyer_uuid);
        return uuid != null ? Bukkit.getOfflinePlayer((UUID)uuid) : null;
    }

    public String getSeller_UUID() {
        return this.seller_uuid;
    }

    public String getBuyer_UUID() {
        return this.buyer_uuid;
    }

    public float getPrice() {
        return this.price;
    }

    public void setPrice(float price) {
        this.price = price;
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

