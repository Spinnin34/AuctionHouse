package com.spawnchunk.auctionhouse.modules;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.modules.Auctions;
import com.spawnchunk.auctionhouse.modules.Listing;
import com.spawnchunk.auctionhouse.modules.ListingType;
import com.spawnchunk.auctionhouse.modules.SortOrder;
import com.spawnchunk.auctionhouse.storage.DatabaseStorage;
import com.spawnchunk.auctionhouse.util.TimeUtil;
import com.spawnchunk.auctionhouse.util.WorldUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Listings {
    private Map<Long, Listing> listings = new LinkedHashMap<Long, Listing>();

    public Listings() {
    }

    public Listings(Map<Long, Listing> listings) {
        Set<Long> keys = listings.keySet();
        for (Long key : keys) {
            Listing listing = listings.get(key);
            this.listings.put(key, listing);
        }
    }

    public Map<Long, Listing> getListings() {
        return this.listings;
    }

    public void setListings(Map<Long, Listing> listings) {
        this.listings = listings;
    }

    private boolean containsIgnoreCase(String string, String filter) {
        if (string == null || string.isEmpty() || filter == null || filter.isEmpty()) {
            return false;
        }
        return string.toLowerCase().contains(filter.toLowerCase());
    }

    private boolean containsIgnoreCase(List<String> strings, String filter) {
        if (strings == null || strings.isEmpty() || filter == null || filter.isEmpty()) {
            return false;
        }
        return strings.stream().anyMatch(string -> this.containsIgnoreCase((String)string, filter));
    }

    private boolean containsIgnoreCase(Set<Enchantment> enchantments, String filter) {
        if (enchantments == null || enchantments.isEmpty() || filter == null || filter.isEmpty()) {
            return false;
        }
        return enchantments.stream().anyMatch(enchantment -> this.containsIgnoreCase(enchantment.getKey().getKey(), filter));
    }

    public Listings getFilteredListings(Player player, String filter, SortOrder order) {
        switch (order) {
            case PRICE_LOWEST: {
                return this.getFilteredListingsPrice(player, filter, true);
            }
            case PRICE_HIGHEST: {
                return this.getFilteredListingsPrice(player, filter, false);
            }
            case CHRONO_NEWEST: {
                return this.getFilteredListingsChrono(player, filter, false);
            }
        }
        return this.getFilteredListingsChrono(player, filter, true);
    }

    public Listings getFilteredListingsChrono(Player player, String filter, boolean chronological) {
        Comparator<Long> comparator = chronological ? Comparator.naturalOrder() : Comparator.reverseOrder();
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() > now).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).filter(entry -> this.containsIgnoreCase(((Listing)entry.getValue()).getItem().getType().getKey().getKey(), filter) || this.containsIgnoreCase(((Listing)entry.getValue()).getSeller().getName(), filter) || this.containsIgnoreCase(Objects.requireNonNull(((Listing)entry.getValue()).getItem().getItemMeta()).getDisplayName(), filter) || this.containsIgnoreCase(((Listing)entry.getValue()).getItem().getItemMeta().getLore(), filter) || ((Listing)entry.getValue()).getItem().getEnchantments().keySet().stream().anyMatch(enchantment -> enchantment.getKey().getKey().equalsIgnoreCase(filter)) || ((Listing)entry.getValue()).getItem().getItemMeta() instanceof EnchantmentStorageMeta && ((EnchantmentStorageMeta)((Listing)entry.getValue()).getItem().getItemMeta()).getStoredEnchants().keySet().stream().anyMatch(enchantment -> enchantment.getKey().getKey().equalsIgnoreCase(filter))).sorted(Map.Entry.comparingByKey(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getFilteredListingsPrice(Player player, String filter, boolean ascending) {
        Comparator<Listing> comparator = ascending ? Comparator.comparing(Listing::getPrice) : Comparator.comparing(Listing::getPrice).reversed();
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() > now).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).filter(entry -> this.containsIgnoreCase(((Listing)entry.getValue()).getItem().getType().getKey().getKey(), filter) || this.containsIgnoreCase(((Listing)entry.getValue()).getSeller().getName(), filter) || this.containsIgnoreCase(Objects.requireNonNull(((Listing)entry.getValue()).getItem().getItemMeta()).getDisplayName(), filter) || this.containsIgnoreCase(((Listing)entry.getValue()).getItem().getItemMeta().getLore(), filter) || this.containsIgnoreCase(((Listing)entry.getValue()).getItem().getEnchantments().keySet(), filter) || ((Listing)entry.getValue()).getItem().getEnchantments().keySet().stream().anyMatch(enchantment -> enchantment.getKey().getKey().equalsIgnoreCase(filter)) || ((Listing)entry.getValue()).getItem().getItemMeta() instanceof EnchantmentStorageMeta && ((EnchantmentStorageMeta)((Listing)entry.getValue()).getItem().getItemMeta()).getStoredEnchants().keySet().stream().anyMatch(enchantment -> enchantment.getKey().getKey().equalsIgnoreCase(filter))).sorted(Map.Entry.comparingByValue(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getActiveListings(Player player, SortOrder order) {
        switch (order) {
            case PRICE_LOWEST: {
                return this.getActiveListingsPrice(player, true);
            }
            case PRICE_HIGHEST: {
                return this.getActiveListingsPrice(player, false);
            }
            case CHRONO_NEWEST: {
                return this.getActiveListingsChrono(player, false);
            }
        }
        return this.getActiveListingsChrono(player, true);
    }

    public Listings getActiveListingsChrono(Player player, boolean chronological) {
        Comparator<Long> comparator = chronological ? Comparator.naturalOrder() : Comparator.reverseOrder();
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() > now).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).sorted(Map.Entry.comparingByKey(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getActiveListingsPrice(Player player, boolean ascending) {
        Comparator<Listing> comparator = ascending ? Comparator.comparing(Listing::getPrice) : Comparator.comparing(Listing::getPrice).reversed();
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() > now).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).sorted(Map.Entry.comparingByValue(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getActiveListingsUnsorted(Player player) {
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() > now).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getPlayerListings(Player player, SortOrder order) {
        switch (order) {
            case PRICE_LOWEST: {
                return this.getPlayerListingsPrice(player, true);
            }
            case PRICE_HIGHEST: {
                return this.getPlayerListingsPrice(player, false);
            }
            case CHRONO_NEWEST: {
                return this.getPlayerListingsChrono(player, false);
            }
        }
        return this.getPlayerListingsChrono(player, true);
    }

    public Listings getPlayerListingsChrono(Player player, boolean chronological) {
        Comparator<Long> comparator = chronological ? Comparator.naturalOrder() : Comparator.reverseOrder();
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        String player_uuid = player.getUniqueId().toString();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() >= now).filter(entry -> !((Listing)entry.getValue()).getType().isServer()).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> ((Listing)entry.getValue()).getSeller_UUID().equals(player_uuid)).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).sorted(Map.Entry.comparingByKey(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getPlayerListingsPrice(Player player, boolean ascending) {
        Comparator<Listing> comparator = ascending ? Comparator.comparing(Listing::getPrice) : Comparator.comparing(Listing::getPrice).reversed();
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        String player_uuid = player.getUniqueId().toString();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() >= now).filter(entry -> !((Listing)entry.getValue()).getType().isServer()).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> ((Listing)entry.getValue()).getSeller_UUID().equals(player_uuid)).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).sorted(Map.Entry.comparingByValue(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getPlayerListingsUnsorted(Player player) {
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        String player_uuid = player.getUniqueId().toString();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() >= now).filter(entry -> !((Listing)entry.getValue()).getType().isServer()).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> ((Listing)entry.getValue()).getSeller_UUID().equals(player_uuid)).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getSoldItems(Player player, SortOrder order) {
        switch (order) {
            case PRICE_LOWEST: {
                return this.getSoldItemsPrice(player, true);
            }
            case PRICE_HIGHEST: {
                return this.getSoldItemsPrice(player, false);
            }
            case CHRONO_NEWEST: {
                return this.getSoldItemsChrono(player, false);
            }
        }
        return this.getSoldItemsChrono(player, true);
    }

    public Listings getSoldItemsChrono(Player player, boolean chronological) {
        Comparator<Long> comparator = chronological ? Comparator.naturalOrder() : Comparator.reverseOrder();
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        String player_uuid = player.getUniqueId().toString();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> !((Listing)entry.getValue()).getType().isServer()).filter(entry -> !((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> ((Listing)entry.getValue()).getSeller_UUID().equals(player_uuid)).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).sorted(Map.Entry.comparingByKey(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getSoldItemsPrice(Player player, boolean ascending) {
        Comparator<Listing> comparator = ascending ? Comparator.comparing(Listing::getPrice) : Comparator.comparing(Listing::getPrice).reversed();
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        String player_uuid = player.getUniqueId().toString();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> !((Listing)entry.getValue()).getType().isServer()).filter(entry -> !((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> ((Listing)entry.getValue()).getSeller_UUID().equals(player_uuid)).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).sorted(Map.Entry.comparingByValue(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getSoldItemsUnsorted(Player player) {
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        String player_uuid = player.getUniqueId().toString();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> !((Listing)entry.getValue()).getType().isServer()).filter(entry -> !((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> ((Listing)entry.getValue()).getSeller_UUID().equals(player_uuid)).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getExpiredListings(Player player, SortOrder order) {
        switch (order) {
            case PRICE_LOWEST: {
                return this.getExpiredListingsPrice(player, true);
            }
            case PRICE_HIGHEST: {
                return this.getExpiredListingsPrice(player, false);
            }
            case CHRONO_NEWEST: {
                return this.getExpiredListingsChrono(player, false);
            }
        }
        return this.getExpiredListingsChrono(player, true);
    }

    public Listings getExpiredListingsChrono(Player player, boolean chronological) {
        Comparator<Long> comparator = chronological ? Comparator.naturalOrder() : Comparator.reverseOrder();
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        String player_uuid = player.getUniqueId().toString();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() < now).filter(entry -> (Long)entry.getKey() >= now - Config.auction_expired_duration).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> ((Listing)entry.getValue()).getSeller_UUID().equals(player_uuid)).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).sorted(Map.Entry.comparingByKey(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getExpiredListingsPrice(Player player, boolean ascending) {
        Comparator<Listing> comparator = ascending ? Comparator.comparing(Listing::getPrice) : Comparator.comparing(Listing::getPrice).reversed();
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        String player_uuid = player.getUniqueId().toString();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() < now).filter(entry -> (Long)entry.getKey() >= now - Config.auction_expired_duration).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> ((Listing)entry.getValue()).getSeller_UUID().equals(player_uuid)).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).sorted(Map.Entry.comparingByValue(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getExpiredListingsUnsorted(Player player) {
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        String player_uuid = player.getUniqueId().toString();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() < now).filter(entry -> (Long)entry.getKey() >= now - Config.auction_expired_duration).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> ((Listing)entry.getValue()).getSeller_UUID().equals(player_uuid)).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getUnclaimedListings(Player player) {
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        String player_uuid = player.getUniqueId().toString();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() < now - Config.auction_expired_duration).filter(entry -> (Long)entry.getKey() >= now - (Config.auction_expired_duration + Config.auction_unclaimed_duration)).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).filter(entry -> ((Listing)entry.getValue()).getSeller_UUID().equals(player_uuid)).filter(entry -> !Config.per_world_listings || (Config.group_worlds ? ((Listing)entry.getValue()).getWorld().startsWith(prefix) : ((Listing)entry.getValue()).getWorld().equals(world))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getExpiredListings() {
        Comparator<Long> comparator = Comparator.naturalOrder();
        long now = TimeUtil.now();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() < now).filter(entry -> (Long)entry.getKey() >= now - Config.auction_expired_duration).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).sorted(Map.Entry.comparingByKey(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getExpiredSoldItems() {
        Comparator<Long> comparator = Comparator.naturalOrder();
        long now = TimeUtil.now();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() < now - Config.auction_sold_duration).filter(entry -> !((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).sorted(Map.Entry.comparingByKey(comparator)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getUnclaimedListings() {
        long now = TimeUtil.now();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() < now - Config.auction_expired_duration).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listings getAbandonedListings() {
        long now = TimeUtil.now();
        LinkedHashMap<Long, Listing> l = this.listings.entrySet().parallelStream().filter(entry -> (Long)entry.getKey() < now - (Config.auction_expired_duration + Config.auction_unclaimed_duration)).filter(entry -> ((Listing)entry.getValue()).getBuyer_UUID().isEmpty()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return new Listings(l);
    }

    public Listing getListing(long timestamp) {
        return this.listings.get(timestamp);
    }

    public void putListing(Listing listing, long timestamp) {
        this.listings.put(timestamp, listing);
    }

    public boolean newListing(Listing listing) {
        long future;
        ListingType type = listing.getType();
        long now = TimeUtil.now();
        long l = future = type.isServer() ? now + Config.auction_future_duration : now + Config.auction_listing_duration;
        while (this.listings.containsKey(future)) {
            ++future;
        }
        long id = future;
        boolean result = DatabaseStorage.createListing(AuctionHouse.conn, id, listing);
        if (result) {
            this.listings.put(future, listing);
        }
        return result;
    }

    public boolean importListing(Listing listing, long timestamp) {
        while (this.listings.containsKey(timestamp)) {
            ++timestamp;
        }
        long id = timestamp;
        boolean result = DatabaseStorage.createListing(AuctionHouse.conn, id, listing);
        if (result) {
            this.listings.put(timestamp, listing);
        }
        return result;
    }

    public boolean removeListings(Set<Long> timestamps) {
        boolean result = true;
        final ArrayList<Long> removed = new ArrayList<Long>();
        for (long timestamp : timestamps) {
            boolean success = this.listings.keySet().remove(timestamp);
            if (success) {
                removed.add(timestamp);
                continue;
            }
            result = false;
        }
        new BukkitRunnable(){

            public void run() {
                DatabaseStorage.deleteListings(AuctionHouse.conn, removed);
            }
        }.runTaskAsynchronously((Plugin)AuctionHouse.plugin);
        return result;
    }

    public boolean removeListing(final long timestamp) {
        boolean result = this.listings.keySet().remove(timestamp);
        if (result) {
            new BukkitRunnable(){

                public void run() {
                    DatabaseStorage.deleteListing(AuctionHouse.conn, timestamp);
                }
            }.runTaskAsynchronously((Plugin)AuctionHouse.plugin);
        }
        return result;
    }

    public void clear() {
        this.listings.clear();
        new BukkitRunnable(){

            public void run() {
                DatabaseStorage.deleteAllListings(AuctionHouse.conn);
            }
        }.runTaskAsynchronously((Plugin)AuctionHouse.plugin);
    }

    public boolean soldListing(final long timestamp, OfflinePlayer buyer) {
        if (this.listings.containsKey(timestamp)) {
            final Listing listing = this.listings.get(timestamp);
            ListingType type = listing.getType();
            if (type != ListingType.SERVER_LISTING_UNLIMITED) {
                long now = TimeUtil.now();
                while (true) {
                    if (!this.listings.containsKey(now)) {
                        final long id = now;
                        listing.setBuyer(buyer);
                        this.listings.put(now, listing);
                        this.listings.keySet().remove(timestamp);
                        new BukkitRunnable(){

                            public void run() {
                                DatabaseStorage.updateListing(AuctionHouse.conn, timestamp, id, listing);
                            }
                        }.runTaskAsynchronously((Plugin)AuctionHouse.plugin);
                        return true;
                    }
                    ++now;
                }
            }
            return true;
        }
        return false;
    }

    public void cancelListing(final Long timestamp) {
        if (this.listings.containsKey(timestamp)) {
            final Listing listing = this.listings.get(timestamp);
            long now = TimeUtil.now();
            while (true) {
                if (!this.listings.containsKey(now)) {
                    this.listings.put(now, listing);
                    this.listings.keySet().remove(timestamp);
                    final long id = now;
                    new BukkitRunnable(){

                        public void run() {
                            DatabaseStorage.updateListing(AuctionHouse.conn, timestamp, id, listing);
                        }
                    }.runTaskAsynchronously((Plugin)AuctionHouse.plugin);
                    return;
                }
                ++now;
            }
        }
    }

    public int count() {
        return this.listings.keySet().size();
    }

    public void updateCounts(Player player) {
        UUID uuid = player.getUniqueId();
        String uuid_string = uuid.toString();
        String world = player.getWorld().getName();
        String prefix = WorldUtil.getWorldPrefix(world);
        long now = TimeUtil.now();
        int activeListingsCount = 0;
        int playerListingsCount = 0;
        int expiredListingsCount = 0;
        int soldItemsCount = 0;
        Set<Long> keys = this.listings.keySet();
        for (Long ts : keys) {
            boolean bWorld;
            Listing listing = this.listings.get(ts);
            String lWorld = listing.getWorld();
            bWorld = !Config.per_world_listings || (Config.group_worlds ? lWorld.startsWith(prefix) : lWorld.equals(world));
            boolean bServer = listing.getType().isServer();
            if (bServer || !bWorld) continue;
            boolean bPlayer = listing.getSeller_UUID().equals(uuid_string);
            boolean bNoBuyer = listing.getBuyer_UUID().isEmpty();
            if (bPlayer) {
                if (bNoBuyer) {
                    if (ts >= now) {
                        ++activeListingsCount;
                        ++playerListingsCount;
                        continue;
                    }
                    if (ts < now - Config.auction_expired_duration) continue;
                    ++expiredListingsCount;
                    continue;
                }
                ++soldItemsCount;
                continue;
            }
            if (!bNoBuyer || ts < now) continue;
            ++activeListingsCount;
        }
        Auctions.setActiveListingsCount(activeListingsCount);
        Auctions.setPlayerListingsCount(uuid, playerListingsCount);
        Auctions.setSoldItemsCount(uuid, soldItemsCount);
        Auctions.setExpiredListingsCount(uuid, expiredListingsCount);
    }
}

