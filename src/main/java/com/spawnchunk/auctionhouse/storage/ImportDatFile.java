package com.spawnchunk.auctionhouse.storage;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.modules.Listing;
import com.spawnchunk.auctionhouse.storage.AuctionsStorage;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class ImportDatFile {
    public static void importData() {
        ArrayList<String> validWorlds = new ArrayList<String>();
        for (World world : Bukkit.getServer().getWorlds()) {
            validWorlds.add(world.getName());
        }
        int count = 0;
        int error = 0;
        String path = AuctionHouse.plugin.getDataFolder().getPath();
        String url = path + File.separator + "auctions.dat";
        String url2 = path + File.separator + "auctions.dat.old";
        File dat = new File(url);
        if (dat.exists()) {
            File old;
            AuctionHouse.logger.info("Found existing auctions.dat, attempting import...");
            TreeMap<Long, String> map = AuctionsStorage.loadAuctionsFile();
            if (map == null) {
                return;
            }
            for (Long id : map.keySet()) {
                boolean result;
                Listing listing = new Listing(map.get(id));
                ItemStack item = listing.getItem();
                if (item == null) continue;
                item = AuctionHouse.nms.updateItem(item);
                listing.setItem(item);
                if (item.getType().equals((Object)Material.CAVE_AIR) || item.getType().equals((Object)Material.AIR) || item.getType().equals((Object)Material.VOID_AIR)) continue;
                if (!validWorlds.contains(listing.getWorld())) {
                    listing.setWorld(((World)Bukkit.getServer().getWorlds().get(0)).getName());
                }
                if (result = AuctionHouse.listings.importListing(listing, id)) {
                    ++count;
                    continue;
                }
                ++error;
            }
            AuctionHouse.logger.info(String.format("Imported %d listing%s", count, count != 1 ? "s" : ""));
            if (error > 0) {
                AuctionHouse.logger.info(String.format("Could not read %d listing%s", error, error != 1 ? "s" : ""));
            }
            if (dat.renameTo(old = new File(url2))) {
                AuctionHouse.logger.info("Renamed auctions.dat to auctions.dat.old");
            }
        }
    }
}

