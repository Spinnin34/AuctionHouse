package com.spawnchunk.auctionhouse.placeholders;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.modules.Auctions;
import java.util.Arrays;
import java.util.List;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class Expansion
extends PlaceholderExpansion {
    private final AuctionHouse plugin;
    private final String identifier;

    public Expansion(AuctionHouse plugin) {
        this.plugin = plugin;
        this.identifier = plugin.getName().toLowerCase();
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getAuthor() {
        return "klugemonkey";
    }

    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public String getRequiredPlugin() {
        return this.plugin.getName();
    }

    public List<String> getPlaceholders() {
        return Arrays.asList(this.placeholder("active_listings"), this.placeholder("player_listings"), this.placeholder("expired_listings"), this.placeholder("sold_items"), this.placeholder("max_listings"));
    }

    public boolean persist() {
        return true;
    }

    public String onRequest(OfflinePlayer op, String params) {
        if (op == null) {
            return null;
        }
        if (!op.isOnline() || op.getPlayer() == null) {
            return null;
        }
        Player player = op.getPlayer();
        if (params.equals("active_listings")) {
            Auctions.updateCounts(player);
            int count = Auctions.getActiveListingsCount();
            return String.format("%d", count);
        }
        if (params.equals("player_listings")) {
            Auctions.updateCounts(player);
            int count = Auctions.getPlayerListingsCount((OfflinePlayer)player);
            return String.format("%d", count);
        }
        if (params.equals("expired_listings")) {
            Auctions.updateCounts(player);
            int count = Auctions.getExpiredListingsCount((OfflinePlayer)player);
            return String.format("%d", count);
        }
        if (params.equals("sold_items")) {
            Auctions.updateCounts(player);
            int count = Auctions.getSoldItemsCount((OfflinePlayer)player);
            return String.format("%d", count);
        }
        if (params.equals("max_listings")) {
            if (op.isOnline()) {
                int count = Auctions.getMaxListings(player);
                return String.format("%d", count);
            }
            return "?";
        }
        return null;
    }

    private String placeholder(String param) {
        return "%" + this.identifier + "_" + param + "%";
    }
}

