package com.spawnchunk.auctionhouse.storage;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.modules.Listing;
import com.spawnchunk.auctionhouse.modules.ListingType;
import com.spawnchunk.auctionhouse.util.FileUtil;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.error.YAMLException;

public class DatabaseStorage {
    static String database = "auctions.db";
    static String path = AuctionHouse.plugin.getDataFolder().getAbsolutePath();
    static String set_pragma = "PRAGMA auto_vacuum = FULL;";
    static String check_table = "SELECT name FROM sqlite_master WHERE type='table' AND name='listings';";
    static String create_table = "CREATE TABLE listings (id INT PRIMARY KEY NOT NULL,world TEXT,seller_uuid TEXT,buyer_uuid TEXT,bidder_uuid TEXT,price REAL,reserve REAL,bid REAL,listing_type TEXT,item BLOB);";
    static String check_table_for_type = "PRAGMA table_info('listings');";
    static String alter_table_for_type = "ALTER TABLE listings ADD COLUMN listing_type STRING DEFAULT 'PLAYER_LISTING';";
    static String vacuum = "VACUUM;";
    static String select_all_rows = "SELECT * FROM listings;";
    static String check_row = "SELECT * FROM listings WHERE id = %d;";
    static String insert_row = "INSERT into listings (id, world, seller_uuid, buyer_uuid, bidder_uuid, price, reserve, bid, listing_type, item) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    static String update_row = "UPDATE listings SET id = ?, world = ?, seller_uuid = ?, buyer_uuid = ?, bidder_uuid = ?, price = ?, reserve = ?, bid = ?, listing_type = ?, item = ? WHERE id = ?;";
    static String delete_row = "DELETE FROM listings WHERE id = %d;";
    static String delete_all_rows = "DELETE FROM listings;";

    public static Connection getConnection() {
        Connection conn;
        boolean init = false;
        String url = path + File.separator + database;
        File db = new File(url);
        if (db.exists()) {
            if (FileUtil.backupFile(db)) {
                MessageUtil.logWarning("A backup of the database has been saved with .backup extension.");
            } else {
                MessageUtil.logSevere("Error! Could not backup database file");
            }
        } else {
            init = true;
        }
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + url);
            if (init) {
                AuctionHouse.logger.info("Created database");
            }
            try {
                Statement stmt = conn.createStatement();
                stmt.execute(set_pragma);
                stmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                Statement stmt2 = conn.createStatement();
                ResultSet rs2 = stmt2.executeQuery(check_table);
                if (!rs2.next()) {
                    try (Statement stmt3 = conn.createStatement();){
                        stmt3.executeUpdate(create_table);
                        AuctionHouse.logger.info("Created tables");
                    }
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                stmt2.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                Statement stmt4 = conn.createStatement();
                ResultSet rs4 = stmt4.executeQuery(check_table_for_type);
                int count = 0;
                while (rs4.next()) {
                    if (!rs4.getString("name").equalsIgnoreCase("listing_type")) continue;
                    ++count;
                }
                if (count == 0) {
                    Statement stmt5 = conn.createStatement();
                    stmt5.executeUpdate(alter_table_for_type);
                    AuctionHouse.logger.info("Updated table");
                    stmt5.close();
                }
                stmt4.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                Statement stmt6 = conn.createStatement();
                if (stmt6.execute(vacuum)) {
                    AuctionHouse.logger.info("Packed database");
                }
                stmt6.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            conn = null;
        }
        return conn;
    }

    public static boolean createListing(Connection conn, long key, Listing value) {
        String item_string;
        String world = value.getWorld();
        String seller_uuid = value.getSeller_UUID() != null ? value.getSeller_UUID() : "";
        String buyer_uuid = value.getBuyer_UUID() != null ? value.getBuyer_UUID() : "";
        String bidder_uuid = value.getBidder_UUID() != null ? value.getBidder_UUID() : "";
        float price = value.getPrice();
        float reserve = value.getReserve();
        float bid = value.getBid();
        ListingType type = value.getType();
        String listing_type = type.name();
        ItemStack item = value.getItem();
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.set("item", (Object)item);
            item_string = config.saveToString();
        }
        catch (YAMLException e) {
            String seller = type.isServer() ? AuctionHouse.servername : (value.getSeller() != null ? value.getSeller().getName() : value.getSeller_UUID());
            MessageUtil.logWarning(String.format("Warning! - A YAML exception occurred while %s tried to list a %s", seller, item.getType().getKey().getKey()));
            MessageUtil.logWarning("This may be the result of a serialization bug or an attempted exploit by using a hacked client.");
            if (Config.debug) {
                e.printStackTrace();
            }
            return false;
        }
        byte[] item_blob = item_string.getBytes(StandardCharsets.UTF_8);
        boolean result = false;
        boolean exists = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(String.format(check_row, key));){
            exists = rs.next();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        if (exists) {
            MessageUtil.logWarning("Record already exists!");
        } else {
            try (PreparedStatement prep = conn.prepareStatement(insert_row);){
                conn.setAutoCommit(true);
                prep.setLong(1, key);
                prep.setString(2, world);
                prep.setString(3, seller_uuid);
                prep.setString(4, buyer_uuid);
                prep.setString(5, bidder_uuid);
                prep.setFloat(6, price);
                prep.setFloat(7, reserve);
                prep.setFloat(8, bid);
                prep.setString(9, listing_type);
                prep.setBytes(10, item_blob);
                prep.executeUpdate();
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("Created listing %d: (item = %dx %s, world = %s, seller = %s, buyer = %s, price = %.2f, reserve = %.2f, bid = %.2f, listing_type = %s)", key, item.getAmount(), item.getType().name(), world, seller_uuid, buyer_uuid, Float.valueOf(price), Float.valueOf(reserve), Float.valueOf(bid), listing_type));
                }
                result = true;
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void readAllListings(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(select_all_rows);
            while (rs.next()) {
                long id = rs.getLong("id");
                String world = rs.getString("world");
                String seller_uuid = rs.getString("seller_uuid");
                String buyer_uuid = rs.getString("buyer_uuid");
                String bidder_uuid = rs.getString("bidder_uuid");
                float price = rs.getFloat("price");
                float reserve = rs.getFloat("reserve");
                float bid = rs.getFloat("bid");
                String listing_type = rs.getString("listing_type");
                ListingType type = listing_type != null && !listing_type.isEmpty() ? ListingType.valueOf(listing_type) : ListingType.PLAYER_LISTING;
                byte[] item_blob = rs.getBytes("item");
                String item_string = new String(item_blob, StandardCharsets.UTF_8);
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("read item = %s", item_string));
                    Pattern p = Pattern.compile("internal: .*");
                    Matcher m = p.matcher(item_string);
                    if (m.find()) {
                        String tag = m.group().replace("internal: ", "");
                        AuctionHouse.logger.info(String.format("tag = %s", AuctionHouse.nms.parseInternal(tag)));
                    }
                }
                YamlConfiguration config = new YamlConfiguration();
                ItemStack item = null;
                try {
                    config.loadFromString(item_string);
                    item = config.getItemStack("item", null);
                }
                catch (InvalidConfigurationException e) {
                    e.printStackTrace();
                }
                if (item == null) continue;
                Listing listing = new Listing(world, seller_uuid, buyer_uuid, bidder_uuid, price, reserve, bid, type, item);
                AuctionHouse.listings.putListing(listing, id);
                if (!Config.debug) continue;
                AuctionHouse.logger.info(String.format("Read listing %d: (item = %dx %s, world = %s, seller = %s, buyer = %s, price = %.2f, reserve = %.2f, bid = %.2f, listing_type = %s)", id, item.getAmount(), item.getType().getKey(), world, seller_uuid, buyer_uuid, Float.valueOf(price), Float.valueOf(reserve), Float.valueOf(bid), listing_type));
            }
            stmt.close();
            int count = AuctionHouse.listings.count();
            AuctionHouse.logger.info(String.format("Loaded %d listing%s", count, count != 1 ? "s" : ""));
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateListing(Connection conn, long key, long id, Listing value) {
        String world = value.getWorld();
        String seller_uuid = value.getSeller_UUID() != null ? value.getSeller_UUID() : "";
        String buyer_uuid = value.getBuyer_UUID() != null ? value.getBuyer_UUID() : "";
        String bidder_uuid = value.getBidder_UUID() != null ? value.getBidder_UUID() : "";
        float price = value.getPrice();
        float reserve = value.getReserve();
        float bid = value.getBid();
        ListingType type = value.getType();
        String listing_type = type.name();
        ItemStack item = value.getItem();
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", (Object)item);
        String item_string = config.saveToString();
        byte[] item_blob = item_string.getBytes(StandardCharsets.UTF_8);
        String query = String.format(check_row, key);
        boolean exists = false;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            exists = rs.next();
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        if (!exists) {
            MessageUtil.logWarning("Could not find record!");
        } else {
            try (PreparedStatement prep = conn.prepareStatement(update_row);){
                conn.setAutoCommit(true);
                prep.setLong(1, id);
                prep.setString(2, world);
                prep.setString(3, seller_uuid);
                prep.setString(4, buyer_uuid);
                prep.setString(5, bidder_uuid);
                prep.setFloat(6, price);
                prep.setFloat(7, reserve);
                prep.setFloat(8, bid);
                prep.setString(9, listing_type);
                prep.setBytes(10, item_blob);
                prep.setLong(11, key);
                prep.executeUpdate();
                if (Config.debug) {
                    AuctionHouse.logger.info(String.format("Updated listing %d: (item = %dx %s, world = %s, seller = %s, buyer = %s, price = %.2f, reserve = %.2f, bid = %.2f, listing_type = %s)", key, item.getAmount(), item.getType().getKey(), world, seller_uuid, buyer_uuid, Float.valueOf(price), Float.valueOf(reserve), Float.valueOf(bid), listing_type));
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteListing(Connection conn, long key) {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(String.format(delete_row, key));
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteListings(Connection conn, List<Long> keys) {
        try {
            Statement stmt = conn.createStatement();
            for (long key : keys) {
                stmt.executeUpdate(String.format(delete_row, key));
            }
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllListings(Connection conn) {
        if (Config.debug) {
            AuctionHouse.logger.info("deleteAllListings()");
        }
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(delete_all_rows);
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

