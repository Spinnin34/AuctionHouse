package com.spawnchunk.auctionhouse.menus;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.events.MenuClickEvent;
import com.spawnchunk.auctionhouse.events.PrePurchaseItemEvent;
import com.spawnchunk.auctionhouse.events.ServerTickEvent;
import com.spawnchunk.auctionhouse.menus.ItemTag;
import com.spawnchunk.auctionhouse.menus.Menu;
import com.spawnchunk.auctionhouse.menus.MenuClickType;
import com.spawnchunk.auctionhouse.menus.MenuItem;
import com.spawnchunk.auctionhouse.modules.Auctions;
import com.spawnchunk.auctionhouse.modules.Listing;
import com.spawnchunk.auctionhouse.modules.ListingType;
import com.spawnchunk.auctionhouse.modules.Listings;
import com.spawnchunk.auctionhouse.modules.SortOrder;
import com.spawnchunk.auctionhouse.storage.LocaleStorage;
import com.spawnchunk.auctionhouse.util.ItemUtil;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import com.spawnchunk.auctionhouse.util.PlayerUtil;
import com.spawnchunk.auctionhouse.util.SoundUtil;
import com.spawnchunk.auctionhouse.util.TimeUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ActiveListingsMenu
implements Listener {
    private final String id;
    private final Menu menu;
    private SortOrder sortOrder = Config.auction_sort_order;
    private int activeListingsCount;
    private int playerListingsCount;
    private int soldItemsCount;
    private int current_page;
    private String filter;
    private boolean menu_mode;
    private List<Long> page_keys = new ArrayList<Long>();
    private final Map<Integer, ItemTag> tags = new HashMap<Integer, ItemTag>();
    private double playerBalance = 0.0;
    final String active_listings_title = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.title", Config.locale));
    final String menu_exit = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.exit", Config.locale));
    final String menu_previous = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.page.previous", Config.locale));
    final String menu_next = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.page.next", Config.locale));
    final String menu_info = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.info", Config.locale));
    final String menu_howto_sell = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.howto_sell", Config.locale));
    final String menu_sort_listings = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.sort_listings", Config.locale));
    final String menu_player_listings = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.button.title", Config.locale));
    final String player_listings_desc1 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.button.desc1", Config.locale));
    final String player_listings_desc2 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.button.desc2", Config.locale));
    final String player_listings_none = MessageUtil.sectionSymbol(LocaleStorage.translate("warning.player_listings.none", Config.locale));
    final String listings_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.listings.value", Config.locale));
    final String balance_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.balance.value", Config.locale));
    final String order_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.order.value", Config.locale));
    final String howto_desc1 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.howto.desc1", Config.locale));
    final String howto_desc2 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.howto.desc2", Config.locale));
    final String info_desc1 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.info.desc1", Config.locale));
    final String info_desc2 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.info.desc2", Config.locale));
    final String info_desc3 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.info.desc3", Config.locale));
    final String info_desc4 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.info.desc4", Config.locale));
    final String info_desc5 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.info.desc5", Config.locale));
    final String info_desc6 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.info.desc6", Config.locale));
    final String top_rule = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.horizontal.rule.top", Config.locale));
    final String click = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.click", Config.locale));
    final String unavailable = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.unavailable", Config.locale));
    final String unaffordable = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.unaffordable", Config.locale));
    final String top_spacing = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.spacing.top", Config.locale));
    final String price_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.price.value", Config.locale));
    final String seller_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.seller.value", Config.locale));
    final String expire_key = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.expire.key", Config.locale));
    final String expire_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.expire.value", Config.locale));
    final String item_expired = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.item_expired", Config.locale));
    final String bottom_spacing = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.spacing.bottom", Config.locale));
    final String shift_left_click = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.shift_left_click", Config.locale));
    final String shift_right_click = MessageUtil.sectionSymbol(LocaleStorage.translate("message.active_listings.shift_right_click", Config.locale));
    final String repair_cost = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.repair_cost", Config.locale));
    final String bottom_rule = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.horizontal.rule.top", Config.locale));

    public ActiveListingsMenu(Player player, boolean menu_mode) {
        this.menu_mode = menu_mode;
        UUID uuid = player.getUniqueId();
        String title = MessageUtil.sectionSymbol(this.active_listings_title);
        if (title.isEmpty()) {
            title = "Auction House";
        }
        this.id = AuctionHouse.menuManager.createMenu(uuid, title, 54);
        this.menu = AuctionHouse.menuManager.getMenu(this.id);
        Bukkit.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)AuctionHouse.plugin);
    }

    public void initialize() {
        Menu menu = AuctionHouse.menuManager.getMenu(this.id);
        if (menu == null) {
            return;
        }
        this.sortOrder = Config.auction_sort_order;
        for (int slot = 0; slot < 45; ++slot) {
            this.tags.put(slot, ItemTag.ITEM);
        }
        if (this.menu_mode) {
            this.tags.put(45, ItemTag.EXIT);
            this.tags.put(46, ItemTag.PLAYER_LISTINGS);
        } else {
            this.tags.put(45, ItemTag.PLAYER_LISTINGS);
            this.tags.remove(46);
        }
        this.tags.put(48, ItemTag.PREVIOUS);
        this.tags.put(49, ItemTag.SORT_LISTINGS);
        this.tags.put(50, ItemTag.NEXT);
        MenuItem blank = new MenuItem(null, "minecraft:air", 1, null);
        MenuItem exit = new MenuItem(this.menu_exit, Config.exit_button, 1, null);
        MenuItem player_listings = new MenuItem(this.menu_player_listings, Config.player_listings_button, 1, MessageUtil.expand(Arrays.asList(this.player_listings_desc1, this.player_listings_desc2, MessageUtil.populate(this.listings_value, 0), MessageUtil.populate(this.balance_value, 0))));
        String order = LocaleStorage.translate(this.sortOrder.key(), Config.locale);
        MenuItem sort_listings = new MenuItem(this.menu_sort_listings, Config.sort_listings_button, 1, MessageUtil.expand(Collections.singletonList(MessageUtil.populate(this.order_value, order))));
        MenuItem howto = new MenuItem(this.menu_howto_sell, Config.howto_button, 1, MessageUtil.expand(Arrays.asList(this.howto_desc1, this.howto_desc2)));
        MenuItem info = new MenuItem(this.menu_info, Config.info_button, 1, MessageUtil.expand(Arrays.asList(this.info_desc1, this.info_desc2, this.info_desc3, this.info_desc4, this.info_desc5, this.info_desc6)));
        if (this.menu_mode) {
            menu.setItem(45, exit);
            menu.setItem(46, player_listings);
        } else {
            menu.setItem(45, player_listings);
            menu.setItem(46, blank);
        }
        menu.setItem(49, sort_listings);
        menu.setItem(52, howto);
        menu.setItem(53, info);
    }

    public void show(final Player player, boolean menu_mode, String filter, final int page) {
        this.filter = filter;
        this.menu_mode = menu_mode;
        this.initialize();
        new BukkitRunnable(){

            public void run() {
                ActiveListingsMenu.this.paginate_task(player, page);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public void paginate_task(final Player player, int page) {
        this.current_page = page;
        this.paginate(player);
        new BukkitRunnable(){

            public void run() {
                ActiveListingsMenu.this.build_task(player);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public void build_task(final Player player) {
        this.build(player, this.current_page);
        new BukkitRunnable(){

            public void run() {
                ActiveListingsMenu.this.open_task(player);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public void open_task(Player player) {
        UUID uuid = player.getUniqueId();
        AuctionHouse.menuManager.openMenu(uuid, this.id);
    }

    public void close(Player player) {
        UUID uuid = player.getUniqueId();
        AuctionHouse.menuManager.closeMenu(uuid, this.id);
    }

    private int paginate(Player player) {
        Listings filteredListings = this.filter == null ? AuctionHouse.listings.getActiveListings(player, this.sortOrder) : AuctionHouse.listings.getFilteredListings(player, this.filter, this.sortOrder);
        this.activeListingsCount = filteredListings.count();
        Map<Long, Listing> map = filteredListings.getListings();
        this.page_keys = new ArrayList<Long>(map.keySet());
        int keys = this.page_keys.size();
        return Math.floorDiv(Math.max(keys - 1, 0), 45);
    }

    private void build(Player player, int page) {
        Menu menu = AuctionHouse.menuManager.getMenu(this.id);
        String playerName = player.getName();
        this.playerBalance = PlayerUtil.getPlayerBalance(player, player.getWorld().getName());
        if (menu == null) {
            return;
        }
        int size = this.page_keys.size();
        long now = TimeUtil.now();
        int pages = Math.floorDiv(Math.max(size - 1, 0), 45);
        if (this.menu_mode) {
            this.tags.put(45, ItemTag.EXIT);
            this.tags.put(46, ItemTag.PLAYER_LISTINGS);
        } else {
            this.tags.put(45, ItemTag.PLAYER_LISTINGS);
            this.tags.remove(46);
        }
        MenuItem exit = new MenuItem(this.menu_exit, Config.exit_button, 1, null);
        MenuItem player_listings = new MenuItem(this.menu_player_listings, Config.player_listings_button, 1, MessageUtil.expand(Arrays.asList(this.player_listings_desc1, this.player_listings_desc2, MessageUtil.populate(this.listings_value, 0), MessageUtil.populate(this.balance_value, 0))));
        MenuItem blank = new MenuItem(null, "minecraft:air", 1, null);
        MenuItem previous = new MenuItem(this.menu_previous, Config.previous_button, 1, null);
        MenuItem next = new MenuItem(this.menu_next, Config.next_button, 1, null);
        Listings filteredListings = this.filter == null ? AuctionHouse.listings.getActiveListings(player, this.sortOrder) : AuctionHouse.listings.getFilteredListings(player, this.filter, this.sortOrder);
        int offset = page * 45;
        for (int i = 0; i < 45; ++i) {
            int index = offset + i;
            if (index < size) {
                int rc;
                ItemStack is;
                long timestamp = this.page_keys.get(index);
                boolean expires = timestamp < Config.auction_future_duration;
                long remaining = timestamp - now;
                Listing listing = filteredListings.getListing(timestamp);
                if (listing == null || (is = listing.getItem()) == null) continue;
                String name = ItemUtil.getCustomName(is);
                Material material = is.getType();
                String key = material.getKey().toString();
                int amount = is.getAmount();
                ItemMeta meta = is.getItemMeta();
                LinkedList<String> lore = new LinkedList<String>();
                if (meta != null) {
                    if (meta.hasLore() && meta.getLore() != null) {
                        lore.addAll(meta.getLore());
                    }
                    if (AuctionHouse.nms.isContainer(is)) {
                        Map<Integer, ItemStack> items = AuctionHouse.nms.getContainerItems(is);
                        HashMap<ItemStack, Integer> item_counts = new HashMap<ItemStack, Integer>();
                        for (Integer slot : items.keySet()) {
                            ItemStack itemStack = items.get(slot);
                            int count = itemStack.getAmount();
                            itemStack.setAmount(0);
                            if (item_counts.containsKey(itemStack)) {
                                item_counts.put(itemStack, item_counts.getOrDefault(itemStack, 0) + count);
                                continue;
                            }
                            item_counts.put(itemStack, count);
                        }
                        for (ItemStack itemStack : item_counts.keySet()) {
                            String item_name = ItemUtil.getItemName(itemStack);
                            Integer item_count = (Integer)item_counts.get(itemStack);
                            lore.addFirst(String.format("\u00a7e%d\u00a77x %s", item_count, item_name));
                        }
                    } else if (material == Material.SPAWNER && Config.spawner_info) {
                        List<String> mobs = AuctionHouse.nms.getMobs(is);
                        ArrayList<String> mob_lore = new ArrayList<String>();
                        if (!mobs.isEmpty()) {
                            for (String mob : mobs) {
                                mob_lore.add(String.format("\u00a7r\u00a79%s", MessageUtil.readable(mob.replace("minecraft:", ""))));
                            }
                        }
                        lore.addAll(0, mob_lore);
                    }
                }
                String nbt = AuctionHouse.nms.getNBTString(is);
                double price = listing.getPrice();
                ListingType type = listing.getType();
                String seller = type.isServer() ? AuctionHouse.servername : (listing.getSeller() != null ? listing.getSellerName() : listing.getSeller_UUID());
                ArrayList<String> desc = new ArrayList<String>();
                desc.add(this.top_rule);
                if (seller != null && !seller.equals(playerName)) {
                    if (this.playerBalance >= price) {
                        desc.add(this.click);
                    } else {
                        desc.add(this.unaffordable);
                    }
                } else {
                    desc.add(this.unavailable);
                }
                desc.add(this.top_spacing);
                desc.add(MessageUtil.populate(this.price_value, price));
                desc.add(MessageUtil.populate(this.seller_value, seller));
                if (expires) {
                    desc.add(remaining > 0L ? MessageUtil.populate(this.expire_value, TimeUtil.duration(remaining, Config.show_seconds)) : this.item_expired);
                }
                desc.add(this.bottom_spacing);
                if (player.hasPermission("auctionhouse.cancel.others") || player.isOp()) {
                    desc.add(this.shift_left_click);
                }
                if ((player.hasPermission("auctionhouse.expire.others") || player.isOp()) && !type.isServer()) {
                    desc.add(this.shift_right_click);
                }
                desc.add(this.bottom_rule);
                if (Config.show_repair_cost && (rc = ItemUtil.getRepairCost(is)) >= 0) {
                    desc.add(MessageUtil.populate(this.repair_cost, rc));
                }
                for (String s : MessageUtil.expand(desc)) {
                    if (s.isEmpty()) continue;
                    lore.add(s);
                }
                MenuItem menuItem = new MenuItem(name, key, amount, lore, nbt);
                menu.setItem(i, menuItem);
                continue;
            }
            menu.setItem(i, blank);
        }
        if (this.menu_mode) {
            menu.setItem(45, exit);
            menu.setItem(46, player_listings);
        } else {
            menu.setItem(45, player_listings);
            menu.setItem(46, blank);
        }
        menu.setItem(48, this.current_page > 0 ? previous : blank);
        menu.setItem(50, this.current_page < pages ? next : blank);
        Auctions.updateCounts(player);
        int listed = Auctions.getPlayerListingsCount((OfflinePlayer)player);
        this.updatePlayerListingsButton(menu, player, listed, this.playerBalance);
    }

    private void updatePlayerListingsButton(Menu menu, Player player, int listed, double playerBalance) {
        this.playerListingsCount = listed;
        List<String> lore = Arrays.asList(this.player_listings_desc1, this.player_listings_desc2, listed > 0 ? MessageUtil.populate(this.listings_value, listed) : this.player_listings_none, MessageUtil.populate(this.balance_value, playerBalance));
        MenuItem player_listings = new MenuItem(player, this.menu_player_listings, Config.player_listings_button, 1, MessageUtil.expand(lore));
        menu.setItem(this.menu_mode ? 46 : 45, player_listings);
        menu.setItemAmount(this.menu_mode ? 46 : 45, 1);
    }

    private void update(Player player) {
        if (this.menu == null) {
            return;
        }
        String playerName = player.getName();
        double balance = PlayerUtil.getPlayerBalance(player, player.getWorld().getName());
        Auctions.updateCounts(player);
        int count = Auctions.getActiveListingsCount();
        int listed = Auctions.getPlayerListingsCount((OfflinePlayer)player);
        int sold = Auctions.getSoldItemsCount((OfflinePlayer)player);
        if (this.activeListingsCount != count) {
            this.activeListingsCount = count;
            int pages = this.paginate(player);
            if (this.current_page > pages) {
                this.current_page = pages;
            }
            this.build(player, this.current_page);
        }
        int size = this.page_keys.size();
        long now = TimeUtil.now();
        int offset = this.current_page * 45;
        for (int i = 0; i < 54; ++i) {
            int index;
            ItemTag tag = this.tags.get(i);
            if (tag == null || !tag.equals((Object)ItemTag.ITEM) || (index = offset + i) >= size) continue;
            long timestamp = this.page_keys.get(index);
            boolean expires = timestamp < Config.auction_future_duration;
            long remaining = timestamp - now;
            Listing listing = AuctionHouse.listings.getListing(timestamp);
            if (listing == null) continue;
            ListingType type = listing.getType();
            OfflinePlayer seller = listing.getSeller();
            String sellerName = type.isServer() ? AuctionHouse.servername : (seller != null ? seller.getName() : listing.getSeller_UUID());
            float price = listing.getPrice();
            if (seller != null && sellerName != null && !sellerName.equals(playerName)) {
                if (balance >= (double)price) {
                    this.menu.replaceItemLore(i, this.unaffordable, this.click);
                } else {
                    this.menu.replaceItemLore(i, this.click, this.unaffordable);
                }
            }
            if (!expires) continue;
            this.menu.replaceItemLore(i, this.expire_key, remaining > 0L ? MessageUtil.populate(this.expire_value, TimeUtil.duration(remaining, Config.show_seconds)) : this.item_expired);
        }
        if (this.playerListingsCount != listed || this.soldItemsCount != sold || this.playerBalance != balance) {
            this.playerListingsCount = listed;
            this.soldItemsCount = sold;
            this.playerBalance = balance;
            this.updatePlayerListingsButton(this.menu, player, listed, this.playerBalance);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onMenuClick(MenuClickEvent event) {
        Player player = event.getPlayer();
        String id = event.getId();
        if (player == null || id == null) {
            return;
        }
        if (id.equals(this.id)) {
            OfflinePlayer seller;
            int count;
            String name;
            OfflinePlayer buyer;
            ListingType type;
            ItemStack is;
            Listing listing;
            long timestamp;
            int size;
            int index;
            ItemTag tag;
            Menu menu = AuctionHouse.menuManager.getMenu(id);
            if (menu == null) {
                return;
            }
            int slot = event.getSlot();
            ItemStack item = menu.getItem(slot);
            if (item == null) {
                return;
            }
            if (item.getType().equals((Object)Material.AIR)) {
                return;
            }
            MenuClickType click = event.getMenuClickType();
            if (PlayerUtil.spamCheck(player)) {
                return;
            }
            if (click == MenuClickType.SHIFT_LEFT && (player.hasPermission("auctionhouse.cancel.others") || player.isOp()) && (tag = this.tags.get(slot)) != null && tag.equals((Object)ItemTag.ITEM)) {
                index = this.current_page * 45 + slot;
                size = this.page_keys.size();
                if (index < size && size > 0) {
                    SoundUtil.clickSound(player);
                    timestamp = this.page_keys.get(index);
                    listing = AuctionHouse.listings.getListing(timestamp);
                    is = Auctions.getItem(timestamp);
                    type = listing.getType();
                    if (is != null) {
                        buyer = Auctions.getBuyer(timestamp);
                        if (buyer != null) {
                            MessageUtil.sendMessage(player, "warning.listing.expired", Config.locale);
                            SoundUtil.failSound(player);
                        } else {
                            name = MessageUtil.sectionSymbol(ItemUtil.getTranslatableName(is));
                            count = is.getAmount();
                            seller = Auctions.getSeller(timestamp);
                            if (seller != null) {
                                if (Auctions.cancelItem(player, timestamp)) {
                                    this.update(player);
                                    if (player == seller) {
                                        MessageUtil.sendMessage(player, "message.cancel.success", Config.locale, player.getName(), count, name);
                                    } else {
                                        MessageUtil.sendMessage(player, "message.cancel.success.other", Config.locale, seller.getName(), count, name);
                                        if (seller.isOnline()) {
                                            MessageUtil.sendMessage(seller.getPlayer(), "message.cancel.admin", Config.locale, player.getName(), count, name);
                                        }
                                    }
                                } else {
                                    SoundUtil.failSound(player);
                                }
                            } else if (Auctions.cancelItem(player, timestamp)) {
                                this.update(player);
                                MessageUtil.sendMessage(player, "message.cancel.success", Config.locale, AuctionHouse.servername, count, name);
                            } else {
                                SoundUtil.failSound(player);
                            }
                        }
                    } else {
                        SoundUtil.failSound(player);
                    }
                } else {
                    SoundUtil.failSound(player);
                }
            }
            if (click == MenuClickType.SHIFT_RIGHT && (player.hasPermission("auctionhouse.expire.others") || player.isOp()) && (tag = this.tags.get(slot)) != null && tag.equals((Object)ItemTag.ITEM)) {
                index = this.current_page * 45 + slot;
                size = this.page_keys.size();
                if (index < size && size > 0) {
                    SoundUtil.clickSound(player);
                    timestamp = this.page_keys.get(index);
                    listing = AuctionHouse.listings.getListing(timestamp);
                    is = Auctions.getItem(timestamp);
                    type = listing.getType();
                    if (!type.isServer()) {
                        if (is != null) {
                            buyer = Auctions.getBuyer(timestamp);
                            if (buyer != null) {
                                MessageUtil.sendMessage(player, "warning.listing.expired", Config.locale);
                                SoundUtil.failSound(player);
                            } else {
                                name = MessageUtil.sectionSymbol(ItemUtil.getTranslatableName(is));
                                count = is.getAmount();
                                seller = Auctions.getSeller(timestamp);
                                if (seller != null) {
                                    if (Auctions.expireItem(player, timestamp)) {
                                        this.update(player);
                                        if (player == seller) {
                                            MessageUtil.sendMessage(player, "message.cancel.success", Config.locale, player.getName(), count, name);
                                        } else {
                                            MessageUtil.sendMessage(player, "message.cancel.success.other", Config.locale, seller.getName(), count, name);
                                            if (seller.isOnline()) {
                                                MessageUtil.sendMessage(seller.getPlayer(), "message.cancel.admin", Config.locale, player.getName(), count, name);
                                            }
                                        }
                                    } else {
                                        SoundUtil.failSound(player);
                                    }
                                } else {
                                    SoundUtil.failSound(player);
                                }
                            }
                        }
                    } else {
                        SoundUtil.failSound(player);
                    }
                } else {
                    SoundUtil.failSound(player);
                }
            }
            if (click == MenuClickType.LEFT && (tag = this.tags.get(slot)) != null) {
                if (tag.equals((Object)ItemTag.ITEM)) {
                    SoundUtil.clickSound(player);
                    index = this.current_page * 45 + slot;
                    if (index < this.page_keys.size() && this.page_keys.size() > 0) {
                        long timestamp2 = this.page_keys.get(index);
                        Listing listing2 = AuctionHouse.listings.getListing(timestamp2);
                        if (listing2 != null) {
                            ListingType type2 = listing2.getType();
                            OfflinePlayer buyer2 = Auctions.getBuyer(timestamp2);
                            if (buyer2 != null) {
                                MessageUtil.sendMessage(player, "warning.listing.expired", Config.locale);
                                SoundUtil.failSound(player);
                            } else {
                                OfflinePlayer seller2 = Auctions.getSeller(timestamp2);
                                String sellerName = type2.isServer() ? AuctionHouse.servername : (seller2 != null ? seller2.getName() : listing2.getSeller_UUID());
                                String playerName = player.getName();
                                if (seller2 != null && sellerName != null && (!seller2.getUniqueId().equals(player.getUniqueId()) || type2.isServer())) {
                                    this.playerBalance = PlayerUtil.getPlayerBalance(player, player.getWorld().getName());
                                    double price = Auctions.getPrice(timestamp2);
                                    if (this.playerBalance >= price) {
                                        if (seller2.hasPlayedBefore() || seller2.isOnline()) {
                                            PrePurchaseItemEvent prePurchaseItemEvent = new PrePurchaseItemEvent(player.getWorld().getName(), seller2.getUniqueId().toString(), player.getUniqueId().toString(), Auctions.getPrice(timestamp2), Auctions.getItem(timestamp2));
                                            Bukkit.getPluginManager().callEvent((Event)prePurchaseItemEvent);
                                            if (prePurchaseItemEvent.isCancelled()) {
                                                SoundUtil.failSound(player);
                                            } else {
                                                Auctions.openPurchaseItemMenu(player, timestamp2, this.current_page);
                                            }
                                        } else {
                                            SoundUtil.failSound(player);
                                        }
                                    } else {
                                        MessageUtil.sendMessage(player, "warning.purchase.insufficient_funds", Config.locale);
                                        SoundUtil.failSound(player);
                                    }
                                } else {
                                    MessageUtil.sendMessage(player, "warning.purchase.own_item", Config.locale);
                                    SoundUtil.failSound(player);
                                }
                            }
                        } else {
                            MessageUtil.sendMessage(player, "warning.listing.expired", Config.locale);
                            SoundUtil.failSound(player);
                        }
                    } else {
                        SoundUtil.failSound(player);
                    }
                    return;
                }
                if (tag.equals((Object)ItemTag.EXIT)) {
                    SoundUtil.clickSound(player);
                    this.close(player);
                    this.exit(player);
                    return;
                }
                if (tag.equals((Object)ItemTag.PLAYER_LISTINGS)) {
                    SoundUtil.clickSound(player);
                    Auctions.openPlayerListingsMenu(player);
                    return;
                }
                if (tag.equals((Object)ItemTag.PREVIOUS)) {
                    SoundUtil.clickSound(player);
                    this.paginate(player);
                    if (this.current_page > 0) {
                        --this.current_page;
                    }
                    this.build(player, this.current_page);
                    return;
                }
                if (tag.equals((Object)ItemTag.SORT_LISTINGS)) {
                    SoundUtil.clickSound(player);
                    this.sortOrder = this.sortOrder.next();
                    String order = LocaleStorage.translate(this.sortOrder.key(), Config.locale);
                    List<String> lore = Collections.singletonList(MessageUtil.populate(this.order_value, order));
                    menu.setItemLore(49, MessageUtil.expand(lore));
                    this.paginate(player);
                    this.build(player, this.current_page);
                    return;
                }
                if (tag.equals((Object)ItemTag.NEXT)) {
                    SoundUtil.clickSound(player);
                    int pages = this.paginate(player);
                    if (this.current_page < pages) {
                        ++this.current_page;
                    }
                    this.build(player, this.current_page);
                }
            }
        }
    }

    private void exit(Player player) {
        String command;
        if (Config.exit_command != null && !Config.exit_command.isEmpty() && !(command = Config.exit_command.replaceAll("%player%", player.getName()).trim()).isEmpty()) {
            Server server = player.getServer();
            ConsoleCommandSender console = server.getConsoleSender();
            server.dispatchCommand((CommandSender)console, command);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onServerTick(ServerTickEvent event) {
        if (this.menu == null) {
            return;
        }
        for (HumanEntity entity : this.menu.getViewers()) {
            if (!(entity instanceof Player)) continue;
            Player player = (Player)entity;
            this.update(player);
        }
    }
}

