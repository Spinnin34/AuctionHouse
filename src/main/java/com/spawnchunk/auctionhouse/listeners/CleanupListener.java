package com.spawnchunk.auctionhouse.listeners;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.events.ListingCleanupEvent;
import com.spawnchunk.auctionhouse.modules.Auctions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CleanupListener
implements Listener {
    @EventHandler(priority=EventPriority.NORMAL)
    public void onCleanup(ListingCleanupEvent event) {
        int removed = 0;
        removed += Auctions.deleteAbandonedItems();
        if ((removed += Auctions.deleteExpiredSoldItems()) > 0) {
            AuctionHouse.logger.info(String.format("Cleaned %d listing%s", removed, removed > 1 ? "s" : ""));
        }
    }
}

