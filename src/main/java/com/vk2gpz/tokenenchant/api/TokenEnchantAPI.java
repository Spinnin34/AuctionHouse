package com.vk2gpz.tokenenchant.api;

import org.bukkit.OfflinePlayer;

/** Stub for compilation - provided by TokenEnchant plugin at runtime. */
public class TokenEnchantAPI {
    private static final TokenEnchantAPI instance = new TokenEnchantAPI();

    public static TokenEnchantAPI getInstance() { return instance; }
    public double getTokens(OfflinePlayer player) { return 0.0; }
    public void removeTokens(OfflinePlayer player, double amount) {}
    public void addTokens(OfflinePlayer player, double amount) {}
}

