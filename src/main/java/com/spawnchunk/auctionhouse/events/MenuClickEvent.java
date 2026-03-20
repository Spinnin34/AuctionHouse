package com.spawnchunk.auctionhouse.events;

import com.spawnchunk.auctionhouse.menus.MenuClickType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MenuClickEvent
extends Event
implements Cancellable {
    private final Player player;
    private final String id;
    private final int slot;
    private final MenuClickType type;
    private boolean cancelled;
    private static final HandlerList handlers = new HandlerList();

    public MenuClickEvent(Player player, String id, int slot, MenuClickType type) {
        this.player = player;
        this.id = id;
        this.slot = slot;
        this.type = type;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getId() {
        return this.id;
    }

    public int getSlot() {
        return this.slot;
    }

    public MenuClickType getMenuClickType() {
        return this.type;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

