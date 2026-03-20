package com.spawnchunk.auctionhouse.menus;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.events.MenuClickEvent;
import com.spawnchunk.auctionhouse.events.MenuCloseEvent;
import com.spawnchunk.auctionhouse.menus.ItemTag;
import com.spawnchunk.auctionhouse.menus.Menu;
import com.spawnchunk.auctionhouse.menus.MenuClickType;
import com.spawnchunk.auctionhouse.menus.MenuItem;
import com.spawnchunk.auctionhouse.modules.Auctions;
import com.spawnchunk.auctionhouse.modules.ListingType;
import com.spawnchunk.auctionhouse.storage.LocaleStorage;
import com.spawnchunk.auctionhouse.util.ItemUtil;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import com.spawnchunk.auctionhouse.util.SoundUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class ConfirmListingMenu
implements Listener {
    private final String id;
    private ItemStack item;
    private final float price;
    private final double listing_fee;
    private final ListingType type;
    private final Map<Integer, ItemTag> tags = new HashMap<Integer, ItemTag>();
    private boolean cancelled = false;
    final String menu_confirm = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.confirm", Config.locale));
    final String menu_cancel = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.cancel", Config.locale));
    final String confirm_listing_title = MessageUtil.sectionSymbol(LocaleStorage.translate("message.confirm_listing.title", Config.locale));
    final String top_rule = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.horizontal.rule.top", Config.locale));
    final String bottom_rule = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.horizontal.rule.top", Config.locale));
    final String fee_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.fee.value", Config.locale));

    public ConfirmListingMenu(Player player, float price, double listing_fee, ListingType type) {
        UUID uuid = player.getUniqueId();
        this.price = price;
        this.listing_fee = listing_fee;
        this.type = type;
        this.cancelled = true;
        String title = MessageUtil.populate(this.confirm_listing_title, listing_fee);
        if (title.isEmpty()) {
            title = String.format("Pay $%s Listing Fee?", listing_fee);
        }
        this.id = AuctionHouse.menuManager.createMenu(uuid, title, 9);
        Bukkit.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)AuctionHouse.plugin);
    }

    private void initialize() {
        Menu menu = AuctionHouse.menuManager.getMenu(this.id);
        if (menu == null) {
            return;
        }
        this.tags.put(0, ItemTag.CONFIRM);
        this.tags.put(1, ItemTag.CONFIRM);
        this.tags.put(2, ItemTag.CONFIRM);
        this.tags.put(3, ItemTag.CONFIRM);
        this.tags.put(5, ItemTag.CANCEL);
        this.tags.put(6, ItemTag.CANCEL);
        this.tags.put(7, ItemTag.CANCEL);
        this.tags.put(8, ItemTag.CANCEL);
        MenuItem confirm = new MenuItem(this.menu_confirm, Config.confirm_button, 1, null);
        MenuItem cancel = new MenuItem(this.menu_cancel, Config.cancel_button, 1, null);
        menu.setItem(0, confirm);
        menu.setItem(1, confirm);
        menu.setItem(2, confirm);
        menu.setItem(3, confirm);
        menu.setItem(5, cancel);
        menu.setItem(6, cancel);
        menu.setItem(7, cancel);
        menu.setItem(8, cancel);
    }

    public void show(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        this.initialize();
        this.item = item;
        this.confirmListing(item);
        AuctionHouse.menuManager.openMenu(uuid, this.id);
    }

    public void close(Player player) {
        UUID uuid = player.getUniqueId();
        AuctionHouse.menuManager.closeMenu(uuid, this.id);
        AuctionHouse.menuManager.removeMenu(this.id);
    }

    private void confirmListing(ItemStack is) {
        Menu menu = AuctionHouse.menuManager.getMenu(this.id);
        if (menu == null) {
            return;
        }
        if (is != null) {
            String name = ItemUtil.getCustomName(is);
            String key = is.getType().getKey().toString();
            int amount = is.getAmount();
            ItemMeta meta = is.getItemMeta();
            List oldLore = meta != null ? meta.getLore() : new ArrayList();
            String nbt = AuctionHouse.nms.getNBTString(is);
            List<String> desc = Arrays.asList(this.top_rule, MessageUtil.populate(this.fee_value, this.listing_fee), this.bottom_rule);
            ArrayList<String> lore = new ArrayList<String>();
            if (oldLore != null) {
                lore.addAll(oldLore);
            }
            for (String s : MessageUtil.expand(desc)) {
                if (s.isEmpty()) continue;
                lore.add(s);
            }
            MenuItem menuItem = new MenuItem(name, key, amount, lore, nbt);
            menu.setItem(4, menuItem);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onMenuClose(MenuCloseEvent event) {
        Player player = event.getPlayer();
        String id = event.getId();
        if (player == null || id == null) {
            return;
        }
        Menu menu = AuctionHouse.menuManager.getMenu(id);
        if (menu != null && id.equals(this.id)) {
            if (!player.isOnline()) {
                this.cancelled = true;
            }
            Auctions.completeListing(player, this.item, this.price, this.listing_fee, this.type, this.cancelled);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onMenuClick(MenuClickEvent event) {
        Player player = event.getPlayer();
        String id = event.getId();
        if (player == null || id == null) {
            return;
        }
        Menu menu = AuctionHouse.menuManager.getMenu(id);
        if (menu != null) {
            ItemTag tag;
            int slot = event.getSlot();
            ItemStack item = menu.getItem(slot);
            if (item == null) {
                return;
            }
            if (item.getType().equals((Object)Material.AIR)) {
                return;
            }
            MenuClickType click = event.getMenuClickType();
            if (id.equals(this.id) && click == MenuClickType.LEFT && (tag = this.tags.get(slot)) != null) {
                if (tag.equals((Object)ItemTag.CONFIRM)) {
                    SoundUtil.clickSound(player);
                    this.cancelled = false;
                    this.close(player);
                } else if (tag.equals((Object)ItemTag.CANCEL)) {
                    SoundUtil.clickSound(player);
                    this.cancelled = true;
                    this.close(player);
                }
            }
        }
    }
}

