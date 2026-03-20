package com.spawnchunk.auctionhouse.listeners;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.events.AuctionItemEvent;
import com.spawnchunk.auctionhouse.events.ItemAction;
import com.spawnchunk.auctionhouse.events.ListItemEvent;
import com.spawnchunk.auctionhouse.events.PrePurchaseItemEvent;
import com.spawnchunk.auctionhouse.events.PurchaseItemEvent;
import com.spawnchunk.auctionhouse.modules.ListingType;
import com.spawnchunk.auctionhouse.storage.LocaleStorage;
import com.spawnchunk.auctionhouse.util.ItemUtil;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AuctionListener
implements Listener {
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onListItemEvent(ListItemEvent event) {
        if (Config.debug) {
            AuctionHouse.logger.info("ListItemEvent");
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPrePurchaseItemEvent(PrePurchaseItemEvent event) {
        if (Config.debug) {
            AuctionHouse.logger.info("PrePurchaseItemEvent");
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPurchaseItemEvent(PurchaseItemEvent event) {
        if (Config.debug) {
            AuctionHouse.logger.info("PurchaseItemEvent");
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onAuctionItemEvent(AuctionItemEvent event) {
        if (Config.debug) {
            AuctionHouse.logger.info("AuctionItemEvent");
        }
        final ItemAction action = event.getItemAction();
        final ItemStack item = event.getItem();
        final float price = event.getPrice();
        ListingType type = event.getType();
        final String world = event.getWorld();
        final String seller = type.isServer() ? AuctionHouse.servername : (event.getSeller() != null ? event.getSeller().getName() : event.getSeller_UUID());
        final String buyer = event.getBuyer() != null ? event.getBuyer().getName() : "";
        new BukkitRunnable(){

            public void run() {
                if (Config.log_listed || Config.log_sold || Config.log_cancelled || Config.log_returned || Config.log_dropped || Config.log_purged) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                    Date date = new Date();
                    String path = AuctionHouse.plugin.getDataFolder().getPath() + File.separator + "logs";
                    String file = String.format("%s.log", dateFormat.format(date));
                    String filename = path + File.separator + file;
                    String time = String.format("[%s]", timeFormat.format(date));
                    File dir = new File(path);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    int count = item.getAmount();
                    String name = ItemUtil.getName(item);
                    switch (action) {
                        case ITEM_LISTED: {
                            if (Config.log_listed) {
                                String enchants = AuctionListener.this.getEnchants(item);
                                AuctionListener.this.logMessage(filename, String.format("%s [ITEM_LISTED]: %s", time, MessageUtil.populate(LocaleStorage.translate("message.log.item_listed", Config.locale), seller, count, name, enchants, Float.valueOf(price), world)));
                            }
                            return;
                        }
                        case ITEM_SOLD: {
                            if (Config.log_sold) {
                                String enchants = AuctionListener.this.getEnchants(item);
                                AuctionListener.this.logMessage(filename, String.format("%s [ITEM_SOLD]: %s", time, MessageUtil.populate(LocaleStorage.translate("message.log.item_sold", Config.locale), seller, count, name, enchants, buyer, Float.valueOf(price), world)));
                            }
                            return;
                        }
                        case ITEM_CANCELLED: {
                            if (Config.log_cancelled) {
                                String enchants = AuctionListener.this.getEnchants(item);
                                AuctionListener.this.logMessage(filename, String.format("%s [ITEM_CANCELLED]: %s", time, MessageUtil.populate(LocaleStorage.translate("message.log.item_cancelled", Config.locale), seller, count, name, enchants, Float.valueOf(price), world)));
                            }
                            return;
                        }
                        case ITEM_RETURNED: {
                            if (Config.log_returned) {
                                String enchants = AuctionListener.this.getEnchants(item);
                                AuctionListener.this.logMessage(filename, String.format("%s [ITEM_RETURNED]: %s", time, MessageUtil.populate(LocaleStorage.translate("message.log.item_returned", Config.locale), count, name, enchants, seller, world)));
                            }
                            return;
                        }
                        case ITEM_PURGED: {
                            if (Config.log_purged) {
                                String enchants = AuctionListener.this.getEnchants(item);
                                AuctionListener.this.logMessage(filename, String.format("%s [ITEM_PURGED]: %s", time, MessageUtil.populate(LocaleStorage.translate("message.log.item_purged", Config.locale), seller, count, name, enchants, Float.valueOf(price), world)));
                            }
                            return;
                        }
                    }
                }
            }
        }.runTaskAsynchronously((Plugin)AuctionHouse.plugin);
    }

    private String getEnchants(ItemStack item) {
        String enchants = "";
        if (ItemUtil.hasEnchants(item)) {
            List<String> enchantments = ItemUtil.getEnchants(item);
            StringJoiner joiner = new StringJoiner(", ");
            for (String enchant : enchantments) {
                joiner.add(enchant);
            }
            enchants = String.format(" [%s]", joiner.toString());
        }
        return enchants;
    }

    private void logMessage(String filename, String message) {
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw);){
            out.println(MessageUtil.nocolor(message));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

