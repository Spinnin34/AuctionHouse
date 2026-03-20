package com.spawnchunk.auctionhouse.modules;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.events.AuctionItemEvent;
import com.spawnchunk.auctionhouse.events.ItemAction;
import com.spawnchunk.auctionhouse.events.ListItemEvent;
import com.spawnchunk.auctionhouse.events.MenuClickEvent;
import com.spawnchunk.auctionhouse.events.MenuCloseEvent;
import com.spawnchunk.auctionhouse.events.PurchaseItemEvent;
import com.spawnchunk.auctionhouse.menus.ActiveListingsMenu;
import com.spawnchunk.auctionhouse.menus.ConfirmListingMenu;
import com.spawnchunk.auctionhouse.menus.ExpiredListingsMenu;
import com.spawnchunk.auctionhouse.menus.PlayerListingsMenu;
import com.spawnchunk.auctionhouse.menus.PurchaseItemMenu;
import com.spawnchunk.auctionhouse.menus.SoldItemsMenu;
import com.spawnchunk.auctionhouse.modules.Economy;
import com.spawnchunk.auctionhouse.modules.Listing;
import com.spawnchunk.auctionhouse.modules.ListingType;
import com.spawnchunk.auctionhouse.modules.Listings;
import com.spawnchunk.auctionhouse.storage.LocaleStorage;
import com.spawnchunk.auctionhouse.util.ItemUtil;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import com.spawnchunk.auctionhouse.util.SoundUtil;
import com.spawnchunk.auctionhouse.util.TimeUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

public class Auctions {
    private static final Map<UUID, ActiveListingsMenu> activeListingsMenus = new HashMap<UUID, ActiveListingsMenu>();
    private static final Map<UUID, PlayerListingsMenu> playerListingsMenus = new HashMap<UUID, PlayerListingsMenu>();
    private static final Map<UUID, ExpiredListingsMenu> expiredListingsMenus = new HashMap<UUID, ExpiredListingsMenu>();
    private static final Map<UUID, SoldItemsMenu> soldItemsMenus = new HashMap<UUID, SoldItemsMenu>();
    private static final Map<UUID, PurchaseItemMenu> purchaseItemMenus = new HashMap<UUID, PurchaseItemMenu>();
    private static final Map<UUID, ConfirmListingMenu> confirmListingMenus = new HashMap<UUID, ConfirmListingMenu>();
    private static int activeListingsCount = 0;
    private static final Map<UUID, Integer> playerListingsCount = new HashMap<UUID, Integer>();
    private static final Map<UUID, Integer> expiredListingsCount = new HashMap<UUID, Integer>();
    private static final Map<UUID, Integer> soldItemsCount = new HashMap<UUID, Integer>();
    public static Set<UUID> lock = new HashSet<UUID>();
    public static String filter;
    public static boolean menu_mode;

    public static void initializeMenus() {
        activeListingsMenus.clear();
        activeListingsCount = 0;
        playerListingsMenus.clear();
        playerListingsCount.clear();
        expiredListingsMenus.clear();
        expiredListingsCount.clear();
        soldItemsMenus.clear();
        soldItemsCount.clear();
        purchaseItemMenus.clear();
        confirmListingMenus.clear();
    }

    public static void openActiveListingsMenu(Player player) {
        Auctions.openActiveListingsMenu(player, 0);
    }

    public static void openActiveListingsMenu(final Player player, final int page) {
        ActiveListingsMenu menu;
        UUID uuid = player.getUniqueId();
        if (activeListingsMenus.containsKey(uuid) && activeListingsMenus.get(uuid) != null) {
            menu = activeListingsMenus.get(uuid);
        } else {
            menu = new ActiveListingsMenu(player, menu_mode);
            activeListingsMenus.put(uuid, menu);
        }
        new BukkitRunnable(){

            public void run() {
                menu.show(player, menu_mode, filter, page);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public static void rebuildActiveListingsMenus() {
        for (UUID uuid : activeListingsMenus.keySet()) {
            ActiveListingsMenu menu = activeListingsMenus.get(uuid);
            if (menu == null) continue;
            menu.initialize();
        }
    }

    public static void openPlayerListingsMenu(final Player player) {
        PlayerListingsMenu menu;
        final int page = 0;
        UUID uuid = player.getUniqueId();
        if (playerListingsMenus.containsKey(uuid) && playerListingsMenus.get(uuid) != null) {
            menu = playerListingsMenus.get(uuid);
        } else {
            menu = new PlayerListingsMenu(player);
            playerListingsMenus.put(uuid, menu);
        }
        new BukkitRunnable(){

            public void run() {
                menu.show(player, page);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public static void rebuildPlayerListingsMenus() {
        for (UUID uuid : playerListingsMenus.keySet()) {
            PlayerListingsMenu menu = playerListingsMenus.get(uuid);
            if (menu == null) continue;
            menu.initialize();
        }
    }

    public static void openExpiredListingsMenu(final Player player) {
        ExpiredListingsMenu menu;
        final int page = 0;
        UUID uuid = player.getUniqueId();
        if (expiredListingsMenus.containsKey(uuid) && expiredListingsMenus.get(uuid) != null) {
            menu = expiredListingsMenus.get(uuid);
        } else {
            menu = new ExpiredListingsMenu(player);
            expiredListingsMenus.put(uuid, menu);
        }
        new BukkitRunnable(){

            public void run() {
                menu.show(player, page);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public static void rebuildExpiredListingsMenus() {
        for (UUID uuid : expiredListingsMenus.keySet()) {
            ExpiredListingsMenu menu = expiredListingsMenus.get(uuid);
            if (menu == null) continue;
            menu.initialize();
        }
    }

    public static void openSoldItemsMenu(final Player player) {
        SoldItemsMenu menu;
        final int page = 0;
        UUID uuid = player.getUniqueId();
        if (soldItemsMenus.containsKey(uuid) && soldItemsMenus.get(uuid) != null) {
            menu = soldItemsMenus.get(uuid);
        } else {
            menu = new SoldItemsMenu(player);
            soldItemsMenus.put(uuid, menu);
        }
        new BukkitRunnable(){

            public void run() {
                menu.show(player, page);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public static void rebuildSoldItemsMenus() {
        for (UUID uuid : soldItemsMenus.keySet()) {
            SoldItemsMenu menu = soldItemsMenus.get(uuid);
            if (menu == null) continue;
            menu.initialize();
        }
    }

    public static void openPurchaseItemMenu(final Player player, final Long timestamp, final int return_page) {
        PurchaseItemMenu menu;
        UUID uuid = player.getUniqueId();
        if (purchaseItemMenus.containsKey(uuid) && purchaseItemMenus.get(uuid) != null) {
            menu = purchaseItemMenus.get(uuid);
        } else {
            menu = new PurchaseItemMenu(player);
            purchaseItemMenus.put(uuid, menu);
        }
        new BukkitRunnable(){

            public void run() {
                menu.show(player, timestamp, return_page);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public static void rebuildPurchaseItemMenus() {
        for (UUID uuid : purchaseItemMenus.keySet()) {
            PurchaseItemMenu menu = purchaseItemMenus.get(uuid);
            if (menu == null) continue;
            menu.initialize();
        }
    }

    private static void openConfirmListingMenu(final Player player, final ItemStack item, float price, double listing_fee, ListingType type) {
        if (Config.debug) {
            AuctionHouse.logger.info("openConfirmListingMenu");
        }
        UUID uuid = player.getUniqueId();
        final ConfirmListingMenu menu = new ConfirmListingMenu(player, price, listing_fee, type);
        if (confirmListingMenus.containsKey(uuid)) {
            Auctions.removeConfirmListingsMenu(player);
        }
        confirmListingMenus.put(uuid, menu);
        new BukkitRunnable(){

            public void run() {
                menu.show(player, item);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public static void removeConfirmListingsMenu(Player player) {
        UUID uuid = player.getUniqueId();
        ConfirmListingMenu menu = confirmListingMenus.get(uuid);
        if (menu != null) {
            MenuClickEvent.getHandlerList().unregister((Listener)menu);
            MenuCloseEvent.getHandlerList().unregister((Listener)menu);
        }
        confirmListingMenus.remove(uuid);
    }

    public static void RebuildAllMenus() {
        Auctions.rebuildActiveListingsMenus();
        Auctions.rebuildPlayerListingsMenus();
        Auctions.rebuildExpiredListingsMenus();
        Auctions.rebuildSoldItemsMenus();
        Auctions.rebuildPurchaseItemMenus();
    }

    private static boolean similarType(ItemStack item, ItemStack match) {
        Material comparisonType = item.getType();
        return match.getType() == Config.item_wildcard || comparisonType == match.getType();
    }

    private static boolean similarName(ItemStack item, ItemStack match) {
        ItemMeta m = match.getItemMeta();
        if (m == null) {
            return true;
        }
        if (!m.hasDisplayName()) {
            return true;
        }
        ItemMeta i = item.getItemMeta();
        String name = i != null ? (i.hasDisplayName() ? i.getDisplayName() : "") : "";
        String pattern = m.getDisplayName();
        if (pattern.startsWith("regex:")) {
            pattern = pattern.replace("regex:", "");
            return name.matches(pattern);
        }
        if (pattern.startsWith("fuzzy:")) {
            pattern = pattern.replace("fuzzy:", "");
            return name.contains(pattern);
        }
        return name.equals(pattern);
    }

    private static boolean similarLore(ItemStack item, ItemStack match) {
        List<String> mLore;
        ItemMeta m = match.getItemMeta();
        ItemMeta i = item.getItemMeta();
        if (m == null && i == null) {
            return true;
        }
        if (m == null) {
            return false;
        }
        if (i == null) {
            return false;
        }
        List<String> iLore = i.hasLore() ? i.getLore() : new ArrayList<String>();
        List<String> list = mLore = m.hasLore() ? m.getLore() : new ArrayList<String>();
        if (iLore == null) {
            return mLore == null;
        }
        if (mLore == null) {
            return true;
        }
        boolean similar = true;
        for (String pattern : mLore) {
            boolean hit = false;
            if (pattern.startsWith("regex:")) {
                pattern = pattern.replace("regex:", "");
                for (String line : iLore) {
                    if (!line.matches(pattern)) continue;
                    hit = true;
                    break;
                }
            } else if (pattern.startsWith("fuzzy:")) {
                pattern = pattern.replace("fuzzy:", "");
                for (String line : iLore) {
                    if (!line.contains(pattern)) continue;
                    hit = true;
                    break;
                }
            } else {
                for (String line : iLore) {
                    if (!line.equals(pattern)) continue;
                    hit = true;
                    break;
                }
            }
            if (hit) continue;
            similar = false;
            break;
        }
        return similar;
    }

    private static boolean similarEnchants(ItemStack item, ItemStack match) {
        ItemMeta m = match.getItemMeta();
        ItemMeta i = item.getItemMeta();
        if (m == null && i == null) {
            return true;
        }
        if (m == null) {
            return false;
        }
        if (i == null) {
            return false;
        }
        return i.hasEnchants() && m.hasEnchants() ? i.getEnchants().equals(m.getEnchants()) : !m.hasEnchants();
    }

    private static boolean similarDamage(ItemStack item, ItemStack match) {
        ItemMeta matchMeta = match.getItemMeta();
        ItemMeta itemMeta = item.getItemMeta();
        if (matchMeta == null && itemMeta == null) {
            return true;
        }
        if (itemMeta instanceof Damageable && matchMeta instanceof Damageable) {
            int matchDamage;
            int itemDamage = ((Damageable)itemMeta).getDamage();
            return itemDamage == (matchDamage = ((Damageable)matchMeta).getDamage());
        }
        return !(itemMeta instanceof Damageable) && !(matchMeta instanceof Damageable);
    }

    private static boolean similarUnbreakable(ItemStack item, ItemStack match) {
        boolean itemIsUnbreakable = item.getItemMeta() != null && item.getItemMeta().isUnbreakable();
        boolean matchIsUnbreakable = match.getItemMeta() != null && match.getItemMeta().isUnbreakable();
        return itemIsUnbreakable == matchIsUnbreakable;
    }

    private static boolean similarCustomModelData(ItemStack item, ItemStack match) {
        int matchCustomModelData;
        if (AuctionHouse.mcVersion < 1141) {
            return true;
        }
        int itemCustomModelData = AuctionHouse.nms.getCustomModelData(item);
        return itemCustomModelData == (matchCustomModelData = AuctionHouse.nms.getCustomModelData(match));
    }

    private static boolean similarPersistentData(ItemStack item, ItemStack match) {
        if (AuctionHouse.mcVersion < 1141) {
            return true;
        }
        Map<String, Object> itemPersistentData = AuctionHouse.nms.getPersistentData(item);
        Map<String, Object> matchPersistentData = AuctionHouse.nms.getPersistentData(match);
        if (matchPersistentData.isEmpty()) {
            return true;
        }
        for (String key : matchPersistentData.keySet()) {
            Object value = matchPersistentData.get(key);
            if (itemPersistentData.containsKey(key)) {
                Object value2 = itemPersistentData.get(key);
                if (Objects.equals(value, value2)) continue;
                return false;
            }
            return false;
        }
        return true;
    }

    private static boolean isSimilar(ItemStack item, ItemStack match, String section) {
        boolean result;
        boolean f7;
        if (match == null) {
            return false;
        }
        if (item == match) {
            return true;
        }
        boolean f = Config.wildcard_item.getOrDefault(section, false) != false || Auctions.similarType(item, match);
        boolean f1 = Config.wildcard_name.getOrDefault(section, false) != false || Auctions.similarName(item, match);
        boolean f2 = Config.wildcard_lore.getOrDefault(section, false) != false || Auctions.similarLore(item, match);
        boolean f3 = Config.wildcard_enchantments.getOrDefault(section, false) != false || Auctions.similarEnchants(item, match);
        boolean f4 = Config.wildcard_damage.getOrDefault(section, false) != false || Auctions.similarDamage(item, match);
        boolean f5 = Config.wildcard_unbreakable.getOrDefault(section, false) != false || Auctions.similarUnbreakable(item, match);
        boolean f6 = Config.wildcard_custommodeldata.getOrDefault(section, false) != false || Auctions.similarCustomModelData(item, match);
        boolean bl = f7 = Config.wildcard_persistentdata.getOrDefault(section, false) != false || Auctions.similarPersistentData(item, match);
        if (Config.debug) {
            AuctionHouse.logger.info(String.format("Testing %s against %s", item.getType().getKey().getKey(), match.getType() == Config.item_wildcard ? "WILDCARD" : match.getType().getKey().getKey()));
            AuctionHouse.logger.info(String.format("  similarType = %s", f ? "true" : "false"));
            AuctionHouse.logger.info(String.format("  similarName = %s", f1 ? "true" : "false"));
            AuctionHouse.logger.info(String.format("  similarLore = %s", f2 ? "true" : "false"));
            AuctionHouse.logger.info(String.format("  similarEnchants = %s", f3 ? "true" : "false"));
            AuctionHouse.logger.info(String.format("  similarDamage = %s", f4 ? "true" : "false"));
            AuctionHouse.logger.info(String.format("  similarUnbreakable = %s", f5 ? "true" : "false"));
            AuctionHouse.logger.info(String.format("  similarCustomModelData = %s", f6 ? "true" : "false"));
            AuctionHouse.logger.info(String.format("  similarPersistentData = %s", f7 ? "true" : "false"));
        }
        boolean bl2 = result = f && f1 && f2 && f3 && f4 && f5 && f6 && f7;
        if (Config.debug && result) {
            AuctionHouse.logger.info(String.format("Blocked restricted item matching restricted_items.%s", section));
        }
        return result;
    }

    public static void clearAllData(CommandSender sender) {
        AuctionHouse.listings.clear();
        Server server = AuctionHouse.plugin.getServer();
        for (Player player : server.getOnlinePlayers()) {
            Auctions.updateCounts(player);
        }
        sender.sendMessage("All auction data cleared!");
    }

    public static void createTestData(CommandSender sender, int entries) {
        OfflinePlayer[] players = AuctionHouse.plugin.getServer().getOfflinePlayers();
        int player_count = players.length;
        if (player_count < 1) {
            sender.sendMessage("No offline players available to create test data!");
            return;
        }
        Random random = new Random();
        Material[] materials = Material.values();
        int count = materials.length;
        int i = 0;
        while (i < entries) {
            int index = random.nextInt(count);
            Material material = materials[index];
            if (material == null || material == Material.AIR || !material.isItem()) continue;
            int max_stacksize = material.getMaxStackSize();
            int amount = random.nextInt(max_stacksize) + 1;
            ItemStack item = new ItemStack(material, amount);
            int price = random.nextInt(1000) + 1;
            int r = random.nextInt(player_count);
            OfflinePlayer seller = players[r];
            String world = ((World)Bukkit.getServer().getWorlds().get(0)).getName();
            if (seller.isOnline()) {
                world = ((Player)seller).getWorld().getName();
            }
            final Listing listing = new Listing(world, seller.getUniqueId().toString(), null, null, price, 0.0f, 0.0f, ListingType.PLAYER_LISTING, item);
            new BukkitRunnable(){

                public void run() {
                    AuctionHouse.listings.newListing(listing);
                }
            }.runTaskLater((Plugin)AuctionHouse.plugin, (long)(i + 1));
            if (seller.isOnline()) {
                Auctions.updateCounts(seller.getPlayer());
            }
            ++i;
        }
        sender.sendMessage(String.format("Creating %s test data entries in the background.", entries));
    }

    public static void sellItemInHand(Player player, float price, ListingType type) {
        Auctions.sellItemInHand(player, price, null, type);
    }

    public static void sellItemInHand(Player player, float price, @Nullable Integer count, ListingType type) {
        double balance;
        boolean bypass;
        boolean bl = bypass = player.isOp() && type.isServer();
        if (player.getGameMode().equals((Object)GameMode.CREATIVE) && Config.auction_prevent_creative) {
            MessageUtil.sendMessage(player, "warning.sell.creative", Config.locale);
            return;
        }
        if (player.getGameMode().equals((Object)GameMode.SPECTATOR) && Config.auction_prevent_spectator) {
            MessageUtil.sendMessage(player, "warning.sell.spectator", Config.locale);
            return;
        }
        if (!player.isOnline()) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack item = inventory.getItemInMainHand();
        if (item.getType().equals((Object)Material.AIR) || item.getType().equals((Object)Material.CAVE_AIR) || item.getType().equals((Object)Material.VOID_AIR)) {
            MessageUtil.sendMessage(player, "warning.sell.no_item", Config.locale);
            return;
        }
        if (!bypass) {
            for (String section : Config.restricted_items.keySet()) {
                ItemStack prohibited = Config.restricted_items.get(section);
                if (!Auctions.isSimilar(item, prohibited, section)) continue;
                MessageUtil.sendMessage(player, "warning.sell.restricted_item", Config.locale);
                return;
            }
        }
        if (Config.auction_prevent_filled_containers && ItemUtil.isFilledContainer(item) && !bypass) {
            MessageUtil.sendMessage(player, "warning.sell.filled_container", Config.locale);
            return;
        }
        if (!Config.auction_allow_damaged_items && ItemUtil.hasDamage(item) && !bypass) {
            MessageUtil.sendMessage(player, "warning.sell.damaged_item", Config.locale);
            return;
        }
        if ((double)price > Config.auction_max_sell_price && !bypass) {
            MessageUtil.sendMessage(player, "warning.sell.max_price", Config.locale, Config.auction_max_sell_price);
            return;
        }
        if ((double)price < Config.auction_min_sell_price && !bypass) {
            MessageUtil.sendMessage(player, "warning.sell.min_price", Config.locale, Config.auction_min_sell_price);
            return;
        }
        if (price < 0.0f) {
            MessageUtil.sendMessage(player, "warning.sell.negative_price", Config.locale);
            return;
        }
        if (count != null && (count <= 0 || count > item.getAmount())) {
            MessageUtil.sendMessage(player, "warning.sell.invalid_amount", Config.locale);
            return;
        }
        Auctions.updateCounts(player);
        int listings = Auctions.getPlayerListingsCount((OfflinePlayer)player);
        int max_listings = Auctions.getMaxListings(player);
        if (max_listings == 0 && !player.isOp()) {
            MessageUtil.sendMessage(player, "warning.sell.unavailable", Config.locale);
            return;
        }
        if (listings >= max_listings && !bypass) {
            MessageUtil.sendMessage(player, "warning.sell.max_listings", Config.locale, max_listings);
            return;
        }
        UUID uuid = player.getUniqueId();
        if (AuctionHouse.playerCooldowns.containsKey(uuid) && !bypass) {
            long lastListing = AuctionHouse.playerCooldowns.get(uuid);
            long nextListing = lastListing + Config.auction_listing_cooldown;
            if (TimeUtil.now() < nextListing) {
                long remaining = nextListing - TimeUtil.now();
                MessageUtil.sendMessage(player, "warning.listing.cooldown", Config.locale, TimeUtil.duration(remaining, true));
                return;
            }
        }
        double listing_fee = bypass ? 0.0 : (double)price * (Config.auction_listing_rate / 100.0) + Config.auction_listing_price;
        OfflinePlayer seller = AuctionHouse.plugin.getServer().getOfflinePlayer(player.getUniqueId());
        String world = player.getWorld().getName();
        if (listing_fee > 0.0 && listing_fee > (balance = Economy.getBalance(seller, world))) {
            MessageUtil.sendMessage(player, "warning.sell.insufficient_funds", Config.locale);
            if (Config.debug) {
                AuctionHouse.logger.info(String.format("listing_fee = %.2f", listing_fee));
            }
            if (Config.debug) {
                AuctionHouse.logger.info(String.format("balance = %.2f", balance));
            }
            return;
        }
        if (!player.isOnline()) {
            return;
        }
        lock.add(player.getUniqueId());
        if (count == null || item.getAmount() == count.intValue()) {
            inventory.setItemInMainHand(new ItemStack(Material.AIR, 1));
        } else if (item.getAmount() > count) {
            ItemStack is = inventory.getItemInMainHand().clone();
            is.setAmount(is.getAmount() - count);
            inventory.setItemInMainHand(is);
            item.setAmount(count.intValue());
        } else {
            MessageUtil.sendMessage(player, "warning.sell.invalid_amount", Config.locale);
            lock.remove(player.getUniqueId());
            return;
        }
        if (listing_fee > 0.0) {
            Auctions.openConfirmListingMenu(player, item, price, listing_fee, type);
        } else {
            Auctions.completeListing(player, item, price, listing_fee, type, false);
        }
    }

    public static void completeListing(Player player, ItemStack item, float price, double listing_fee, ListingType type, boolean cancelled) {
        boolean bypass = player.isOp() && type.isServer();
        Listing listing = null;
        String world = player.getWorld().getName();
        String seller_uuid = player.getUniqueId().toString();
        String seller_name = type.isServer() ? AuctionHouse.servername : player.getName();
        boolean cancel = cancelled;
        if (!player.isOnline()) {
            cancel = true;
        }
        if (!cancel) {
            double balance;
            if (listing_fee > 0.0 && listing_fee > (balance = Economy.getBalance((OfflinePlayer)player, world))) {
                MessageUtil.sendMessage(player, "warning.sell.insufficient_funds", Config.locale);
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("listing_fee = %.2f", listing_fee));
                }
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("balance = %.2f", balance));
                }
                cancel = true;
            }
            ListItemEvent event = new ListItemEvent(world, seller_uuid, price, type, item);
            Bukkit.getPluginManager().callEvent((Event)event);
            if (!event.isCancelled()) {
                double escrow = 0.0;
                boolean withdraw_success = bypass;
                if (!bypass) {
                    withdraw_success = Economy.withdrawPlayer((OfflinePlayer)player, world, listing_fee);
                }
                if (withdraw_success) {
                    escrow = listing_fee;
                    float reserve = 0.0f;
                    listing = new Listing(world, seller_uuid, null, null, price, reserve, 0.0f, type, item);
                    if (AuctionHouse.listings.newListing(listing)) {
                        if (listing_fee > 0.0) {
                            MessageUtil.sendMessage(player, "message.sell.fee", Config.locale, listing_fee);
                            MessageUtil.sendMessage(player, "message.sell.seller_balance", Config.locale, Economy.getBalance((OfflinePlayer)player, world));
                        }
                    } else {
                        MessageUtil.sendMessage(player, "warning.sell.unknown", Config.locale);
                        cancel = true;
                        if (!bypass) {
                            Economy.depositPlayer((OfflinePlayer)player, world, escrow);
                        }
                    }
                } else {
                    MessageUtil.sendMessage(player, "warning.sell.unknown", Config.locale);
                    cancel = true;
                }
                UUID uuid = player.getUniqueId();
                AuctionHouse.playerCooldowns.put(uuid, TimeUtil.now());
                Auctions.updateCounts(player);
            } else {
                MessageUtil.sendMessage(player, "warning.sell.unknown", Config.locale);
                cancel = true;
            }
        }
        if (cancel) {
            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(new ItemStack[]{item});
            for (Integer index : remaining.keySet()) {
                player.getWorld().dropItem(player.getLocation(), (ItemStack)remaining.get(index));
            }
        } else {
            int amount = item.getAmount();
            String translatable_name = ItemUtil.getTranslatableName(item);
            String item_name = MessageUtil.sectionSymbol(ItemUtil.getTranslatableName(item));
            MessageUtil.sendMessage(player, "message.sell.listed_item", Config.locale, amount, item_name, Float.valueOf(price));
            for (Player p : AuctionHouse.plugin.getServer().getOnlinePlayers()) {
                if (p == player) continue;
                if (Config.announce_chat_listings) {
                    MessageUtil.sendMessage(listing.getWorld(), p, "message.sell.player.listed_item", Config.locale, seller_name, amount, item_name, Float.valueOf(price));
                }
                if (!Config.announce_action_bar_listings) continue;
                MessageUtil.sendActionBar(listing.getWorld(), p, 0L, "message.sell.player.listed_item", Config.locale, seller_name, amount, item_name, Float.valueOf(price));
            }
            if (Config.announce_discord_listings) {
                MessageUtil.discordMessage("discord.sell.player.listed_item", Config.locale, seller_name, amount, MessageUtil.nocolor(item_name), Float.valueOf(price));
            }
            AuctionItemEvent event1 = new AuctionItemEvent(ItemAction.ITEM_LISTED, listing.getWorld(), seller_uuid, listing.getBuyer_UUID(), listing.getBidder_UUID(), listing.getPrice(), listing.getReserve(), listing.getBid(), listing.getType(), listing.getItem());
            Bukkit.getPluginManager().callEvent((Event)event1);
        }
        lock.remove(player.getUniqueId());
    }

    public static int getMaxListings(Player player) {
        int max = Config.auction_default_max_listings;
        if (Config.debug) {
            AuctionHouse.logger.info(String.format("Found auction.defaultMaxListings: %d in config.yml", max));
        }
        Set<org.bukkit.permissions.PermissionAttachmentInfo> effective = player.getEffectivePermissions();
        for (org.bukkit.permissions.PermissionAttachmentInfo info : effective) {
            String perm = info.getPermission();
            if (!perm.toLowerCase().startsWith("auctionhouse.auctions.")) continue;
            String value = perm.replaceAll("\\D+", "");
            try {
                int max_perm = Integer.parseInt(value);
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("Found effective permission %s (either player or group)", perm));
                }
                max = Math.max(max, max_perm);
            }
            catch (NumberFormatException ignored) {
                if (!Config.debug) continue;
                AuctionHouse.logger.info(String.format("Could not parse number value from auctionhouse.auctions.# permission for %s", player.getName()));
            }
        }
        if (AuctionHouse.chat != null && AuctionHouse.chat.isEnabled()) {
            String[] groups = AuctionHouse.chat.getPlayerGroups(player);
            if (groups != null) {
                for (String group : groups) {
                    int max_group_meta = AuctionHouse.chat.getGroupInfoInteger(player.getWorld(), group, "auctions", -1);
                    if (Config.debug && max_group_meta >= 0) {
                        AuctionHouse.logger.info(String.format("Found group meta value auction = %d", max_group_meta));
                    }
                    if (max_group_meta < 0) continue;
                    max = Math.max(max, max_group_meta);
                }
            }
            int max_meta = AuctionHouse.chat.getPlayerInfoInteger(player, "auctions", -1);
            if (Config.debug && max_meta >= 0) {
                AuctionHouse.logger.info(String.format("Found player meta value auction = %d", max_meta));
            }
            if (max_meta >= 0) {
                max = Math.max(max, max_meta);
            }
        }
        if (Config.debug) {
            AuctionHouse.logger.info(String.format("Maximum auctions set to %d", max));
        }
        return max;
    }

    public static void updateCounts(Player player) {
        AuctionHouse.listings.updateCounts(player);
    }

    public static int getActiveListingsCount() {
        return activeListingsCount;
    }

    public static void setActiveListingsCount(int count) {
        activeListingsCount = count;
    }

    public static int getPlayerListingsCount(OfflinePlayer op) {
        UUID uuid = op.getUniqueId();
        return playerListingsCount.getOrDefault(uuid, 0);
    }

    public static void setPlayerListingsCount(UUID uuid, int count) {
        playerListingsCount.put(uuid, count);
    }

    public static int getExpiredListingsCount(OfflinePlayer op) {
        UUID uuid = op.getUniqueId();
        return expiredListingsCount.getOrDefault(uuid, 0);
    }

    public static void setExpiredListingsCount(UUID uuid, int count) {
        expiredListingsCount.put(uuid, count);
    }

    public static int getExpiredListingsCount() {
        return AuctionHouse.listings.getExpiredListings().count();
    }

    public static int getSoldItemsCount(OfflinePlayer op) {
        UUID uuid = op.getUniqueId();
        return soldItemsCount.getOrDefault(uuid, 0);
    }

    public static void setSoldItemsCount(UUID uuid, int count) {
        soldItemsCount.put(uuid, count);
    }

    public static OfflinePlayer getSeller(Long timestamp) {
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        return listing != null ? listing.getSeller() : null;
    }

    public static OfflinePlayer getBuyer(Long timestamp) {
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        return listing != null ? listing.getBuyer() : null;
    }

    public static float getPrice(Long timestamp) {
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        return listing != null ? listing.getPrice() : 0.0f;
    }

    public static float getReserve(Long timestamp) {
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        return listing != null ? listing.getReserve() : 0.0f;
    }

    public static float getBid(Long timestamp) {
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        return listing != null ? listing.getBid() : 0.0f;
    }

    public static ListingType getType(Long timestamp) {
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        return listing != null ? listing.getType() : ListingType.PLAYER_LISTING;
    }

    public static ItemStack getItem(Long timestamp) {
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        return listing != null ? listing.getItem() : null;
    }

    private static boolean canGiveItem(Player player, ItemStack item) {
        if (Config.drop_at_feet) {
            return true;
        }
        PlayerInventory pi = player.getInventory();
        if (pi.firstEmpty() != -1) {
            return true;
        }
        if (pi.containsAtLeast(item, 1)) {
            int free = 0;
            for (ItemStack i : pi.getStorageContents()) {
                if (!i.isSimilar(item)) continue;
                free += i.getMaxStackSize() - i.getAmount();
            }
            return free >= item.getAmount();
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     */
    public static boolean purchaseItem(Player player, Long timestamp) {
        if (player.getGameMode().equals((Object)GameMode.CREATIVE) && Config.auction_prevent_creative) {
            MessageUtil.sendMessage(player, "warning.purchase.creative", Config.locale);
            return false;
        }
        if (player.getGameMode().equals((Object)GameMode.SPECTATOR) && Config.auction_prevent_spectator) {
            MessageUtil.sendMessage(player, "warning.purchase.spectator", Config.locale);
            return false;
        }
        OfflinePlayer buyer = AuctionHouse.plugin.getServer().getOfflinePlayer(player.getUniqueId());
        String world = player.getWorld().getName();
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        if (listing == null) {
            MessageUtil.sendMessage(player, "warning.listing.expired", Config.locale);
            return false;
        }
        float price = listing.getPrice();
        OfflinePlayer seller = listing.getSeller();
        int amount = listing.getItem().getAmount();
        ListingType type = listing.getType();
        if (seller == null) {
            return false;
        }
        if (player.getUniqueId().equals(seller.getUniqueId()) && !type.isServer()) {
            MessageUtil.sendMessage(player, "warning.purchase.own_item", Config.locale);
            return false;
        }
        double balance = Economy.getBalance(buyer, world);
        if ((double)price > balance) {
            MessageUtil.sendMessage(player, "warning.purchase.insufficient_funds", Config.locale);
            return false;
        }
        if (!Auctions.canGiveItem(player, listing.getItem().clone())) {
            MessageUtil.sendMessage(player, "warning.player.no_inventory", Config.locale);
            return false;
        }
        PurchaseItemEvent event = new PurchaseItemEvent(listing.getWorld(), listing.getSeller_UUID(), buyer.getUniqueId().toString(), listing.getPrice(), listing.getItem());
        Bukkit.getPluginManager().callEvent((Event)event);
        if (event.isCancelled()) {
            return false;
        }
        double tax = (double)price * (Config.auction_sales_tax / 100.0);
        tax = Math.min(tax, Config.auction_max_sales_tax);
        double revenue = (double)price - tax;
        boolean withdrawSuccess = Economy.withdrawPlayer(buyer, world, price);
        if (!withdrawSuccess) {
            MessageUtil.sendMessage(player, "warning.purchase.problem", Config.locale);
            return false;
        }
        boolean depositSuccess = true;
        if (!type.isServer()) {
            depositSuccess = Economy.depositPlayer(seller, world, revenue);
        }
        if (!depositSuccess) {
            Economy.depositPlayer(buyer, world, price);
            MessageUtil.sendMessage(player, "warning.purchase.problem", Config.locale);
            return false;
        }
        if (AuctionHouse.listings.soldListing(timestamp, buyer)) {
            Player p;
            String nbt;
            ItemStack is = listing.getItem().clone();
            if (Config.replace_item_uuids && (nbt = AuctionHouse.nms.getNBTString(is)) != null) {
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("nbt = %s", nbt));
                }
                String seller_uuid = seller.getUniqueId().toString();
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("seller_uuid = %s", seller_uuid));
                }
                String buyer_uuid = buyer.getUniqueId().toString();
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("buyer_uuid = %s", buyer_uuid));
                }
                nbt = nbt.replace(seller_uuid, buyer_uuid);
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("nbt = %s", nbt));
                }
                is = AuctionHouse.nms.setNBTString(is, nbt);
            }
            if (Config.replace_player_names && is != null && (nbt = AuctionHouse.nms.getNBTString(is)) != null) {
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("nbt = %s", nbt));
                }
                String seller_name = seller.getName();
                String buyer_name = buyer.getName();
                if (seller_name != null && buyer_name != null) {
                    if (Config.debug) {
                        AuctionHouse.logger.info(String.format("seller_name = %s", seller_name));
                    }
                    if (Config.debug) {
                        AuctionHouse.logger.info(String.format("buyer_name = %s", buyer_name));
                    }
                    nbt = nbt.replace(seller_name, buyer_name);
                    if (Config.debug) {
                        AuctionHouse.logger.info(String.format("nbt = %s", nbt));
                    }
                    is = AuctionHouse.nms.setNBTString(is, nbt);
                }
            }
            if (is != null) {
                if (Config.drop_at_feet) {
                    player.getWorld().dropItem(player.getLocation(), is);
                } else {
                    HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(new ItemStack[]{is});
                    for (Integer index : remaining.keySet()) {
                        player.getWorld().dropItem(player.getLocation(), (ItemStack)remaining.get(index));
                    }
                }
            }
            Auctions.updateCounts(player);
            String item_name = MessageUtil.sectionSymbol(ItemUtil.getTranslatableName(is));
            MessageUtil.sendMessage(player, "message.purchase.success", Config.locale, amount, item_name, Float.valueOf(price));
            String buyer_name = buyer.getName();
            String seller_name = type.isServer() ? AuctionHouse.servername : seller.getName();
            double buyer_balance = Economy.getBalance(buyer, world);
            MessageUtil.sendMessage(player, "message.purchase.buyer_balance", Config.locale, buyer_balance);
            if (!type.isServer() && seller.isOnline() && (p = seller.getPlayer()) != null) {
                double seller_balance = Economy.getBalance(seller, world);
                SoundUtil.soldSound(p);
                MessageUtil.sendMessage(listing.getWorld(), p, "message.purchase.notify", Config.locale, buyer_name, amount, item_name);
                MessageUtil.sendMessage(listing.getWorld(), p, "message.purchase.earnings", Config.locale, revenue, revenue < (double)price ? LocaleStorage.translate("message.purchase.after_taxes", Config.locale) : "");
                MessageUtil.sendMessage(listing.getWorld(), p, "message.purchase.seller_balance", Config.locale, seller_balance);
                if (Config.announce_action_bar_purchases) {
                    long delay = 0L;
                    MessageUtil.sendActionBar(listing.getWorld(), p, delay, "message.purchase.notify", Config.locale, buyer_name, amount, item_name);
                    MessageUtil.sendActionBar(listing.getWorld(), p, delay += 20L, "message.purchase.notify", Config.locale, buyer_name, amount, item_name);
                    MessageUtil.clearActionBar(listing.getWorld(), p, delay += 40L);
                    MessageUtil.sendActionBar(listing.getWorld(), p, delay += 5L, "message.purchase.earnings", Config.locale, revenue, revenue < (double)price ? LocaleStorage.translate("message.purchase.after_taxes", Config.locale) : "");
                    MessageUtil.clearActionBar(listing.getWorld(), p, delay += 40L);
                    MessageUtil.sendActionBar(listing.getWorld(), p, delay += 5L, "message.purchase.seller_balance", Config.locale, seller_balance);
                }
            }
            for (Player p2 : AuctionHouse.plugin.getServer().getOnlinePlayers()) {
                if (p2 == player || seller.isOnline() && p2 == seller.getPlayer()) continue;
                if (Config.announce_chat_purchases) {
                    MessageUtil.sendMessage(listing.getWorld(), p2, "message.sell.player.item_sold", Config.locale, seller_name, amount, item_name, buyer_name, Float.valueOf(price));
                }
                if (!Config.announce_action_bar_purchases) continue;
                MessageUtil.sendActionBar(listing.getWorld(), p2, 0L, "message.sell.player.item_sold", Config.locale, seller_name, amount, item_name, buyer_name, Float.valueOf(price));
            }
            if (Config.announce_discord_purchases) {
                MessageUtil.discordMessage("discord.sell.player.item_sold", Config.locale, seller_name, amount, MessageUtil.nocolor(item_name), buyer_name, Float.valueOf(price));
            }
            AuctionItemEvent auctionItemEvent = new AuctionItemEvent(ItemAction.ITEM_SOLD, listing.getWorld(), listing.getSeller_UUID(), buyer.getUniqueId().toString(), listing.getBidder_UUID(), listing.getPrice(), listing.getReserve(), listing.getBid(), listing.getType(), listing.getItem());
            Bukkit.getPluginManager().callEvent((Event)auctionItemEvent);
            return true;
        }
        if (Config.debug) {
            AuctionHouse.logger.info("Warning! AuctionHouse could not get a timestamp");
        }
        Economy.depositPlayer(buyer, world, price);
        if (!type.isServer()) {
            Economy.withdrawPlayer(seller, world, price);
        }
        MessageUtil.sendMessage(player, "warning.purchase.problem", Config.locale);
        return false;
    }

    public static void clearSoldItems(Player player) {
        Listings soldItems = AuctionHouse.listings.getSoldItemsChrono(player, true);
        if (soldItems == null) {
            MessageUtil.sendMessage(player, "warning.sold_items.none", Config.locale);
            return;
        }
        AuctionHouse.listings.removeListings(soldItems.getListings().keySet());
        Auctions.updateCounts(player);
        MessageUtil.sendMessage(player, "warning.sold_items.cleared", Config.locale);
    }

    public static boolean expireItem(Player player, Long timestamp) {
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        if (listing != null && listing.getBuyer() == null) {
            OfflinePlayer seller = listing.getSeller();
            AuctionHouse.listings.cancelListing(timestamp);
            Auctions.updateCounts(player);
            AuctionItemEvent event = new AuctionItemEvent(ItemAction.ITEM_CANCELLED, listing.getWorld(), listing.getSeller_UUID(), listing.getBuyer_UUID(), listing.getBidder_UUID(), listing.getPrice(), listing.getReserve(), listing.getBid(), listing.getType(), listing.getItem());
            Bukkit.getPluginManager().callEvent((Event)event);
            return true;
        }
        MessageUtil.sendMessage(player, "warning.cancel.failed", Config.locale);
        return false;
    }

    public static boolean cancelItem(Player player, Long timestamp) {
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        if (listing != null && listing.getBuyer() == null && player.isOnline() && Auctions.returnItem(player, timestamp)) {
            AuctionItemEvent event = new AuctionItemEvent(ItemAction.ITEM_CANCELLED, listing.getWorld(), listing.getSeller_UUID(), listing.getBuyer_UUID(), listing.getBidder_UUID(), listing.getPrice(), listing.getReserve(), listing.getBid(), listing.getType(), listing.getItem());
            Bukkit.getPluginManager().callEvent((Event)event);
            return true;
        }
        MessageUtil.sendMessage(player, "warning.cancel.failed", Config.locale);
        return false;
    }

    public static void cancelAllItems(Player player) {
        Auctions.updateCounts(player);
        if (Auctions.getPlayerListingsCount((OfflinePlayer)player) == 0) {
            MessageUtil.sendMessage(player, "message.cancel_all.none", Config.locale);
            return;
        }
        boolean success = true;
        Listings playerListings = AuctionHouse.listings.getPlayerListingsChrono(player, true);
        Map<Long, Listing> active = playerListings.getListings();
        for (Long timestamp : active.keySet()) {
            Listing listing = active.get(timestamp);
            if (listing == null || listing.getBuyer() != null) continue;
            if (player.isOnline() && Auctions.returnItem(player, timestamp)) {
                AuctionItemEvent event = new AuctionItemEvent(ItemAction.ITEM_CANCELLED, listing.getWorld(), listing.getSeller_UUID(), listing.getBuyer_UUID(), listing.getBidder_UUID(), listing.getPrice(), listing.getReserve(), listing.getBid(), listing.getType(), listing.getItem());
                Bukkit.getPluginManager().callEvent((Event)event);
                continue;
            }
            success = false;
        }
        if (success) {
            MessageUtil.sendMessage(player, "message.cancel_all.success", Config.locale);
        } else {
            MessageUtil.sendMessage(player, "warning.cancel_all.failed", Config.locale);
        }
    }

    public static boolean returnItem(Player player, Long timestamp) {
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        if (listing != null && listing.getBuyer() == null) {
            ItemStack item = listing.getItem().clone();
            if (player.isOnline() && AuctionHouse.listings.removeListing(timestamp)) {
                Auctions.givePlayerItem(Objects.requireNonNull(player), item);
                Auctions.updateCounts(player);
                AuctionItemEvent event = new AuctionItemEvent(ItemAction.ITEM_RETURNED, listing.getWorld(), listing.getSeller_UUID(), listing.getBuyer_UUID(), listing.getBidder_UUID(), listing.getPrice(), listing.getReserve(), listing.getBid(), listing.getType(), listing.getItem());
                Bukkit.getPluginManager().callEvent((Event)event);
                return true;
            }
        }
        return false;
    }

    public static void returnAllItems(Player player) {
        Auctions.updateCounts(player);
        if (Auctions.getExpiredListingsCount((OfflinePlayer)player) == 0) {
            MessageUtil.sendMessage(player, "message.return_all.none", Config.locale);
            return;
        }
        boolean success = true;
        Listings expiredListings = AuctionHouse.listings.getExpiredListingsChrono(player, true);
        Map<Long, Listing> active = expiredListings.getListings();
        for (Long timestamp : active.keySet()) {
            Listing listing = active.get(timestamp);
            if (listing == null || listing.getBuyer() != null) continue;
            if (player.isOnline() && Auctions.returnItem(player, timestamp)) {
                AuctionItemEvent event = new AuctionItemEvent(ItemAction.ITEM_RETURNED, listing.getWorld(), listing.getSeller_UUID(), listing.getBuyer_UUID(), listing.getBidder_UUID(), listing.getPrice(), listing.getReserve(), listing.getBid(), listing.getType(), listing.getItem());
                Bukkit.getPluginManager().callEvent((Event)event);
                continue;
            }
            success = false;
        }
        if (success) {
            MessageUtil.sendMessage(player, "message.return_all.success", Config.locale);
        } else {
            MessageUtil.sendMessage(player, "warning.return_all.failed", Config.locale);
        }
    }

    private static void givePlayerItem(Player player, ItemStack drop) {
        if (Config.drop_at_feet) {
            player.getWorld().dropItem(player.getLocation(), drop);
        } else {
            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(new ItemStack[]{drop});
            for (Integer index : remaining.keySet()) {
                player.getWorld().dropItem(player.getLocation(), (ItemStack)remaining.get(index));
            }
        }
    }

    public static void checkExpiredItems(Player player) {
        int expired_count = Auctions.getExpiredListingsCount((OfflinePlayer)player);
        if (expired_count > 0) {
            MessageUtil.sendMessage(player, "message.expired_listings.notice1", Config.locale);
            MessageUtil.sendMessage(player, "message.expired_listings.notice2", Config.locale);
        }
    }

    public static List<Player> checkUnclaimedItems() {
        ArrayList<Player> players = new ArrayList<Player>();
        Listings unclaimedListings = AuctionHouse.listings.getUnclaimedListings();
        if (unclaimedListings.count() == 0) {
            return players;
        }
        Map<Long, Listing> map = unclaimedListings.getListings();
        for (Long timestamp : map.keySet()) {
            Player player;
            Listing listing = map.get(timestamp);
            OfflinePlayer op = listing.getSeller();
            if (!op.isOnline() || players.contains(player = op.getPlayer())) continue;
            players.add(player);
        }
        return players;
    }

    public static void returnUnclaimedItems(Player player) {
        Listings unclaimedListings = AuctionHouse.listings.getUnclaimedListings(player);
        if (unclaimedListings.count() == 0) {
            return;
        }
        Map<Long, Listing> map = unclaimedListings.getListings();
        boolean nospace = false;
        for (Long timestamp : map.keySet()) {
            Listing listing = map.get(timestamp);
            ItemStack item = listing.getItem();
            if (Auctions.canGiveItem(player, item)) {
                if (!AuctionHouse.listings.removeListing(timestamp)) continue;
                Auctions.givePlayerItem(player, item);
                AuctionItemEvent event = new AuctionItemEvent(ItemAction.ITEM_RETURNED, listing.getWorld(), listing.getSeller_UUID(), listing.getBuyer_UUID(), listing.getBidder_UUID(), listing.getPrice(), listing.getReserve(), listing.getBid(), listing.getType(), listing.getItem());
                Bukkit.getPluginManager().callEvent((Event)event);
                continue;
            }
            nospace = true;
        }
        if (nospace) {
            MessageUtil.sendMessage(player, "warning.player.no_inventory", Config.locale);
        }
        SoundUtil.dropSound(player);
        MessageUtil.sendMessage(player, "warning.unclaimed_listings.notice", Config.locale);
    }

    public static int deleteAbandonedItems() {
        int count = 0;
        Listings abandonedListings = AuctionHouse.listings.getAbandonedListings();
        if (abandonedListings.count() == 0) {
            return count;
        }
        Map<Long, Listing> map = abandonedListings.getListings();
        HashSet<Long> marked = new HashSet<Long>();
        for (Long timestamp : map.keySet()) {
            Listing listing = map.get(timestamp);
            if (listing == null) continue;
            ListingType type = listing.getType();
            ItemStack item = listing.getItem();
            if (item == null) continue;
            int amount = item.getAmount();
            String name = item.getType().getKey().getKey();
            String seller_uuid = listing.getSeller_UUID();
            OfflinePlayer op = listing.getSeller();
            String seller = type.isServer() ? AuctionHouse.servername : (op != null ? op.getName() : seller_uuid);
            if (Config.debug) {
                AuctionHouse.logger.info(String.format("Deleted %dx %s abandoned by %s", amount, name, seller));
            }
            marked.add(timestamp);
            ++count;
            AuctionItemEvent event = new AuctionItemEvent(ItemAction.ITEM_PURGED, listing.getWorld(), listing.getSeller_UUID(), listing.getBuyer_UUID(), listing.getBidder_UUID(), listing.getPrice(), listing.getReserve(), listing.getBid(), listing.getType(), listing.getItem());
            Bukkit.getPluginManager().callEvent((Event)event);
        }
        AuctionHouse.listings.removeListings(marked);
        return count;
    }

    public static int deleteExpiredSoldItems() {
        int count = 0;
        Listings allSoldItems = AuctionHouse.listings.getExpiredSoldItems();
        if (allSoldItems.count() == 0) {
            return count;
        }
        Map<Long, Listing> map = allSoldItems.getListings();
        HashSet<Long> marked = new HashSet<Long>();
        for (Long timestamp : map.keySet()) {
            Listing listing = map.get(timestamp);
            ListingType type = listing.getType();
            ItemStack item = listing.getItem();
            int amount = item.getAmount();
            String name = item.getType().getKey().getKey();
            String seller = type.isServer() ? AuctionHouse.servername : (listing.getSeller() != null ? listing.getSeller().getName() : listing.getSeller_UUID());
            if (Config.debug) {
                AuctionHouse.logger.info(String.format("Deleted expired sold item %dx %s by %s", amount, name, seller));
            }
            marked.add(timestamp);
            ++count;
        }
        AuctionHouse.listings.removeListings(marked);
        return count;
    }

    public static int purgeAllItems(OfflinePlayer seller) {
        int count = 0;
        Map<Long, Listing> map = AuctionHouse.listings.getListings();
        HashSet<Long> marked = new HashSet<Long>();
        for (Long timestamp : map.keySet()) {
            Listing listing = map.get(timestamp);
            if (listing.getSeller() != seller) continue;
            marked.add(timestamp);
            ++count;
        }
        AuctionHouse.listings.removeListings(marked);
        return count;
    }
}

