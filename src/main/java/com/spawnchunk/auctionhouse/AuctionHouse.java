package com.spawnchunk.auctionhouse;

import com.spawnchunk.auctionhouse.commands.AHCommand;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.events.DropUnclaimedEvent;
import com.spawnchunk.auctionhouse.events.ListingCleanupEvent;
import com.spawnchunk.auctionhouse.events.ServerTickEvent;
import com.spawnchunk.auctionhouse.listeners.AuctionListener;
import com.spawnchunk.auctionhouse.listeners.CleanupListener;
import com.spawnchunk.auctionhouse.listeners.DiscordSRVListener;
import com.spawnchunk.auctionhouse.listeners.MenuListener;
import com.spawnchunk.auctionhouse.listeners.PlayerListener;
import com.spawnchunk.auctionhouse.menus.MenuManager;
import com.spawnchunk.auctionhouse.modules.Listings;
import com.spawnchunk.auctionhouse.nms.NMS;
import com.spawnchunk.auctionhouse.nms.v1_20_R3;
import com.spawnchunk.auctionhouse.nms.v1_21_R3;
import com.spawnchunk.auctionhouse.placeholders.Expansion;
import com.spawnchunk.auctionhouse.placeholders.MVdWExpansion;
import com.spawnchunk.auctionhouse.storage.DatabaseStorage;
import com.spawnchunk.auctionhouse.storage.ImportDBFile;
import com.spawnchunk.auctionhouse.storage.ImportDatFile;
import com.spawnchunk.auctionhouse.storage.LocaleStorage;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import com.spawnchunk.auctionhouse.util.WorldUtil;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class AuctionHouse
extends JavaPlugin
implements Listener {
    public static AuctionHouse plugin;
    public static PluginManager pm;
    public static Chat chat;
    public static Economy econ;
    public static HeadDatabaseAPI hdb;
    public static TokenEnchantAPI te;
    public static boolean discord;
    public static Connection conn;
    public static DiscordSRVListener discordSRVListener;
    public static NMS nms;
    public static Config config;
    public static Logger logger;
    public static String version;
    public static int mcVersion;
    public static String servername;
    public static String levelname;
    public static Listings listings;
    public static final MenuManager menuManager;
    public static Map<UUID, Long> playerCooldowns;
    public static int tickEventId;
    public static int dropEventId;
    public static int cleanupEventId;
    public static final Map<String, TreeMap<String, String>> locales;

    public static void main(String[] args) {
    }

    public void onEnable() {
        plugin = this;
        logger = plugin.getLogger();
        Server server = this.getServer();
        LocaleStorage.loadLocales();
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException e) {
            MessageUtil.logSevere("Could not load SQLite JDBC driver!");
            MessageUtil.logSevere("Plugin will be disabled!");
            this.getServer().getPluginManager().disablePlugin((Plugin)plugin);
            return;
        }
        conn = DatabaseStorage.getConnection();
        if (conn == null) {
            MessageUtil.logSevere("Could not create SQLite database!");
            MessageUtil.logSevere("Plugin will be disabled!");
            this.getServer().getPluginManager().disablePlugin((Plugin)plugin);
            return;
        }
        String packageName = plugin.getServer().getClass().getPackage().getName();
        version = packageName.substring(packageName.lastIndexOf(46) + 1);
        // Paper 1.20.5+ removed versioned CraftBukkit packages
        String mcVersionStr = Bukkit.getMinecraftVersion();
        try {
            mcVersion = Integer.parseInt(mcVersionStr.replaceAll("[^0-9]", ""));
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        logger.info(String.format("Detected Minecraft version %s (parsed: %d)", mcVersionStr, mcVersion));
        if (mcVersion >= 1214) {
            nms = new v1_21_R3();
        } else if (version.equals("v1_20_R3")) {
            nms = new v1_20_R3();
        } else {
            logger.log(Level.SEVERE, "Error! This plugin supports Minecraft 1.20.4 and 1.21.4+!");
            logger.log(Level.SEVERE, "Plugin will be disabled!");
            this.getServer().getPluginManager().disablePlugin((Plugin)plugin);
            return;
        }
        levelname = WorldUtil.getMainWorld();
        this.onServerFullyLoaded();
        if (this.setupHDB()) {
            logger.log(Level.INFO, "Enabled HeadDatabase support");
        }
        if (this.setupDiscordSRV()) {
            logger.log(Level.INFO, "Enabled DiscordSRV support");
        }
        config = new Config();
        servername = LocaleStorage.translate("server.name", Config.locale);
        if (!Config.chat_hook) {
            chat = null;
            MessageUtil.logWarning("Meta-based auction limits will be disabled");
        }
        DatabaseStorage.readAllListings(conn);
        ImportDatFile.importData();
        ImportDBFile.importData();
        ConsoleCommandSender sender = server.getConsoleSender();
        BukkitScheduler scheduler = server.getScheduler();
        PluginManager pm = Bukkit.getPluginManager();
        ServerTickEvent serverTickEvent = new ServerTickEvent((CommandSender)sender, server);
        tickEventId = scheduler.scheduleSyncRepeatingTask((Plugin)this, () -> pm.callEvent((Event)serverTickEvent), 0L, (long)Config.updateTicks);
        long dropTicks = Math.max(1200L, Config.unclaimed_check_duration / 50L);
        DropUnclaimedEvent dropUnclaimedEvent = new DropUnclaimedEvent((CommandSender)sender, server);
        dropEventId = scheduler.scheduleSyncRepeatingTask((Plugin)this, () -> pm.callEvent((Event)dropUnclaimedEvent), 0L, dropTicks);
        long cleanupTicks = Math.max(1200L, Config.auction_cleanup_duration / 50L);
        if (Config.debug) {
            logger.info(String.format("cleanupTicks = %s", cleanupTicks));
        }
        ListingCleanupEvent listingCleanupEvent = new ListingCleanupEvent((CommandSender)sender, server);
        cleanupEventId = scheduler.scheduleSyncRepeatingTask((Plugin)this, () -> pm.callEvent((Event)listingCleanupEvent), 0L, cleanupTicks);
    }

    public void onDisable() {
        menuManager.closeAllMenus();
        logger.info("Saving auctions data");
        if (conn != null) {
            try {
                conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider rsp_econ = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp_econ != null) {
            logger.info(String.format("Registered Service Provider %s for Vault's Economy API", rsp_econ.getPlugin().getName()));
            econ = (Economy)rsp_econ.getProvider();
        }
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider rsp_chat = this.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp_chat != null) {
            logger.info(String.format("Registered Service Provider %s for Vault's Chat API", rsp_chat.getPlugin().getName()));
            chat = (Chat)rsp_chat.getProvider();
        }
        return chat != null;
    }

    private boolean setupHDB() {
        hdb = this.getServer().getPluginManager().isPluginEnabled("HeadDatabase") ? new HeadDatabaseAPI() : null;
        return hdb != null;
    }

    private boolean setupDiscordSRV() {
        discord = this.getServer().getPluginManager().isPluginEnabled("DiscordSRV");
        return discord;
    }

    public void onServerFullyLoaded() {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)plugin, () -> {
            pm = this.getServer().getPluginManager();
            if (Config.economy.equalsIgnoreCase("vault")) {
                if (pm.getPlugin("Vault") == null) {
                    MessageUtil.logSevere("Error! Required Vault plugin was not found!");
                    MessageUtil.logSevere("Plugin will be disabled!");
                    pm.disablePlugin((Plugin)plugin);
                    return;
                }
                if (!this.setupEconomy()) {
                    MessageUtil.logSevere("Error! No plugin supporting Vault's Economy API was found!");
                    MessageUtil.logSevere("Plugin will be disabled!");
                    pm.disablePlugin((Plugin)plugin);
                    return;
                }
                if (!this.setupChat()) {
                    MessageUtil.logWarning("Warning! No plugin supporting Vault's Chat API was found!");
                    MessageUtil.logWarning("Meta-based auction limits are disabled.");
                }
            } else if (Config.economy.equalsIgnoreCase("tokenenchant")) {
                if (pm.getPlugin("TokenEnchant") != null) {
                    te = TokenEnchantAPI.getInstance();
                    if (te != null) {
                        logger.info("Enabled TokenEnchant API support");
                    }
                } else {
                    logger.warning("Error! Required TokenEnchant plugin was not found!");
                    logger.warning("Plugin will be disabled!");
                    pm.disablePlugin((Plugin)plugin);
                    return;
                }
                if (pm.getPlugin("Vault") == null && !this.setupChat()) {
                    MessageUtil.logWarning("Warning! No plugin supporting Vault's Chat API was found!");
                    MessageUtil.logWarning("Meta-based auction limits are disabled.");
                }
            } else {
                MessageUtil.logSevere("Error! Economy plugin was not found!");
                MessageUtil.logSevere("Plugin will be disabled!");
                pm.disablePlugin((Plugin)plugin);
                return;
            }
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)plugin, this::registerExpansion);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)plugin, this::registerMVdW);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)plugin, this::registerListeners);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)plugin, this::registerCommands);
        });
    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents((Listener)new MenuListener(), (Plugin)plugin);
        this.getServer().getPluginManager().registerEvents((Listener)new PlayerListener(), (Plugin)plugin);
        this.getServer().getPluginManager().registerEvents((Listener)new CleanupListener(), (Plugin)plugin);
        this.getServer().getPluginManager().registerEvents((Listener)new AuctionListener(), (Plugin)plugin);
    }

    private void registerCommands() {
        Objects.requireNonNull(plugin.getCommand("ah")).setExecutor((CommandExecutor)new AHCommand());
    }

    private void registerExpansion() {
        if (this.setupPlaceholderAPI()) {
            new Expansion(this).register();
            logger.info("Registered PlaceholderAPI placeholders");
        }
    }

    private void registerMVdW() {
        if (this.setupMVdWPlaceholderAPI()) {
            new MVdWExpansion(this).register();
            logger.info("Registered MVdWPlaceholderAPI placeholders");
        }
    }

    private boolean setupPlaceholderAPI() {
        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null && papi.isEnabled()) {
            logger.info("Found PlaceholderAPI plugin");
            return true;
        }
        return false;
    }

    private boolean setupMVdWPlaceholderAPI() {
        Plugin mvdw = Bukkit.getPluginManager().getPlugin("MVdWPlaceholderAPI");
        if (mvdw != null && mvdw.isEnabled()) {
            logger.info("Found MVdWPlaceholderAPI plugin");
            return true;
        }
        return false;
    }

    static {
        chat = null;
        econ = null;
        hdb = null;
        te = null;
        discord = false;
        conn = null;
        discordSRVListener = new DiscordSRVListener();
        listings = new Listings();
        menuManager = new MenuManager();
        playerCooldowns = new HashMap<UUID, Long>();
        locales = new HashMap<String, TreeMap<String, String>>();
    }
}

