package com.spawnchunk.auctionhouse.menus;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.events.MenuClickEvent;
import com.spawnchunk.auctionhouse.menus.ItemTag;
import com.spawnchunk.auctionhouse.menus.Menu;
import com.spawnchunk.auctionhouse.menus.MenuClickType;
import com.spawnchunk.auctionhouse.menus.MenuItem;
import com.spawnchunk.auctionhouse.modules.Auctions;
import com.spawnchunk.auctionhouse.modules.Listing;
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

public class PurchaseItemMenu
implements Listener {
    private final String id;
    private long timestamp;
    private int return_page;
    private final Map<Integer, ItemTag> tags = new HashMap<Integer, ItemTag>();
    final String menu_confirm = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.confirm", Config.locale));
    final String menu_cancel = MessageUtil.sectionSymbol(LocaleStorage.translate("message.menu.cancel", Config.locale));
    final String purchase_item_title = MessageUtil.sectionSymbol(LocaleStorage.translate("message.purchase_item.title", Config.locale));
    final String top_rule = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.horizontal.rule.top", Config.locale));
    final String price_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.price.value", Config.locale));
    final String seller_value = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.seller.value", Config.locale));
    final String bottom_rule = MessageUtil.sectionSymbol(LocaleStorage.translate("message.listing.horizontal.rule.top", Config.locale));

    public PurchaseItemMenu(Player player) {
        UUID uuid = player.getUniqueId();
        String title = this.purchase_item_title;
        if (title.isEmpty()) {
            title = "Purchase Item: Are you sure?";
        }
        this.id = AuctionHouse.menuManager.createMenu(uuid, title, 9);
        Bukkit.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)AuctionHouse.plugin);
    }

    public void initialize() {
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

    public void show(Player player, long key, int page) {
        UUID uuid = player.getUniqueId();
        this.initialize();
        this.return_page = page;
        this.timestamp = key;
        this.confirmPurchase(this.timestamp);
        AuctionHouse.menuManager.openMenu(uuid, this.id);
    }

    public void close(Player player) {
        UUID uuid = player.getUniqueId();
        AuctionHouse.menuManager.closeMenu(uuid, this.id);
    }

    private void confirmPurchase(long timestamp) {
        Menu menu = AuctionHouse.menuManager.getMenu(this.id);
        if (menu == null) {
            return;
        }
        Listing listing = AuctionHouse.listings.getListing(timestamp);
        ItemStack is = listing.getItem();
        if (is != null) {
            String name = ItemUtil.getCustomName(is);
            String key = is.getType().getKey().toString();
            int amount = is.getAmount();
            ItemMeta meta = is.getItemMeta();
            List oldLore = meta != null ? meta.getLore() : new ArrayList();
            String nbt = AuctionHouse.nms.getNBTString(is);
            float price = listing.getPrice();
            ListingType type = listing.getType();
            String seller = type.isServer() ? AuctionHouse.servername : (listing.getSeller() != null ? listing.getSeller().getName() : listing.getSeller_UUID());
            List<String> desc = Arrays.asList(this.top_rule, MessageUtil.populate(this.price_value, Float.valueOf(price)), MessageUtil.populate(this.seller_value, seller), this.bottom_rule);
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
    public void onMenuClick(MenuClickEvent event) {
        ItemTag tag;
        Player player = event.getPlayer();
        String id = event.getId();
        if (player == null || id == null) {
            return;
        }
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
        if (id.equals(this.id) && click == MenuClickType.LEFT && (tag = this.tags.get(slot)) != null) {
            if (tag.equals((Object)ItemTag.CONFIRM)) {
                SoundUtil.clickSound(player);
                if (!Auctions.purchaseItem(player, this.timestamp)) {
                    SoundUtil.failSound(player);
                }
                Auctions.openActiveListingsMenu(player, this.return_page);
            } else if (tag.equals((Object)ItemTag.CANCEL)) {
                SoundUtil.clickSound(player);
                Auctions.openActiveListingsMenu(player, this.return_page);
            }
        }
    }
}

