package com.spawnchunk.auctionhouse.config;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.events.DropUnclaimedEvent;
import com.spawnchunk.auctionhouse.events.ListingCleanupEvent;
import com.spawnchunk.auctionhouse.events.ServerTickEvent;
import com.spawnchunk.auctionhouse.modules.Auctions;
import com.spawnchunk.auctionhouse.modules.SortOrder;
import com.spawnchunk.auctionhouse.storage.LocaleStorage;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import github.scarsz.discordsrv.DiscordSRV;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Config {
    private FileConfiguration fc = AuctionHouse.plugin.getConfig();
    private static final int config_version = 33;
    public static boolean debug = false;
    public static boolean log_ansi = true;
    public static String locale = "en_us";
    public static String decimal_format = "";
    public static boolean strict = false;
    public static String economy = "vault";
    public static boolean chat_hook = false;
    public static int updateTicks = 0;
    public static double auction_listing_price = 0.0;
    public static double auction_listing_rate = 0.0;
    public static long auction_listing_cooldown = 0L;
    public static long auction_listing_duration = 0L;
    public static boolean show_seconds = false;
    public static double auction_sales_tax = 0.0;
    public static double auction_max_sales_tax = 0.0;
    public static long auction_expired_duration = 0L;
    public static long auction_unclaimed_duration = 0L;
    public static long auction_cleanup_duration = 0L;
    public static long auction_sold_duration = 0L;
    public static boolean auction_prevent_creative = false;
    public static boolean auction_prevent_spectator = false;
    public static boolean auction_prevent_filled_containers = false;
    public static double auction_min_sell_price = 0.0;
    public static double auction_max_sell_price = 0.0;
    public static boolean auction_allow_damaged_items = false;
    public static int auction_default_max_listings = 0;
    public static SortOrder auction_sort_order = SortOrder.CHRONO_OLDEST;
    public static String click_sound = "";
    public static String fail_sound = "";
    public static String drop_sound = "";
    public static String sold_sound = "";
    public static String exit_button = "minecraft:iron_door";
    public static String back_button = "minecraft:iron_door";
    public static String previous_button = "minecraft:paper";
    public static String sort_listings_button = "minecraft:sunflower";
    public static String next_button = "minecraft:paper";
    public static String info_button = "minecraft:book";
    public static String howto_button = "minecraft:emerald";
    public static String return_all_button = "minecraft:flower_pot";
    public static String player_listings_button = "minecraft:diamond";
    public static String expired_listings_button = "minecraft:poisonous_potato";
    public static String sold_items_button = "minecraft:gold_ingot";
    public static String clear_button = "minecraft:barrier";
    public static String confirm_button = "minecraft:lime_stained_glass_pane";
    public static String cancel_button = "minecraft:red_stained_glass_pane";
    public static String sign_trigger = "";
    public static List<String> block_name_triggers = new ArrayList<String>();
    public static List<String> entity_name_triggers = new ArrayList<String>();
    public static String exit_command = "";
    public static boolean log_listed = false;
    public static boolean log_sold = false;
    public static boolean log_cancelled = false;
    public static boolean log_returned = false;
    public static boolean log_dropped = false;
    public static boolean log_purged = false;
    public static List<String> known_worlds = AuctionHouse.plugin.getServer().getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList());
    public static List<String> disabled_worlds = new ArrayList<String>();
    public static boolean announce_chat_listings = false;
    public static boolean announce_chat_purchases = false;
    public static boolean announce_action_bar_listings = false;
    public static boolean announce_action_bar_purchases = false;
    public static boolean announce_discord_listings = false;
    public static boolean announce_discord_purchases = false;
    public static String discord_channel = "";
    public static boolean per_world_listings = false;
    public static boolean group_worlds = false;
    public static boolean replace_item_uuids = false;
    public static boolean replace_player_names = false;
    public static boolean drop_at_feet = false;
    public static long unclaimed_check_duration = 0L;
    public static boolean unclaimed_check_on_world_change = false;
    public static Material item_wildcard = Material.STRUCTURE_VOID;
    public static Map<String, ItemStack> restricted_items = new LinkedHashMap<String, ItemStack>();
    public static Map<String, Boolean> wildcard_name = new HashMap<String, Boolean>();
    public static Map<String, Boolean> wildcard_item = new HashMap<String, Boolean>();
    public static Map<String, Boolean> wildcard_lore = new HashMap<String, Boolean>();
    public static Map<String, Boolean> wildcard_enchantments = new HashMap<String, Boolean>();
    public static Map<String, Boolean> wildcard_damage = new HashMap<String, Boolean>();
    public static Map<String, Boolean> wildcard_unbreakable = new HashMap<String, Boolean>();
    public static Map<String, Boolean> wildcard_custommodeldata = new HashMap<String, Boolean>();
    public static Map<String, Boolean> wildcard_persistentdata = new HashMap<String, Boolean>();
    public static TreeMap<String, String> translations = new TreeMap();
    public static TreeMap<String, String> translations_fallback = new TreeMap();
    public static boolean spawner_info = false;
    public static boolean integer_price = false;
    public static boolean show_repair_cost = false;
    public static boolean spam_check = false;
    public static NumberFormat numberFormat = null;
    public static long auction_future_duration = 0x3FFFFFFFFFFFFFFFL;

    public Config() {
        this.parseConfig();
    }

    private long getDuration(String duration) {
        long millis = 0L;
        if (duration != null && !duration.isEmpty()) {
            String input = "P".concat(duration.replace("d", "DT")).toUpperCase();
            Duration d = Duration.parse(input);
            millis = d.toMillis();
        }
        return millis;
    }

    private void parseConfig() {
        ConfigurationSection cs7;
        ConfigurationSection cs6;
        ConfigurationSection cs5;
        ConfigurationSection cs4;
        ConfigurationSection cs3;
        ConfigurationSection cs1;
        ConfigurationSection cs;
        int version;
        int n = version = this.fc.contains("configVersion") ? this.fc.getInt("configVersion") : 0;
        if (version < 33) {
            this.upgradeConfig(version);
        }
        if (this.fc.contains("debug") && (debug = this.fc.getBoolean("debug"))) {
            AuctionHouse.logger.info("debug = true");
        }
        if (this.fc.contains("ansi")) {
            log_ansi = this.fc.getBoolean("ansi");
            if (debug) {
                AuctionHouse.logger.info(String.format("log_ansi = %s", log_ansi ? "true" : "false"));
            }
        }
        if (this.fc.contains("locale")) {
            locale = this.fc.getString("locale", "en_us");
            if (debug) {
                AuctionHouse.logger.info(String.format("locale = %s", locale));
            }
        }
        locale = AuctionHouse.locales.containsKey(locale) ? locale : "en_us";
        AuctionHouse.logger.info(String.format("Using %s locale", locale));
        numberFormat = this.getNumberFormat(locale);
        if (this.fc.contains("decimal_format") && (decimal_format = this.fc.getString("decimal_format")) != null) {
            if (debug) {
                AuctionHouse.logger.info(String.format("decimal_format = %s", decimal_format));
            }
            this.applyDecimalFormat(decimal_format);
        }
        if (this.fc.contains("strict")) {
            strict = this.fc.getBoolean("strict");
            if (debug) {
                AuctionHouse.logger.info(String.format("strict = %s", strict ? "true" : "false"));
            }
        }
        if (this.fc.contains("economy")) {
            economy = this.fc.getString("economy");
            if (debug) {
                AuctionHouse.logger.info(String.format("economy = %s", economy));
            }
        }
        if (this.fc.contains("chatHook")) {
            chat_hook = this.fc.getBoolean("chatHook");
            if (debug) {
                AuctionHouse.logger.info(String.format("chatHook = %s", chat_hook ? "true" : "false"));
            }
        }
        if (this.fc.contains("updateTicks")) {
            updateTicks = this.fc.getInt("updateTicks");
            if (debug) {
                AuctionHouse.logger.info(String.format("updateTicks = %d", updateTicks));
            }
        }
        int n2 = updateTicks = updateTicks < 1 ? 1 : Math.min(updateTicks, 100);
        if (this.fc.contains("auction") && (cs = this.fc.getConfigurationSection("auction")) != null) {
            String duration;
            if (cs.contains("listingPrice")) {
                auction_listing_price = cs.getDouble("listingPrice");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.listingPrice = %.2f", auction_listing_price));
                }
            }
            if (cs.contains("listingRate")) {
                auction_listing_rate = cs.getDouble("listingRate");
                double d = auction_listing_rate < 0.0 ? 0.0 : (auction_listing_rate = auction_listing_rate > 100.0 ? 100.0 : auction_listing_rate);
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.listingRate = %.2f", auction_listing_rate));
                }
            }
            if (cs.contains("listingCooldown")) {
                duration = cs.getString("listingCooldown");
                auction_listing_cooldown = this.getDuration(duration);
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.listingCooldown = %s", duration));
                }
            }
            if (cs.contains("listingDuration")) {
                duration = cs.getString("listingDuration");
                auction_listing_duration = this.getDuration(duration);
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.listingDuration = %s", duration));
                }
            }
            if (cs.contains("showSeconds")) {
                show_seconds = cs.getBoolean("showSeconds");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.showSeconds = %s", show_seconds ? "true" : "false"));
                }
            }
            if (cs.contains("salesTax")) {
                auction_sales_tax = cs.getDouble("salesTax");
                double d = auction_sales_tax < 0.0 ? 0.0 : (auction_sales_tax = auction_sales_tax > 100.0 ? 100.0 : auction_sales_tax);
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.salesTax = %.2f", auction_sales_tax));
                }
            }
            if (cs.contains("maxSalesTax")) {
                auction_max_sales_tax = cs.getDouble("maxSalesTax");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.maxSalesTax = %.2f", auction_max_sales_tax));
                }
            }
            if (cs.contains("expiredDuration")) {
                duration = cs.getString("expiredDuration");
                auction_expired_duration = this.getDuration(duration);
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.expiredDuration = %s", duration));
                }
            }
            if (cs.contains("unclaimedDuration")) {
                duration = cs.getString("unclaimedDuration");
                auction_unclaimed_duration = this.getDuration(duration);
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.unclaimedDuration = %s", duration));
                }
            }
            if (cs.contains("cleanupDuration")) {
                duration = cs.getString("cleanupDuration");
                auction_cleanup_duration = this.getDuration(duration);
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.cleanupDuration = %s", duration));
                }
            }
            if (cs.contains("soldDuration")) {
                duration = cs.getString("soldDuration");
                auction_sold_duration = this.getDuration(duration);
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.soldDuration = %s", duration));
                }
            }
            if (cs.contains("preventCreative")) {
                auction_prevent_creative = cs.getBoolean("preventCreative");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.preventCreative = %s", auction_prevent_creative ? "true" : "false"));
                }
            }
            if (cs.contains("preventSpectator")) {
                auction_prevent_spectator = cs.getBoolean("preventSpectator");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.preventSpectator = %s", auction_prevent_spectator ? "true" : "false"));
                }
            }
            if (cs.contains("preventFilledContainers")) {
                auction_prevent_filled_containers = cs.getBoolean("preventFilledContainers");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.preventFilledContainers = %s", auction_prevent_filled_containers ? "true" : "false"));
                }
            }
            if (cs.contains("minSellPrice")) {
                auction_min_sell_price = cs.getDouble("minSellPrice");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.minSellPrice = %.2f", auction_min_sell_price));
                }
            }
            if (cs.contains("maxSellPrice")) {
                auction_max_sell_price = cs.getDouble("maxSellPrice");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.maxSellPrice = %.2f", auction_max_sell_price));
                }
            }
            if (cs.contains("allowDamagedItems")) {
                auction_allow_damaged_items = cs.getBoolean("allowDamagedItems");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.allowDamagedItems = %s", auction_allow_damaged_items ? "true" : "false"));
                }
            }
            if (cs.contains("defaultMaxListings")) {
                auction_default_max_listings = cs.getInt("defaultMaxListings");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.defaultMaxListings = %d", auction_default_max_listings));
                }
            }
            if (cs.contains("sortOrder")) {
                String sortOrder = cs.getString("sortOrder");
                if (sortOrder == null) {
                    sortOrder = "oldest";
                }
                switch (sortOrder.toLowerCase()) {
                    case "lowest_price": {
                        auction_sort_order = SortOrder.PRICE_LOWEST;
                        break;
                    }
                    case "highest_price": {
                        auction_sort_order = SortOrder.PRICE_HIGHEST;
                        break;
                    }
                    case "newest": {
                        auction_sort_order = SortOrder.CHRONO_NEWEST;
                        break;
                    }
                    default: {
                        auction_sort_order = SortOrder.CHRONO_OLDEST;
                    }
                }
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.sortOrder = %s", auction_sort_order.toString()));
                }
            }
            if (AuctionHouse.discord && cs.contains("discord_channel")) {
                discord_channel = cs.getString("discord_channel");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.discord_channel = %s", discord_channel));
                }
                if (discord_channel != null && !discord_channel.isEmpty()) {
                    Map channels = DiscordSRV.getPlugin().getChannels();
                    if (channels.containsKey(discord_channel)) {
                        AuctionHouse.logger.warning(String.format("%s is not a valid discord channel name!", discord_channel));
                    } else if (debug) {
                        AuctionHouse.logger.info("channel name is valid");
                    }
                }
            }
            if (cs.contains("multiworld")) {
                per_world_listings = cs.getBoolean("multiworld");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.multiworld = %s", per_world_listings ? "true" : "false"));
                }
            }
            if (cs.contains("groupWorlds")) {
                group_worlds = cs.getBoolean("groupWorlds");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.groupWorlds = %s", group_worlds ? "true" : "false"));
                }
            }
            if (cs.contains("replaceUUIDs")) {
                replace_item_uuids = cs.getBoolean("replaceUUIDs");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.replaceUUIDs = %s", replace_item_uuids ? "true" : "false"));
                }
            }
            if (cs.contains("replacePlayerNames")) {
                replace_player_names = cs.getBoolean("replacePlayerNames");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.replacePlayerNames = %s", replace_player_names ? "true" : "false"));
                }
            }
            if (cs.contains("dropAtFeet")) {
                drop_at_feet = cs.getBoolean("dropAtFeet");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.dropAtFeet = %s", drop_at_feet ? "true" : "false"));
                }
            }
            if (cs.contains("unclaimedCheckDuration")) {
                duration = cs.getString("unclaimedCheckDuration");
                unclaimed_check_duration = this.getDuration(duration);
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.unclaimedCheckDuration = %s", duration));
                }
            }
            if (cs.contains("unclaimedCheckOnWorldChange")) {
                unclaimed_check_on_world_change = cs.getBoolean("unclaimedCheckOnWorldChange");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.unclaimedCheckOnWorldChange = %s", unclaimed_check_on_world_change ? "true" : "false"));
                }
            }
            if (cs.contains("spawnerInfo")) {
                spawner_info = cs.getBoolean("spawnerInfo");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.spawnerInfo = %s", spawner_info ? "true" : "false"));
                }
            }
            if (cs.contains("integerPrice")) {
                integer_price = cs.getBoolean("integerPrice");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.integerPrice = %s", integer_price ? "true" : "false"));
                }
            }
            if (cs.contains("showRepairCost")) {
                show_repair_cost = cs.getBoolean("showRepairCost");
                if (debug) {
                    AuctionHouse.logger.info(String.format("auction.showRepairCost = %s", show_repair_cost ? "true" : "false"));
                }
            }
        }
        if (this.fc.contains("announce") && (cs1 = this.fc.getConfigurationSection("announce")) != null) {
            if (cs1.contains("chat.listings")) {
                announce_chat_listings = cs1.getBoolean("chat.listings");
                if (debug) {
                    AuctionHouse.logger.info(String.format("announce.chat.listings = %s", announce_chat_listings ? "true" : "false"));
                }
            }
            if (cs1.contains("chat.purchases")) {
                announce_chat_purchases = cs1.getBoolean("chat.purchases");
                if (debug) {
                    AuctionHouse.logger.info(String.format("announce.chat.purchases = %s", announce_chat_purchases ? "true" : "false"));
                }
            }
            if (cs1.contains("action_bar.listings")) {
                announce_action_bar_listings = cs1.getBoolean("action_bar.listings");
                if (debug) {
                    AuctionHouse.logger.info(String.format("announce.action_bar.listings = %s", announce_action_bar_listings ? "true" : "false"));
                }
            }
            if (cs1.contains("action_bar.purchases")) {
                announce_action_bar_purchases = cs1.getBoolean("action_bar.purchases");
                if (debug) {
                    AuctionHouse.logger.info(String.format("announce.action_bar.purchases = %s", announce_action_bar_purchases ? "true" : "false"));
                }
            }
            if (cs1.contains("discord.listings")) {
                announce_discord_listings = cs1.getBoolean("discord.listings");
                if (debug) {
                    AuctionHouse.logger.info(String.format("announce.discord.listings = %s", announce_discord_listings ? "true" : "false"));
                }
                if (announce_discord_listings && discord_channel.isEmpty()) {
                    AuctionHouse.logger.warning("announce.discord.listing = true, but discord_channel is empty!");
                }
            }
            if (cs1.contains("discord.purchases")) {
                announce_discord_purchases = cs1.getBoolean("discord.purchases");
                if (debug) {
                    AuctionHouse.logger.info(String.format("announce.discord.purchases = %s", announce_discord_purchases ? "true" : "false"));
                }
                if (announce_discord_listings && discord_channel.isEmpty()) {
                    AuctionHouse.logger.warning("announce.discord.purchases = true, but discord_channel is empty!");
                }
            }
        }
        if (this.fc.contains("restricted_items")) {
            ConfigurationSection cs2 = this.fc.getConfigurationSection("restricted_items");
            if (cs2 != null) {
                Set<?> keys = cs2.getKeys(false);
                for (String key : (Set<String>) keys) {
                    if (!cs2.isConfigurationSection(key)) continue;
                    this.loadRestrictedItem(cs2.getConfigurationSection(key));
                }
            }
            this.logRestrictedItemsList();
        }
        if (this.fc.contains("sounds") && (cs3 = this.fc.getConfigurationSection("sounds")) != null) {
            if (cs3.contains("click")) {
                click_sound = cs3.getString("click");
                if (debug) {
                    AuctionHouse.logger.info(String.format("sounds.click = %s", click_sound));
                }
            }
            if (cs3.contains("fail")) {
                fail_sound = cs3.getString("fail");
                if (debug) {
                    AuctionHouse.logger.info(String.format("sounds.fail = %s", fail_sound));
                }
            }
            if (cs3.contains("drop")) {
                drop_sound = cs3.getString("drop");
                if (debug) {
                    AuctionHouse.logger.info(String.format("sounds.drop = %s", drop_sound));
                }
            }
            if (cs3.contains("sold")) {
                sold_sound = cs3.getString("sold");
                if (debug) {
                    AuctionHouse.logger.info(String.format("sounds.sold = %s", sold_sound));
                }
            }
        }
        if (this.fc.contains("buttons") && (cs4 = this.fc.getConfigurationSection("buttons")) != null) {
            if (cs4.contains("exit")) {
                exit_button = cs4.getString("exit");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.exit = %s", exit_button));
                }
            }
            if (cs4.contains("back")) {
                back_button = cs4.getString("back");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.back = %s", back_button));
                }
            }
            if (cs4.contains("previous")) {
                previous_button = cs4.getString("previous");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.previous = %s", previous_button));
                }
            }
            if (cs4.contains("sort_listings")) {
                sort_listings_button = cs4.getString("sort_listings");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.sort_listings = %s", sort_listings_button));
                }
            }
            if (cs4.contains("next")) {
                next_button = cs4.getString("next");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.next = %s", next_button));
                }
            }
            if (cs4.contains("info")) {
                info_button = cs4.getString("info");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.info = %s", info_button));
                }
            }
            if (cs4.contains("howto")) {
                howto_button = cs4.getString("howto");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.howto = %s", howto_button));
                }
            }
            if (cs4.contains("return_all")) {
                return_all_button = cs4.getString("return_all");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.return_all = %s", return_all_button));
                }
            }
            if (cs4.contains("player_listings")) {
                player_listings_button = cs4.getString("player_listings");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.player_listings = %s", player_listings_button));
                }
            }
            if (cs4.contains("expired_listings")) {
                expired_listings_button = cs4.getString("expired_listings");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.expired_listings = %s", expired_listings_button));
                }
            }
            if (cs4.contains("sold_items")) {
                sold_items_button = cs4.getString("sold_items");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.sold_items = %s", sold_items_button));
                }
            }
            if (cs4.contains("clear")) {
                clear_button = cs4.getString("clear");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.clear = %s", clear_button));
                }
            }
            if (cs4.contains("confirm")) {
                confirm_button = cs4.getString("confirm");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.confirm = %s", confirm_button));
                }
            }
            if (cs4.contains("cancel")) {
                cancel_button = cs4.getString("cancel");
                if (debug) {
                    AuctionHouse.logger.info(String.format("buttons.cancel = %s", cancel_button));
                }
            }
        }
        if (this.fc.contains("triggers") && (cs5 = this.fc.getConfigurationSection("triggers")) != null) {
            String trigger;
            if (cs5.contains("sign")) {
                trigger = cs5.getString("sign");
                String string = sign_trigger = trigger != null ? MessageUtil.sectionSymbol(trigger) : "";
                if (debug) {
                    AuctionHouse.logger.info(String.format("triggers.sign = %s", sign_trigger));
                }
            }
            if (cs5.contains("block_name")) {
                if (cs5.isString("block_name")) {
                    block_name_triggers.add(cs5.getString("block_name"));
                } else if (cs5.isList("block_name")) {
                    block_name_triggers = cs5.getStringList("block_name");
                }
                if (debug) {
                    AuctionHouse.logger.info(String.format("triggers.block_name = %s", block_name_triggers));
                }
            }
            if (cs5.contains("entity_name")) {
                if (cs5.isString("entity_name")) {
                    trigger = cs5.getString("entity_name");
                    if (trigger != null) {
                        entity_name_triggers.add(MessageUtil.sectionSymbol(trigger));
                    }
                } else if (cs5.isList("entity_name")) {
                    entity_name_triggers = cs5.getStringList("entity_name");
                    entity_name_triggers.replaceAll(MessageUtil::sectionSymbol);
                }
                if (debug) {
                    AuctionHouse.logger.info(String.format("triggers.entity_name = %s", entity_name_triggers));
                }
            }
        }
        if (this.fc.contains("commands.exit")) {
            exit_command = this.fc.getString("commands.exit", "");
            if (debug) {
                AuctionHouse.logger.info(String.format("commands.exit = %s", exit_command));
            }
        }
        if (this.fc.contains("log") && (cs6 = this.fc.getConfigurationSection("log")) != null) {
            if (cs6.contains("listed")) {
                log_listed = cs6.getBoolean("listed");
                if (debug) {
                    AuctionHouse.logger.info(String.format("log.listed = %s", log_listed ? "true" : "false"));
                }
            }
            if (cs6.contains("sold")) {
                log_sold = cs6.getBoolean("sold");
                if (debug) {
                    AuctionHouse.logger.info(String.format("log.sold = %s", log_sold ? "true" : "false"));
                }
            }
            if (cs6.contains("cancelled")) {
                log_cancelled = cs6.getBoolean("cancelled");
                if (debug) {
                    AuctionHouse.logger.info(String.format("log.cancelled = %s", log_cancelled ? "true" : "false"));
                }
            }
            if (cs6.contains("returned")) {
                log_returned = cs6.getBoolean("returned");
                if (debug) {
                    AuctionHouse.logger.info(String.format("log.returned = %s", log_returned ? "true" : "false"));
                }
            }
            if (cs6.contains("dropped")) {
                log_dropped = cs6.getBoolean("dropped");
                if (debug) {
                    AuctionHouse.logger.info(String.format("log.dropped = %s", log_dropped ? "true" : "false"));
                }
            }
            if (cs6.contains("purged")) {
                log_purged = cs6.getBoolean("purged");
                if (debug) {
                    AuctionHouse.logger.info(String.format("log.purged = %s", log_purged ? "true" : "false"));
                }
            }
        }
        if (this.fc.contains("anticheat") && (cs7 = this.fc.getConfigurationSection("anticheat")) != null && cs7.contains("spam_check")) {
            spam_check = cs7.getBoolean("spam_check");
            if (debug) {
                AuctionHouse.logger.info(String.format("anticheat.spam_check = %s", spam_check ? "true" : "false"));
            }
        }
        if (this.fc.contains("disabled-worlds")) {
            disabled_worlds = this.fc.getStringList("disabled-worlds");
            if (debug) {
                AuctionHouse.logger.info("disabled-worlds = [");
                if (disabled_worlds.isEmpty()) {
                    AuctionHouse.logger.info("<None>");
                } else {
                    for (String w : disabled_worlds) {
                        AuctionHouse.logger.info(String.format("  %s", w));
                    }
                }
                AuctionHouse.logger.info("]");
                AtomicInteger unknown = new AtomicInteger();
                for (String w : disabled_worlds) {
                    if (known_worlds.contains(w)) continue;
                    MessageUtil.logWarning(String.format("%s is an unknown world!", w));
                    unknown.getAndIncrement();
                }
                if (unknown.get() > 0) {
                    MessageUtil.logWarning("Valid worlds are:");
                    for (String k : known_worlds) {
                        MessageUtil.logWarning(String.format("  %s", k));
                    }
                }
            }
        }
    }

    private void upgradeConfig(int version) {
        ConfigurationSection cs1;
        ConfigurationSection cs;
        if (version > 0) {
            MessageUtil.logWarning("Upgrading config file to the latest version");
        }
        this.fc.options().copyDefaults(true);
        if (this.fc.contains("blacklist") && (cs = this.fc.getConfigurationSection("blacklist")) != null) {
            this.fc.createSection("restricted_items", cs.getValues(true));
            this.fc.set("blacklist", null);
        }
        if (this.fc.contains("auction") && (cs1 = this.fc.getConfigurationSection("auctions")) != null) {
            if (cs1.contains("announce")) {
                cs1.set("announce", null);
            }
            if (cs1.contains("action_bar")) {
                cs1.set("action_bar", null);
            }
            if (cs1.contains("discord")) {
                cs1.set("discord", null);
            }
        }
        this.fc.set("configVersion", (Object)33);
        AuctionHouse.plugin.saveConfig();
    }

    public void reloadConfig() {
        if (debug) {
            AuctionHouse.logger.info(LocaleStorage.translate("message.config.reload", locale));
        }
        AuctionHouse.plugin.reloadConfig();
        this.fc = AuctionHouse.plugin.getConfig();
        restricted_items.clear();
        translations.clear();
        translations_fallback.clear();
        disabled_worlds.clear();
        numberFormat = null;
        LocaleStorage.reloadLocales();
        this.parseConfig();
        Auctions.RebuildAllMenus();
        Server server = AuctionHouse.plugin.getServer();
        ConsoleCommandSender sender = server.getConsoleSender();
        BukkitScheduler scheduler = server.getScheduler();
        scheduler.cancelTask(AuctionHouse.tickEventId);
        AuctionHouse.tickEventId = scheduler.scheduleSyncRepeatingTask((Plugin)AuctionHouse.plugin, () -> Config.lambda$reloadConfig$0((CommandSender)sender, server), 0L, (long)updateTicks);
        scheduler.cancelTask(AuctionHouse.dropEventId);
        long dropTicks = Math.max(1200L, unclaimed_check_duration / 50L);
        AuctionHouse.dropEventId = scheduler.scheduleSyncRepeatingTask((Plugin)AuctionHouse.plugin, () -> Config.lambda$reloadConfig$1((CommandSender)sender, server), 0L, dropTicks);
        scheduler.cancelTask(AuctionHouse.cleanupEventId);
        long cleanupTicks = Math.max(1200L, auction_cleanup_duration / 50L);
        AuctionHouse.cleanupEventId = scheduler.scheduleSyncRepeatingTask((Plugin)AuctionHouse.plugin, () -> Config.lambda$reloadConfig$2((CommandSender)sender, server), 0L, cleanupTicks);
    }

    private void loadRestrictedItem(ConfigurationSection cs) {
        if (cs == null) {
            return;
        }
        String section = cs.getName();
        Set keys = cs.getKeys(false);
        Material material = item_wildcard;
        if (keys.contains("item") && cs.isString("item")) {
            String itemName = cs.getString("item");
            if (itemName != null) {
                Material match = Material.matchMaterial((String)itemName);
                if (match != null) {
                    material = match;
                    wildcard_item.put(section, material == item_wildcard);
                } else {
                    AuctionHouse.logger.info(String.format("Invalid Item Material \"%s\" in restricted items", itemName));
                    wildcard_item.put(section, true);
                }
            } else {
                wildcard_item.put(section, true);
            }
        } else {
            wildcard_item.put(section, true);
        }
        String displayName = null;
        if (keys.contains("name") && cs.isString("name")) {
            displayName = cs.getString("name");
            wildcard_name.put(section, false);
        } else {
            wildcard_name.put(section, true);
        }
        List<String> loreList = new ArrayList();
        if (keys.contains("lore")) {
            String line;
            wildcard_lore.put(section, false);
            if (cs.isList("lore")) {
                loreList = cs.getStringList("lore");
            }
            if (cs.isString("lore") && (line = cs.getString("lore")) != null && !line.isEmpty()) {
                loreList.add(line);
            }
        } else {
            wildcard_lore.put(section, true);
        }
        HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
        if (cs.contains("enchantments")) {
            wildcard_enchantments.put(section, false);
            List<?> enchants = cs.getMapList("enchantments");
            for (Map<?, ?> map : (List<Map<?, ?>>) enchants) {
                Enchantment ench = null;
                int level = 0;
                for (Object k : map.keySet()) {
                    Object v;
                    if (!(k instanceof String)) continue;
                    String key = (String)k;
                    if (key.equalsIgnoreCase("ench")) {
                        v = map.get(key);
                        if (!(v instanceof String)) continue;
                        String e = (String)v;
                        if (e.startsWith("minecraft:")) {
                            NamespacedKey ns = NamespacedKey.minecraft((String)e.replace("minecraft:", ""));
                            ench = Enchantment.getByKey((NamespacedKey)ns);
                            continue;
                        }
                        ench = Enchantment.getByName((String)e);
                        continue;
                    }
                    if (!key.equalsIgnoreCase("level") || !((v = map.get(key)) instanceof Integer)) continue;
                    level = (Integer)v;
                }
                if (ench == null) continue;
                enchantments.put(ench, level);
            }
        } else {
            wildcard_enchantments.put(section, true);
        }
        int damage = 0;
        if (keys.contains("damage")) {
            wildcard_damage.put(section, false);
            try {
                damage = cs.getInt("damage");
            }
            catch (NumberFormatException numberFormatException) {}
        } else {
            wildcard_damage.put(section, true);
        }
        boolean unbreakable = false;
        if (keys.contains("unbreakable")) {
            wildcard_unbreakable.put(section, false);
            unbreakable = cs.getBoolean("unbreakable");
        } else {
            wildcard_unbreakable.put(section, true);
        }
        int custom_model_data = 0;
        if (AuctionHouse.mcVersion >= 1141) {
            if (keys.contains("custom_model_data")) {
                wildcard_custommodeldata.put(section, false);
                try {
                    custom_model_data = cs.getInt("custom_model_data");
                }
                catch (NumberFormatException ench) {}
            } else {
                wildcard_custommodeldata.put(section, true);
            }
        } else {
            wildcard_custommodeldata.put(section, true);
        }
        HashMap<String, Object> persistentData = new HashMap<String, Object>();
        if (AuctionHouse.mcVersion >= 1132 && keys.contains("persistent_data")) {
            ConfigurationSection cs_pdk;
            if (cs.isConfigurationSection("persistent_data") && (cs_pdk = cs.getConfigurationSection("persistent_data")) != null) {
                Set<String> pdk_keys = cs_pdk.getKeys(false);
                for (String key : pdk_keys) {
                    Object obj = cs_pdk.get(key);
                    if (obj == null) continue;
                    if (obj instanceof String) {
                        persistentData.put(key, obj);
                        continue;
                    }
                    if (obj instanceof Boolean) {
                        persistentData.put(key, obj);
                        continue;
                    }
                    if (obj instanceof Byte) {
                        persistentData.put(key, obj);
                        continue;
                    }
                    if (obj instanceof Short) {
                        persistentData.put(key, obj);
                        continue;
                    }
                    if (obj instanceof Integer) {
                        persistentData.put(key, obj);
                        continue;
                    }
                    if (obj instanceof Long) {
                        persistentData.put(key, obj);
                        continue;
                    }
                    if (obj instanceof Float) {
                        persistentData.put(key, obj);
                        continue;
                    }
                    if (!(obj instanceof Double)) continue;
                    persistentData.put(key, obj);
                }
                if (!persistentData.isEmpty()) {
                    wildcard_persistentdata.put(section, false);
                }
            }
        } else {
            wildcard_persistentdata.put(section, true);
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        boolean changed = false;
        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(MessageUtil.sectionSymbol(displayName).replaceAll("\\\\u00[aA]7", "\u00a7"));
                changed = true;
            }
            if (!loreList.isEmpty()) {
                ArrayList<String> lore = new ArrayList<String>();
                for (String l : loreList) {
                    if (l == null) continue;
                    String line = l.replaceAll("\\\\u00[aA]7", "\u00a7");
                    lore.add(MessageUtil.sectionSymbol(line));
                }
                meta.setLore(lore);
                changed = true;
            }
            if (!enchantments.isEmpty()) {
                for (Enchantment ench : enchantments.keySet()) {
                    meta.addEnchant(ench, Math.max(0, (Integer)enchantments.get(ench)), true);
                }
                changed = true;
            }
            if (meta instanceof Damageable && damage > 0) {
                ((Damageable)meta).setDamage(damage);
                changed = true;
            }
            if (unbreakable) {
                meta.setUnbreakable(true);
                changed = true;
            }
            if (changed) {
                item.setItemMeta(meta);
            }
            if (AuctionHouse.mcVersion >= 1141 && custom_model_data > 0) {
                item = AuctionHouse.nms.setCustomModelData(item, custom_model_data);
            }
            if (AuctionHouse.mcVersion >= 1132 && !persistentData.isEmpty()) {
                for (String key : persistentData.keySet()) {
                    Object value = persistentData.get(key);
                    item = AuctionHouse.nms.setPersistentDataKey(item, key, value);
                }
            }
        }
        restricted_items.put(section, item);
    }

    private void logRestrictedItemsList() {
        if (debug) {
            for (String section : restricted_items.keySet()) {
                ItemStack item = restricted_items.get(section);
                AuctionHouse.logger.info(String.format("restricted_items.%s = ItemStack {", section));
                AuctionHouse.logger.info(String.format("  Material = %s", item.getType() == item_wildcard ? "WILDCARD" : item.getType().getKey()));
                ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta != null) {
                    Map<String, Object> persistentData;
                    int customModelData;
                    int durability;
                    List<String> lore;
                    if (itemMeta.hasDisplayName()) {
                        AuctionHouse.logger.info(String.format("  Name = %s", itemMeta.getDisplayName()));
                    }
                    if ((lore = itemMeta.getLore()) != null && !lore.isEmpty()) {
                        AuctionHouse.logger.info("  Lore = [");
                        for (String line : lore) {
                            AuctionHouse.logger.info(String.format("    %s,", line));
                        }
                        AuctionHouse.logger.info("  ]");
                    }
                    if (itemMeta.hasEnchants()) {
                        Map<Enchantment, Integer> enchants = itemMeta.getEnchants();
                        AuctionHouse.logger.info("  Enchantments = [");
                        for (Enchantment ench : enchants.keySet()) {
                            int level = (Integer)enchants.get(ench);
                            AuctionHouse.logger.info(String.format("    {%s, %d},", ench.getKey().getKey(), level));
                        }
                        AuctionHouse.logger.info("  ]");
                    }
                    if (itemMeta instanceof Damageable && (durability = ((Damageable)item.getItemMeta()).getDamage()) > 0) {
                        AuctionHouse.logger.info(String.format("  Damage = %d", durability));
                    }
                    if (itemMeta.isUnbreakable()) {
                        AuctionHouse.logger.info("  Unbreakable = true");
                    }
                    if (AuctionHouse.mcVersion >= 1141 && (customModelData = AuctionHouse.nms.getCustomModelData(item)) > 0) {
                        AuctionHouse.logger.info(String.format("  CustomModelData = %d", customModelData));
                    }
                    if (AuctionHouse.mcVersion >= 1132 && !(persistentData = AuctionHouse.nms.getPersistentData(item)).isEmpty()) {
                        AuctionHouse.logger.info("  PersistentData:");
                        for (String key : persistentData.keySet()) {
                            Object value = persistentData.get(key);
                            String class_name = value.getClass().getCanonicalName().replace("java.lang.", "");
                            AuctionHouse.logger.info(String.format("    %s = (%s) %s", key, class_name, value.toString()));
                        }
                    }
                }
                AuctionHouse.logger.info("}");
            }
        }
    }

    public NumberFormat getNumberFormat(@Nullable String locale) {
        Locale loc = Locale.US;
        if (locale != null && locale.contains("_")) {
            String language = locale.split("_")[0];
            String country = locale.split("_")[1];
            loc = new Locale(language, country);
        }
        NumberFormat nf = NumberFormat.getInstance(loc);
        return nf;
    }

    public void applyDecimalFormat(String decimal_format) {
        if (!decimal_format.isEmpty() && numberFormat instanceof DecimalFormat) {
            ((DecimalFormat)numberFormat).applyPattern(decimal_format);
            DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat)numberFormat).getDecimalFormatSymbols();
            if (decimalFormatSymbols.getGroupingSeparator() == '@') {
                numberFormat.setGroupingUsed(false);
            }
        }
    }

    private static /* synthetic */ void lambda$reloadConfig$2(CommandSender sender, Server server) {
        ListingCleanupEvent event = new ListingCleanupEvent(sender, server);
        Bukkit.getPluginManager().callEvent((Event)event);
    }

    private static /* synthetic */ void lambda$reloadConfig$1(CommandSender sender, Server server) {
        DropUnclaimedEvent event = new DropUnclaimedEvent(sender, server);
        Bukkit.getPluginManager().callEvent((Event)event);
    }

    private static /* synthetic */ void lambda$reloadConfig$0(CommandSender sender, Server server) {
        ServerTickEvent event = new ServerTickEvent(sender, server);
        Bukkit.getPluginManager().callEvent((Event)event);
    }
}

