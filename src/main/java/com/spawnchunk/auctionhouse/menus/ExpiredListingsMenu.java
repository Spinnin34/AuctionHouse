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

public class ExpiredListingsMenu
implements Listener {
    private final String id;
    private SortOrder sortOrder = Config.auction_sort_order;
    private int expiredListingsCount;
    private int current_page;
    private List<Long> page_keys = new ArrayList<Long>();
    private final Map<Integer, ItemTag> tags = new HashMap<Integer, ItemTag>();
    final String menu_back = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.back", Config.locale));
    final String menu_previous = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.page.previous", Config.locale));
    final String menu_next = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.page.next", Config.locale));
    final String menu_sort_listings = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.sort_listings", Config.locale));
    final String order_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.order.value", Config.locale));
    final String menu_return_all = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.return_all", Config.locale));
    final String return_none = MessageUtil.sectionSymbol(LocaleStorage.translate("warning.expired_listings.return.none", Config.locale));
    final String return_desc1 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.return.desc1", Config.locale));
    final String return_desc2 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.return.desc2", Config.locale));
    final String info_desc1 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.info.desc1", Config.locale));
    final String info_desc2 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.info.desc2", Config.locale));
    final String info_desc3 = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.info.desc3", Config.locale));
    final String expired_listings_title = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.title", Config.locale));
    final String top_rule = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.horizontal.rule.top", Config.locale));
    final String top_spacing = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.spacing.top", Config.locale));
    final String click = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.click", Config.locale));
    final String bottom_spacing = MessageUtil.sectionSymbol(LocaleStorage.translate("message.expired_listings.spacing.bottom", Config.locale));
    final String bottom_rule = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.horizontal.rule.top", Config.locale));

    public ExpiredListingsMenu(Player player) {
        UUID uuid = player.getUniqueId();
        String title = this.expired_listings_title;
        if (title.isEmpty()) {
            title = "Cancelled / Expired Listings";
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
        this.tags.put(48, ItemTag.PREVIOUS);
        this.tags.put(49, ItemTag.SORT_LISTINGS);
        this.tags.put(50, ItemTag.NEXT);
        this.tags.put(52, ItemTag.RETURN_ALL);
        MenuItem back = new MenuItem(this.menu_back, Config.back_button, 1, null);
        String order = LocaleStorage.translate(this.sortOrder.key(), Config.locale);
        MenuItem sort_listings = new MenuItem(this.menu_sort_listings, Config.sort_listings_button, 1, MessageUtil.expand(Collections.singletonList(MessageUtil.populate(this.order_value, order))));
        MenuItem return_all = new MenuItem(this.menu_return_all, Config.return_all_button, 1, MessageUtil.expand(Arrays.asList(this.return_desc1, this.return_desc2)));
        MenuItem info = new MenuItem(MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.info", Config.locale)), Config.info_button, 1, MessageUtil.expand(Arrays.asList(this.info_desc1, this.info_desc2, this.info_desc3)));
        menu.setItem(45, back);
        menu.setItem(49, sort_listings);
        menu.setItem(52, return_all);
        menu.setItem(53, info);
    }

    public void show(final Player player, final int page) {
        this.initialize();
        new BukkitRunnable(){

            public void run() {
                ExpiredListingsMenu.this.paginate_task(player, page);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public void paginate_task(final Player player, int page) {
        this.current_page = page;
        this.paginate(player);
        new BukkitRunnable(){

            public void run() {
                ExpiredListingsMenu.this.build_task(player);
            }
        }.runTaskLater((Plugin)AuctionHouse.plugin, 1L);
    }

    public void build_task(final Player player) {
        this.build(player, this.current_page);
        new BukkitRunnable(){

            public void run() {
                ExpiredListingsMenu.this.open_task(player);
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
        Listings expiredListings = AuctionHouse.listings.getExpiredListings(player, this.sortOrder);
        this.expiredListingsCount = expiredListings.count();
        Map<Long, Listing> map = expiredListings.getListings();
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
        int pages = Math.floorDiv(Math.max(size - 1, 0), 45);
        MenuItem blank = new MenuItem(null, "minecraft:air", 1, null);
        MenuItem previous = new MenuItem(this.menu_previous, Config.previous_button, 1, null);
        MenuItem next = new MenuItem(this.menu_next, Config.next_button, 1, null);
        Listings expiredListings = AuctionHouse.listings.getExpiredListings(player, this.sortOrder);
        int offset = page * 45;
        for (int i = 0; i < 45; ++i) {
            int index = offset + i;
            if (index < size) {
                ItemStack is;
                long timestamp = this.page_keys.get(index);
                Listing listing = expiredListings.getListing(timestamp);
                if (listing == null || (is = listing.getItem()) == null) continue;
                String name = ItemUtil.getCustomName(is);
                String key = is.getType().getKey().toString();
                int amount = is.getAmount();
                ItemMeta meta = is.getItemMeta();
                List oldLore = meta != null ? meta.getLore() : new ArrayList();
                String nbt = AuctionHouse.nms.getNBTString(is);
                List<String> desc = Arrays.asList(this.top_rule, this.top_spacing, this.click, this.bottom_spacing, this.bottom_rule);
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
        Auctions.updateCounts(player);
        int expired = Auctions.getExpiredListingsCount((OfflinePlayer)player);
        this.updateReturnAllButton(menu, expired);
    }

    private void updateReturnAllButton(Menu menu, int expired) {
        this.expiredListingsCount = expired;
        List<String> lore = expired > 0 ? MessageUtil.expand(Arrays.asList(this.return_desc1, this.return_desc2)) : MessageUtil.expand(Collections.singletonList(this.return_none));
        menu.setItemLore(52, lore);
        menu.setItemAmount(52, 1);
    }

    private void update(Player player) {
        Menu menu = AuctionHouse.menuManager.getMenu(this.id);
        if (menu == null) {
            return;
        }
        Auctions.updateCounts(player);
        int expired = Auctions.getExpiredListingsCount((OfflinePlayer)player);
        if (this.expiredListingsCount != expired) {
            this.expiredListingsCount = expired;
            int pages = this.paginate(player);
            if (this.current_page > pages) {
                this.current_page = pages;
            }
            this.build(player, this.current_page);
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
            if (click == MenuClickType.LEFT && (tag = this.tags.get(slot)) != null) {
                if (tag.equals((Object)ItemTag.ITEM)) {
                    int index = this.current_page * 45 + slot;
                    int size = this.page_keys.size();
                    if (index < size && size > 0) {
                        SoundUtil.clickSound(player);
                        long timestamp = this.page_keys.get(index);
                        Auctions.returnItem(player, timestamp);
                        this.paginate(player);
                        int expired_listing_count = Auctions.getExpiredListingsCount((OfflinePlayer)player);
                        int pages = Math.floorDiv(Math.max(expired_listing_count - 1, 0), 45);
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
                    return;
                }
                if (tag.equals((Object)ItemTag.RETURN_ALL)) {
                    SoundUtil.clickSound(player);
                    Listings expiredListings = AuctionHouse.listings.getExpiredListings(player, this.sortOrder);
                    int expired_listing_count = Auctions.getExpiredListingsCount((OfflinePlayer)player);
                    if (expired_listing_count > 0) {
                        Auctions.returnAllItems(player);
                        expired_listing_count = Auctions.getExpiredListingsCount((OfflinePlayer)player);
                    }
                    if (expired_listing_count > 0) {
                        this.build(player, this.current_page);
                    } else {
                        Auctions.openPlayerListingsMenu(player);
                    }
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

