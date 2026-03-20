package com.spawnchunk.auctionhouse.listeners;

import com.spawnchunk.auctionhouse.AuctionHouse;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageSentEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;

public class DiscordSRVListener {
    @Subscribe(priority=ListenerPriority.MONITOR)
    public void discordMessageReceived(DiscordGuildMessageReceivedEvent event) {
        Message message = event.getMessage();
        AuctionHouse.logger.info("Received a chat message on Discord: " + message);
    }

    @Subscribe(priority=ListenerPriority.MONITOR)
    public void discordMessageSent(DiscordGuildMessageSentEvent event) {
        Message message = event.getMessage();
        AuctionHouse.logger.info("Sent a chat message to Discord: " + message);
    }
}

