package com.spawnchunk.auctionhouse.placeholders;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.modules.Auctions;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MVdWExpansion {
    private final AuctionHouse plugin;
    private final String identifier;

    public MVdWExpansion(AuctionHouse plugin) {
        this.plugin = plugin;
        this.identifier = plugin.getName().toLowerCase();
    }

    public void register() {
        PlaceholderAPI.registerPlaceholder((Plugin)this.plugin, (String)(this.identifier + "_active_listings"), (PlaceholderReplacer)new PlaceholderReplacer(){

            public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
                Player player = event.getPlayer();
                Auctions.updateCounts(player);
                int count = Auctions.getActiveListingsCount();
                return String.format("%d", count);
            }
        });
        PlaceholderAPI.registerPlaceholder((Plugin)this.plugin, (String)(this.identifier + "_player_listings"), (PlaceholderReplacer)new PlaceholderReplacer(){

            public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
                Player player = event.getPlayer();
                Auctions.updateCounts(player);
                int count = Auctions.getPlayerListingsCount((OfflinePlayer)player);
                return String.format("%d", count);
            }
        });
        PlaceholderAPI.registerPlaceholder((Plugin)this.plugin, (String)(this.identifier + "_expired_listings"), (PlaceholderReplacer)new PlaceholderReplacer(){

            public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
                Player player = event.getPlayer();
                Auctions.updateCounts(player);
                int count = Auctions.getExpiredListingsCount((OfflinePlayer)player);
                return String.format("%d", count);
            }
        });
        PlaceholderAPI.registerPlaceholder((Plugin)this.plugin, (String)(this.identifier + "_sold_items"), (PlaceholderReplacer)new PlaceholderReplacer(){

            public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
                Player player = event.getPlayer();
                Auctions.updateCounts(player);
                int count = Auctions.getSoldItemsCount((OfflinePlayer)player);
                return String.format("%d", count);
            }
        });
        PlaceholderAPI.registerPlaceholder((Plugin)this.plugin, (String)(this.identifier + "_max_listings"), (PlaceholderReplacer)new PlaceholderReplacer(){

            public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
                Player player = event.getPlayer();
                int count = Auctions.getMaxListings(player);
                return String.format("%d", count);
            }
        });
    }
}

