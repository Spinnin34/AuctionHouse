package com.spawnchunk.auctionhouse.storage;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.modules.Listing;
import com.spawnchunk.auctionhouse.modules.ListingType;
import com.spawnchunk.auctionhouse.util.ItemUtil;
import com.spawnchunk.auctionhouse.util.TimeUtil;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.inventory.ItemStack;

public class ImportDBFile {
    static String path = AuctionHouse.plugin.getDataFolder().getAbsolutePath();
    static String check_table_listings = "SELECT name FROM sqlite_master WHERE type='table' AND name='listings';";
    static String check_table_expired = "SELECT name FROM sqlite_master WHERE type='table' AND name='expired';";
    static String check_table_sell_log = "SELECT name FROM sqlite_master WHERE type='table' AND name='sell_log';";
    static String select_all_listings = "SELECT * FROM listings;";
    static String select_all_expired = "SELECT * FROM expired;";
    static String select_all_sell_log = "SELECT * FROM sell_log;";

    public static void importData() {
        String url = path + File.separator + "data.db";
        String url2 = path + File.separator + "data.db.old";
        File db = new File(url);
        if (db.exists()) {
            AuctionHouse.logger.info("Found existing data.db, attempting import...");
            try {
                boolean result;
                Listing listing;
                ItemStack item;
                String listing_type;
                ListingType type;
                long id;
                float price;
                float bid;
                float reserve;
                String bidder_uuid;
                String buyer_uuid;
                String seller_uuid;
                String world;
                String uuid;
                int error;
                int count;
                Connection conn = DriverManager.getConnection("jdbc:sqlite:" + url);
                try {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(check_table_listings);
                    if (rs.next()) {
                        try {
                            Statement stmt2 = conn.createStatement();
                            ResultSet rs2 = stmt2.executeQuery(select_all_listings);
                            count = 0;
                            error = 0;
                            while (rs2.next()) {
                                uuid = rs2.getString("id");
                                if (uuid == null) continue;
                                world = AuctionHouse.levelname;
                                seller_uuid = rs2.getString("listed_by");
                                buyer_uuid = "";
                                bidder_uuid = "";
                                reserve = 0.0f;
                                bid = 0.0f;
                                price = rs2.getLong("price");
                                id = rs2.getLong("end_time");
                                type = ListingType.PLAYER_LISTING;
                                listing_type = type.name();
                                item = AuctionHouse.nms.deserialize(rs2.getString("item"));
                                listing = new Listing(world, seller_uuid, buyer_uuid, bidder_uuid, price, reserve, bid, type, item);
                                result = AuctionHouse.listings.importListing(listing, id);
                                if (result) {
                                    ++count;
                                } else {
                                    ++error;
                                }
                                AuctionHouse.logger.info(String.format("Read listing %d: (name = %s, item = %dx %s, lore = %s, enchants = %s, world = %s, seller = %s, buyer = %s, price = %.2f, reserve = %.2f, bid = %.2f, listing_type = %s)", id, ItemUtil.getName(item), item.getAmount(), item.getType().getKey(), ItemUtil.getLoreString(item), ItemUtil.getEnchantsString(item), world, seller_uuid, buyer_uuid, Float.valueOf(price), Float.valueOf(reserve), Float.valueOf(bid), listing_type));
                            }
                            stmt2.close();
                            AuctionHouse.logger.info(String.format("Imported %d listing%s", count, count != 1 ? "s" : ""));
                            if (error > 0) {
                                AuctionHouse.logger.info(String.format("Could not read %d listing%s", error, error != 1 ? "s" : ""));
                            }
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    stmt.close();
                }
                catch (SQLException stmt) {
                    // empty catch block
                }
                try {
                    Statement stmt3 = conn.createStatement();
                    ResultSet rs3 = stmt3.executeQuery(check_table_expired);
                    if (rs3.next()) {
                        try {
                            Statement stmt4 = conn.createStatement();
                            ResultSet rs4 = stmt4.executeQuery(select_all_expired);
                            count = 0;
                            error = 0;
                            while (rs4.next()) {
                                uuid = rs4.getString("id");
                                if (uuid == null) continue;
                                world = AuctionHouse.levelname;
                                seller_uuid = rs4.getString("player");
                                buyer_uuid = "";
                                bidder_uuid = "";
                                reserve = 0.0f;
                                bid = 0.0f;
                                price = 0.0f;
                                id = TimeUtil.now();
                                type = ListingType.PLAYER_LISTING;
                                listing_type = type.name();
                                item = AuctionHouse.nms.deserialize(rs4.getString("item"));
                                listing = new Listing(world, seller_uuid, buyer_uuid, bidder_uuid, price, reserve, bid, type, item);
                                result = AuctionHouse.listings.importListing(listing, id);
                                if (result) {
                                    ++count;
                                } else {
                                    ++error;
                                }
                                AuctionHouse.logger.info(String.format("Read expired listing %d: (name = %s, item = %dx %s, lore = %s, enchants = %s, world = %s, seller = %s, buyer = %s, price = %.2f, reserve = %.2f, bid = %.2f, listing_type = %s)", id, ItemUtil.getName(item), item.getAmount(), item.getType().getKey(), ItemUtil.getLoreString(item), ItemUtil.getEnchantsString(item), world, seller_uuid, buyer_uuid, Float.valueOf(price), Float.valueOf(reserve), Float.valueOf(bid), listing_type));
                            }
                            stmt4.close();
                            AuctionHouse.logger.info(String.format("Imported %d expired listing%s", count, count != 1 ? "s" : ""));
                            if (error > 0) {
                                AuctionHouse.logger.info(String.format("Could not read %d expired listing%s", error, error != 1 ? "s" : ""));
                            }
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    stmt3.close();
                }
                catch (SQLException stmt3) {
                    // empty catch block
                }
                try {
                    Statement stmt5 = conn.createStatement();
                    ResultSet rs5 = stmt5.executeQuery(check_table_sell_log);
                    if (rs5.next()) {
                        try {
                            Statement stmt6 = conn.createStatement();
                            ResultSet rs6 = stmt6.executeQuery(select_all_sell_log);
                            count = 0;
                            error = 0;
                            while (rs6.next()) {
                                ItemStack item2;
                                uuid = rs6.getString("id");
                                if (uuid == null) continue;
                                world = AuctionHouse.levelname;
                                seller_uuid = rs6.getString("seller");
                                buyer_uuid = rs6.getString("buyer");
                                bidder_uuid = "";
                                reserve = 0.0f;
                                bid = 0.0f;
                                long id2 = rs6.getLong("time");
                                ListingType type2 = ListingType.PLAYER_LISTING;
                                String listing_type2 = type2.name();
                                String item_string = rs6.getString("item");
                                float price2 = Float.parseFloat(item_string.substring(item_string.lastIndexOf(":") + 1, item_string.length() - 1));
                                Listing listing2 = new Listing(world, seller_uuid, buyer_uuid, bidder_uuid, price2, reserve, bid, type2, item2 = AuctionHouse.nms.deserialize(item_string.substring(0, item_string.lastIndexOf(":") - 1)));
                                boolean result2 = AuctionHouse.listings.importListing(listing2, id2);
                                if (result2) {
                                    ++count;
                                } else {
                                    ++error;
                                }
                                AuctionHouse.logger.info(String.format("Read sold item %d: (name = %s, item = %dx %s, lore = %s, enchants = %s, world = %s, seller = %s, buyer = %s, price = %.2f, reserve = %.2f, bid = %.2f, listing_type = %s)", id2, ItemUtil.getName(item2), item2.getAmount(), item2.getType().getKey(), ItemUtil.getLoreString(item2), ItemUtil.getEnchantsString(item2), world, seller_uuid, buyer_uuid, Float.valueOf(price2), Float.valueOf(reserve), Float.valueOf(bid), listing_type2));
                            }
                            stmt6.close();
                            AuctionHouse.logger.info(String.format("Imported %d sold item%s", count, count != 1 ? "s" : ""));
                            if (error > 0) {
                                AuctionHouse.logger.info(String.format("Could not read %d sold item%s", error, error != 1 ? "s" : ""));
                            }
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    stmt5.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            catch (SQLException e) {
                // empty catch block
            }
            File old = new File(url2);
            if (db.renameTo(old)) {
                AuctionHouse.logger.info("Renamed data.db to data.db.old");
            }
        }
    }
}

