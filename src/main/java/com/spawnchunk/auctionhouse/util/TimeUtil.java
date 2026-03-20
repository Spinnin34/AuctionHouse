package com.spawnchunk.auctionhouse.util;

import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.storage.LocaleStorage;

public class TimeUtil {
    private static final long second = 1000L;
    private static final long minute = 60000L;
    private static final long hour = 3600000L;
    private static final long day = 86400000L;
    private static long last = System.currentTimeMillis();

    public static long now() {
        long timestamp = System.currentTimeMillis();
        if (timestamp == last) {
            ++timestamp;
        }
        last = timestamp;
        return timestamp;
    }

    private static int days(long t) {
        long duration = Math.abs(t);
        return (int)Math.floorDiv(duration, 86400000L);
    }

    private static int hours(long t) {
        long duration = Math.abs(t);
        long hours = Math.floorMod(duration, 86400000L);
        return (int)Math.floorDiv(hours, 3600000L);
    }

    private static int minutes(long t) {
        long duration = Math.abs(t);
        long hours = Math.floorMod(duration, 86400000L);
        long minutes = Math.floorMod(hours, 3600000L);
        return (int)Math.floorDiv(minutes, 60000L);
    }

    private static int seconds(long t) {
        long duration = Math.abs(t);
        long hours = Math.floorMod(duration, 86400000L);
        long minutes = Math.floorMod(hours, 3600000L);
        long seconds = Math.floorMod(minutes, 60000L);
        return (int)Math.floorDiv(seconds, 1000L);
    }

    public static long duration(int days, int hours, int minutes, int seconds) {
        return (long)days * 86400000L + (long)hours * 3600000L + (long)minutes * 60000L + (long)seconds * 1000L;
    }

    public static String duration(long t, boolean flag) {
        long duration = Math.abs(t);
        int d = TimeUtil.days(duration);
        int h = TimeUtil.hours(duration);
        int m = TimeUtil.minutes(duration);
        int s = TimeUtil.seconds(duration);
        String days = LocaleStorage.translate("duration.days", Config.locale);
        String hours = LocaleStorage.translate("duration.hours", Config.locale);
        String minutes = LocaleStorage.translate("duration.minutes", Config.locale);
        String seconds = LocaleStorage.translate("duration.seconds", Config.locale);
        if (d > 0) {
            return flag ? String.format("%d%s %d%s %d%s %d%s", d, days, h, hours, m, minutes, s, seconds) : String.format("%d%s %d%s %d%s", d, days, h, hours, m, minutes);
        }
        if (h > 0) {
            return flag ? String.format("%d%s %d%s %d%s", h, hours, m, minutes, s, seconds) : String.format("%d%s %d%s", h, hours, m, minutes);
        }
        if (m > 0) {
            return flag ? String.format("%d%s %d%s", m, minutes, s, seconds) : String.format("%d%s", m, minutes);
        }
        if (s > 0) {
            return flag ? String.format("%d%s", s, seconds) : String.format("<1%s", minutes);
        }
        return LocaleStorage.translate("duration.now", Config.locale);
    }
}

