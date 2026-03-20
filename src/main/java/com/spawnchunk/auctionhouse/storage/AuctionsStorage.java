package com.spawnchunk.auctionhouse.storage;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.TreeMap;

public class AuctionsStorage {
    public static TreeMap<Long, String> loadAuctionsFile() {
        File fn;
        TreeMap map = new TreeMap();
        String path = AuctionHouse.plugin.getDataFolder().getPath();
        String fname = path + File.separator + "auctions.dat";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!(fn = new File(fname)).exists()) {
            try {
                fn.createNewFile();
            }
            catch (Exception e) {
                MessageUtil.logSevere("Error! Could not create the data file!");
                e.printStackTrace();
                return null;
            }
        }
        try {
            FileInputStream fis = new FileInputStream(fname);
            if (fis.available() > 0) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    map = (TreeMap)ois.readObject();
                    ois.close();
                }
                catch (Exception e) {
                    MessageUtil.logSevere("Error! Could not read the data file!");
                    e.printStackTrace();
                    return null;
                }
            }
            fis.close();
            AuctionHouse.logger.info(String.format("Loaded %d listings", map != null ? map.size() : 0));
        }
        catch (Exception e) {
            MessageUtil.logSevere("Error! Could not load the data file!");
            e.printStackTrace();
            return null;
        }
        return map;
    }
}

