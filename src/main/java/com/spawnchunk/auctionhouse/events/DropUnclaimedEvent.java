package com.spawnchunk.auctionhouse.events;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DropUnclaimedEvent
extends Event {
    private final CommandSender sender;
    private final Server server;
    private static final HandlerList handlers = new HandlerList();

    public DropUnclaimedEvent(CommandSender sender, Server server) {
        this.sender = sender;
        this.server = server;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public Server getServer() {
        return this.server;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

