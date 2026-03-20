package com.spawnchunk.auctionhouse.storage;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.util.FileUtil;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class LocaleStorage {
    private static final int locale_version = 35;

    public static void reloadLocales() {
        AuctionHouse.locales.clear();
        LocaleStorage.loadLocales();
    }

    public static void loadLocales() {
        LocaleStorage.installLocaleFile();
        List<String> jsonFiles = LocaleStorage.listJsonFiles();
        for (String filename : jsonFiles) {
            String locale = LocaleStorage.containsLocale(filename);
            if (locale.isEmpty()) continue;
            TreeMap<String, String> map = LocaleStorage.loadLocaleFile(filename);
            AuctionHouse.locales.put(locale, map);
        }
    }

    private static void installLocaleFile() {
        File fn;
        String path = AuctionHouse.plugin.getDataFolder().getPath();
        String filename = "en_us.json";
        String fname = path + File.separator + filename;
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!(fn = new File(fname)).exists()) {
            AuctionHouse.plugin.saveResource(filename, false);
        } else if (LocaleStorage.isOutdated(filename)) {
            if (FileUtil.backupFile(fn)) {
                AuctionHouse.plugin.saveResource(filename, true);
                MessageUtil.logWarning(String.format("Warning! Locale file %s has been updated!", filename));
                MessageUtil.logWarning("A backup of the old file has been saved with .backup extension.");
                MessageUtil.logWarning("Please update the new file to include any customized translations.");
            } else {
                MessageUtil.logSevere("Error! Could not backup existing locale file");
            }
        }
    }

    private static List<String> listJsonFiles() {
        String[] list;
        List<String> files = new ArrayList<String>();
        String path = AuctionHouse.plugin.getDataFolder().getPath();
        File dir = new File(path);
        if (dir.exists() && (list = dir.list((file, name) -> name.toLowerCase().endsWith(".json"))) != null) {
            files = Arrays.asList(list);
        }
        return files;
    }

    private static boolean isOutdated(String filename) {
        String path = AuctionHouse.plugin.getDataFolder().getPath();
        String fname = path + File.separator + filename;
        File fn = new File(fname);
        if (fn.exists()) {
            JSONParser parser = new JSONParser();
            try {
                Object object = parser.parse((Reader)new InputStreamReader((InputStream)new FileInputStream(fname), StandardCharsets.UTF_8));
                JSONObject jsonObject = (JSONObject)object;
                String version = (String)jsonObject.get((Object)"version");
                if (version == null) {
                    return true;
                }
                if (Integer.parseInt(version) >= 35) {
                    return false;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private static String containsLocale(String filename) {
        String path = AuctionHouse.plugin.getDataFolder().getPath();
        String fname = path + File.separator + filename;
        File fn = new File(fname);
        if (fn.exists()) {
            JSONParser parser = new JSONParser();
            try {
                Object object = parser.parse((Reader)new InputStreamReader((InputStream)new FileInputStream(fname), StandardCharsets.UTF_8));
                JSONObject jsonObject = (JSONObject)object;
                String language = (String)jsonObject.get((Object)"language.name");
                String region = (String)jsonObject.get((Object)"language.region");
                String locale = (String)jsonObject.get((Object)"language.code");
                locale = locale.toLowerCase();
                if (!language.isEmpty() && !region.isEmpty() && locale.equalsIgnoreCase(filename.replace(".json", ""))) {
                    AuctionHouse.logger.info(String.format("Found locale file %s", filename));
                    return locale;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private static TreeMap<String, String> loadLocaleFile(String filename) {
        TreeMap<String, String> map = new TreeMap<String, String>();
        String path = AuctionHouse.plugin.getDataFolder().getPath();
        String fname = path + File.separator + filename;
        File fn = new File(fname);
        if (fn.exists()) {
            if (LocaleStorage.isOutdated(filename)) {
                MessageUtil.logWarning(String.format("Warning! Locale file %s is not the latest version!", filename));
                MessageUtil.logWarning("File should be updated to include and missing translations");
            }
            JSONParser parser = new JSONParser();
            try {
                Object object = parser.parse((Reader)new InputStreamReader((InputStream)new FileInputStream(fname), StandardCharsets.UTF_8));
                JSONObject jsonObject = (JSONObject)object;
                for (Object obj : jsonObject.keySet()) {
                    String key = (String)obj;
                    String value = (String)jsonObject.get((Object)key);
                    map.put(key, value);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static String translate(String key, String loc) {
        String fallback;
        if (AuctionHouse.locales.containsKey(loc)) {
            if (Config.translations.isEmpty()) {
                Config.translations = AuctionHouse.locales.get(loc);
            }
            if (Config.translations.containsKey(key)) {
                return Config.translations.get(key);
            }
            return String.format("<missing translation: %s>", key);
        }
        MessageUtil.logWarning(String.format("Locale %s not found! Please check that %s.json exists.", loc, loc));
        Config.locale = fallback = "en_us";
        if (Config.translations_fallback.isEmpty()) {
            Config.translations_fallback = AuctionHouse.locales.get(fallback);
        }
        if (Config.translations_fallback.containsKey(key)) {
            return Config.translations_fallback.get(key);
        }
        return String.format("<missing translation: %s>", key);
    }
}

