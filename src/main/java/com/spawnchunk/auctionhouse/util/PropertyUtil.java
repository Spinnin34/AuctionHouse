package com.spawnchunk.auctionhouse.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class PropertyUtil {
    PropertyUtil() {
    }

    public static String getProperty(File f, String property) {
        Properties pr = new Properties();
        try {
            FileInputStream in = new FileInputStream(f);
            pr.load(in);
            return pr.getProperty(property);
        }
        catch (IOException iOException) {
            return "";
        }
    }

    public static void setProperty(File f, String property, String value) {
        Properties pr = new Properties();
        try {
            FileInputStream in = new FileInputStream(f);
            pr.load(in);
            in.close();
            pr.setProperty(property, value);
            FileOutputStream out = new FileOutputStream(f);
            pr.store(out, "");
            out.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

