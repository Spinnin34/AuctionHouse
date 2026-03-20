package com.spawnchunk.auctionhouse.modules;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

public class Economy {
    public static double getBalance(OfflinePlayer player, String world) {
        if (Config.economy.equalsIgnoreCase("vault")) {
            return AuctionHouse.econ.getBalance(player, world);
        }
        if (Config.economy.equalsIgnoreCase("tokenenchant")) {
            return AuctionHouse.te.getTokens(player);
        }
        return 0.0;
    }

    public static boolean withdrawPlayer(OfflinePlayer player, String world, double amount) {
        if (Config.economy.equalsIgnoreCase("vault")) {
            EconomyResponse withdrawResponse = AuctionHouse.econ.withdrawPlayer(player, world, amount);
            if (withdrawResponse != null && withdrawResponse.transactionSuccess()) {
                return true;
            }
            if (Config.debug) {
                AuctionHouse.logger.info("Warning! Vault could not withdraw from the buyer");
                if (withdrawResponse == null) {
                    AuctionHouse.logger.info("Economy plugin response is null!");
                } else {
                    AuctionHouse.logger.info(String.format("Economy plugin response type %s", withdrawResponse.type.toString()));
                    AuctionHouse.logger.info(String.format("Economy plugin response message %s", withdrawResponse.errorMessage));
                }
            }
            return false;
        }
        if (Config.economy.equalsIgnoreCase("tokenenchant")) {
            double tokens = AuctionHouse.te.getTokens(player);
            if (tokens >= amount) {
                AuctionHouse.te.removeTokens(player, amount);
                return true;
            }
            return false;
        }
        return false;
    }

    public static boolean depositPlayer(OfflinePlayer player, String world, double amount) {
        if (Config.economy.equalsIgnoreCase("vault")) {
            EconomyResponse depositResponse = AuctionHouse.econ.depositPlayer(player, world, amount);
            if (depositResponse != null && depositResponse.transactionSuccess()) {
                return true;
            }
            if (Config.debug) {
                AuctionHouse.logger.info("Warning! Vault could not deposit to the seller");
                if (depositResponse == null) {
                    AuctionHouse.logger.info("Economy plugin response is null!");
                } else {
                    AuctionHouse.logger.info(String.format("Economy plugin response type %s", depositResponse.type.toString()));
                    AuctionHouse.logger.info(String.format("Economy plugin response message %s", depositResponse.errorMessage));
                }
            }
            return false;
        }
        if (Config.economy.equalsIgnoreCase("tokenenchant")) {
            AuctionHouse.te.addTokens(player, amount);
            return true;
        }
        return false;
    }
}

