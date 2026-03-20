package com.spawnchunk.auctionhouse.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MenuCloseEvent
extends Event {
    private final Player player;
    private final String id;
    private static final HandlerList handlers = new HandlerList();

    public MenuCloseEvent(Player player, String id) {
        this.player = player;
        this.id = id;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getId() {
        return this.id;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

