package com.spawnchunk.auctionhouse.util;

import java.io.File;

public class WorldUtil {
    public static String getWorldPrefix(String world) {
        return world.split("_")[0];
    }

    public static String getMainWorld() {
        File f = new File("server.properties");
        return PropertyUtil.getProperty(f, "level-name");
    }
}

