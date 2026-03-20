package com.spawnchunk.auctionhouse.menus;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.events.MenuClickEvent;
import com.spawnchunk.auctionhouse.events.ServerTickEvent;
import com.spawnchunk.auctionhouse.menus.ItemTag;
import com.spawnchunk.auctionhouse.menus.Menu;
import com.spawnchunk.auctionhouse.menus.MenuClickType;
import com.spawnchunk.auctionhouse.menus.MenuItem;
import com.spawnchunk.auctionhouse.modules.Auctions;
import com.spawnchunk.auctionhouse.modules.Listing;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListingsMenu
implements Listener {
    private final String id;
    private SortOrder sortOrder = Config.auction_sort_order;
    private int playerListingsCount;
    private int expiredListingsCount;
    private int soldItemsCount;
    private int current_page;
    private List<Long> page_keys = new ArrayList<Long>();
    private final Map<Integer, ItemTag> tags = new HashMap<Integer, ItemTag>();
    final String menu_back = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.back", Config.locale));
    final String menu_previous = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.page.previous", Config.locale));
    final String menu_next = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.page.next", Config.locale));
    final String menu_sort_listings = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.sort_listings", Config.locale));
    final String order_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.order.value", Config.locale));
    final String menu_expired_listings = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.button.title", Config.locale));
    final String expired_listings_desc1 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.button.desc1", Config.locale));
    final String expired_listings_desc2 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.button.desc2", Config.locale));
    final String returnable_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.returnable.value", Config.locale));
    final String returnable_none = MessageUtil.sectionSymbol(LocaleStorage.translate("warning.returnable.none", Config.locale));
    final String menu_sold_items = MessageUtil.sectionSymbol(LocaleStorage.translate("message.sold_items.button.title", Config.locale));
    final String sold_items_desc1 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.sold_items.button.desc1", Config.locale));
    final String sold_items_desc2 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.sold_items.button.desc2", Config.locale));
    final String sold_items_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.sold_items.value", Config.locale));
    final String sold_items_none = MessageUtil.sectionSymbol(LocaleStorage.translate("warning.sold_items.none", Config.locale));
    final String menu_info = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.info", Config.locale));
    final String info_desc1 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.info.desc1", Config.locale));
    final String info_desc2 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.info.desc2", Config.locale));
    final String info_desc3 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.info.desc3", Config.locale));
    final String info_desc4 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.info.desc4", Config.locale));
    final String info_desc5 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.info.desc5", Config.locale));
    final String player_listings_title = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.title", Config.locale));
    final String top_rule = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.horizontal.rule.top", Config.locale));
    final String click = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.click", Config.locale));
    final String top_spacing = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.spacing.top", Config.locale));
    final String price_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.price.value", Config.locale));
    final String expire_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.expire.value", Config.locale));
    final String item_expired = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.item_expired", Config.locale));
    final String key = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.expire.key", Config.locale));
    final String bottom_spacing = MessageUtil.sectionSymbol(LocaleStorage.translate("message.player_listings.spacing.bottom", Config.locale));
    final String bottom_rule = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.horizontal.rule.top", Config.locale));

    public PlayerListingsMenu(Player player) {
        UUID uuid = player.getUniqueId();
        String title = this.player_listings_title;
        if (title.isEmpty()) {
            title = "Your Current Listings";
        }
        this.id = AuctionHouse.menuManager.createMenu(uuid, title, 54);
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
        this.tags.put(45, ItemTag.BACK);
        this.tags.put(46, ItemTag.EXPIRED_LISTINGS);
        this.tags.put(48, ItemTag.PREVIOUS);
        this.tags.put(49, ItemTag.SORT_LISTINGS);
        this.tags.put(50, ItemTag.NEXT);
        this.tags.put(52, ItemTag.SOLD_ITEMS);
        MenuItem back = new MenuItem(this.menu_back, Config.back_button, 1, null);
        String order = LocaleStorage.translate(this.sortOrder.key(), Config.locale);
        MenuItem sort_listings = new MenuItem(this.menu_sort_listings, Config.sort_listings_button, 1, MessageUtil.expand(Collections.singletonList(MessageUtil.populate(this.order_value, order))));
        MenuItem expired_listings = new MenuItem(this.menu_expired_listings, Config.expired_listings_button, 1, MessageUtil.expand(Arrays.asList(this.expired_listings_desc1, this.expired_listings_desc2, MessageUtil.populate(this.returnable_value, 0))));
        MenuItem sold_items = new MenuItem(this.menu_sold_items, Config.sold_items_button, 1, MessageUtil.expand(Arrays.asList(this.sold_items_desc1, this.sold_items_desc2, MessageUtil.populate(this.sold_items_value, 0))));
        MenuItem info = new MenuItem(this.menu_info, Config.info_button, 1, MessageUtil.expand(Arrays.asList(this.info_desc1, this.info_desc2, this.info_desc3, this.info_desc4, this.info_desc5)));
        menu.setItem(45, back);
        menu.setItem(46, expired_listings);
        menu.setItem(49, sort_listings);
        menu.setItem(52, sold_items);
        menu.setItem(53, info);
    }

    public void show(final Player player, final int page) {
        this.initialize();
        new BukkitRunnable(){

            public void run() {
                PlayerListingsMenu.this.paginate_task(player, page);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public void paginate_task(final Player player, int page) {
        this.current_page = page;
        this.paginate(player);
        new BukkitRunnable(){

            public void run() {
                PlayerListingsMenu.this.build_task(player);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public void build_task(final Player player) {
        this.build(player, this.current_page);
        new BukkitRunnable(){

            public void run() {
                PlayerListingsMenu.this.open_task(player);
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
        Listings playerListings = AuctionHouse.listings.getPlayerListings(player, this.sortOrder);
        this.playerListingsCount = playerListings.count();
        Map<Long, Listing> map = playerListings.getListings();
        this.page_keys = new ArrayList<Long>(map.keySet());
        int keys = this.page_keys.size();
        return Math.floorDiv(Math.max(keys - 1, 0), 45);
    }

    private void build(Player player, int page) {
        Menu menu = AuctionHouse.menuManager.getMenu(this.id);
        if (menu == null) {
            return;
        }
        int size = this.page_keys.size();
        long now = TimeUtil.now();
        int pages = Math.floorDiv(Math.max(size - 1, 0), 45);
        MenuItem blank = new MenuItem(null, "minecraft:air", 1, null);
        MenuItem previous = new MenuItem(this.menu_previous, Config.previous_button, 1, null);
        MenuItem next = new MenuItem(this.menu_next, Config.next_button, 1, null);
        Listings playerListings = AuctionHouse.listings.getPlayerListings(player, this.sortOrder);
        int offset = page * 45;
        for (int i = 0; i < 45; ++i) {
            int index = offset + i;
            if (index < size) {
                ItemStack is;
                long timestamp = this.page_keys.get(index);
                long remaining = timestamp - now;
                Listing listing = playerListings.getListing(timestamp);
                if (listing == null || (is = listing.getItem()) == null) continue;
                String name = ItemUtil.getCustomName(is);
                String key = is.getType().getKey().toString();
                int amount = is.getAmount();
                ItemMeta meta = is.getItemMeta();
                List oldLore = meta != null ? meta.getLore() : new ArrayList();
                String nbt = AuctionHouse.nms.getNBTString(is);
                float price = listing.getPrice();
                List<String> desc = Arrays.asList(this.top_rule, this.click, this.top_spacing, MessageUtil.populate(this.price_value, Float.valueOf(price)), remaining > 0L ? MessageUtil.populate(this.expire_value, TimeUtil.duration(remaining, Config.show_seconds)) : this.item_expired, this.bottom_spacing, this.bottom_rule);
                ArrayList<String> lore = new ArrayList<String>();
                if (oldLore != null) {
                    lore.addAll(oldLore);
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
        menu.setItem(48, this.current_page > 0 ? previous : blank);
        menu.setItem(50, this.current_page < pages ? next : blank);
        OfflinePlayer op = AuctionHouse.plugin.getServer().getOfflinePlayer(player.getUniqueId());
        Auctions.updateCounts(player);
        int expired = Auctions.getExpiredListingsCount(op);
        this.updateExpiredListingsButton(menu, expired);
        int sold = Auctions.getSoldItemsCount(op);
        this.updateSoldItemsButton(menu, sold);
    }

    private void updateExpiredListingsButton(Menu menu, int expired) {
        this.expiredListingsCount = expired;
        List<String> lore = MessageUtil.expand(Arrays.asList(this.expired_listings_desc1, this.expired_listings_desc2, expired > 0 ? MessageUtil.populate(this.returnable_value, expired) : this.returnable_none));
        MenuItem expired_listings = new MenuItem(this.menu_expired_listings, Config.expired_listings_button, 1, lore);
        menu.setItem(46, expired_listings);
        menu.setItemAmount(46, 1);
    }

    private void updateSoldItemsButton(Menu menu, int sold) {
        this.soldItemsCount = sold;
        if (sold > 0) {
            List<String> lore = Arrays.asList(this.sold_items_desc1, this.sold_items_desc2, MessageUtil.populate(this.sold_items_value, sold));
            MenuItem sold_items = new MenuItem(this.menu_sold_items, Config.sold_items_button, 1, MessageUtil.expand(lore));
            menu.setItem(52, sold_items);
        } else {
            List<String> lore = Collections.singletonList(this.sold_items_none);
            menu.setItemLore(52, MessageUtil.expand(lore));
        }
        menu.setItemAmount(52, 1);
    }

    private void update(Player player) {
        int sold;
        Menu menu = AuctionHouse.menuManager.getMenu(this.id);
        if (menu == null) {
            return;
        }
        Auctions.updateCounts(player);
        OfflinePlayer op = AuctionHouse.plugin.getServer().getOfflinePlayer(player.getUniqueId());
        int count = Auctions.getPlayerListingsCount(op);
        if (this.playerListingsCount != count) {
            this.playerListingsCount = count;
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
            long remaining = timestamp - now;
            menu.replaceItemLore(i, this.key, remaining > 0L ? MessageUtil.populate(this.expire_value, TimeUtil.duration(remaining, Config.show_seconds)) : this.item_expired);
        }
        int expired = Auctions.getExpiredListingsCount(op);
        if (this.expiredListingsCount != expired) {
            this.updateExpiredListingsButton(menu, expired);
        }
        if (this.soldItemsCount != (sold = Auctions.getSoldItemsCount(op))) {
            this.updateSoldItemsButton(menu, sold);
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
            ItemTag tag;
            Menu menu = AuctionHouse.menuManager.getMenu(id);
            if (menu == null) {
                return;
            }
            int slot = event.getSlot();
            ItemStack itemStack = menu.getItem(slot);
            if (itemStack == null) {
                return;
            }
            if (itemStack.getType().equals((Object)Material.AIR)) {
                return;
            }
            MenuClickType click = event.getMenuClickType();
            if (PlayerUtil.spamCheck(player)) {
                return;
            }
            if (click == MenuClickType.LEFT && (tag = this.tags.get(slot)) != null) {
                if (tag.equals((Object)ItemTag.ITEM)) {
                    int index = this.current_page * 45 + slot;
                    int size = this.page_keys.size();
                    if (index < size && size > 0) {
                        SoundUtil.clickSound(player);
                        long timestamp = this.page_keys.get(index);
                        OfflinePlayer buyer = Auctions.getBuyer(timestamp);
                        if (buyer != null) {
                            MessageUtil.sendMessage(player, "warning.listing.expired", Config.locale);
                            SoundUtil.failSound(player);
                        } else {
                            String name = MessageUtil.sectionSymbol(ItemUtil.getTranslatableName(itemStack));
                            int count = itemStack.getAmount();
                            if (Auctions.cancelItem(player, timestamp)) {
                                this.update(player);
                                MessageUtil.sendMessage(player, "message.cancel.success", Config.locale, player.getName(), count, name);
                            }
                        }
                        this.paginate(player);
                        int player_listing_count = Auctions.getPlayerListingsCount((OfflinePlayer)player);
                        int pages = Math.floorDiv(Math.max(player_listing_count - 1, 0), 45);
                        while (this.current_page > pages) {
                            --this.current_page;
                        }
                        this.build(player, this.current_page);
                    } else {
                        SoundUtil.failSound(player);
                    }
                    return;
                }
                if (tag.equals((Object)ItemTag.BACK)) {
                    SoundUtil.clickSound(player);
                    Auctions.openActiveListingsMenu(player);
                    return;
                }
                if (tag.equals((Object)ItemTag.EXPIRED_LISTINGS)) {
                    SoundUtil.clickSound(player);
                    Auctions.openExpiredListingsMenu(player);
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
                    return;
                }
                if (tag.equals((Object)ItemTag.SOLD_ITEMS)) {
                    SoundUtil.clickSound(player);
                    Auctions.openSoldItemsMenu(player);
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onServerTick(ServerTickEvent event) {
        Menu menu = AuctionHouse.menuManager.getMenu(this.id);
        if (menu == null) {
            return;
        }
        List<HumanEntity> viewers = menu.getViewers();
        for (HumanEntity entity : viewers) {
            if (!(entity instanceof Player)) continue;
            Player player = (Player)entity;
            this.update(player);
        }
    }
}

