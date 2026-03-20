package com.spawnchunk.auctionhouse.commands;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.modules.Auctions;
import com.spawnchunk.auctionhouse.modules.ListingType;
import com.spawnchunk.auctionhouse.storage.LocaleStorage;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import com.spawnchunk.auctionhouse.util.PlayerUtil;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AHCommand
implements TabExecutor {
    private boolean isDisabledWorld(Player player) {
        return Config.disabled_worlds.contains(player.getWorld().getName());
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        boolean isOp;
        boolean isConsole = sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender;
        boolean isCommand = sender instanceof BlockCommandSender;
        boolean isPlayer = sender instanceof Player;
        Player player = isPlayer ? (Player)sender : null;
        UUID uuid = player != null ? player.getUniqueId() : null;
        boolean bl = isOp = sender instanceof Player && sender.isOp();
        if (isPlayer && player.isSleeping()) {
            MessageUtil.sendMessage(sender, "warning.command.sleeping", Config.locale);
            return true;
        }
        if (args.length == 0) {
            if (isPlayer) {
                if (player.hasPermission("auctionhouse.use") || isOp) {
                    if (this.isDisabledWorld(player)) {
                        MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                    } else {
                        Auctions.filter = null;
                        Auctions.menu_mode = false;
                        Auctions.openActiveListingsMenu(player);
                    }
                }
            } else {
                MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
            }
            return true;
        }
        String arg0 = args[0].trim();
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.reload", Config.locale))) {
            if (args.length == 1) {
                if (isCommand) {
                    MessageUtil.sendMessage(sender, "warning.command.deny", Config.locale);
                } else if (isPlayer && !player.hasPermission("auctionhouse.reload") && !isOp) {
                    MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
                } else {
                    AuctionHouse.menuManager.closeAllMenus();
                    Auctions.initializeMenus();
                    AuctionHouse.config.reloadConfig();
                    MessageUtil.sendMessage(sender, "message.command.reloaded", Config.locale);
                }
            } else {
                String syntax = String.format("ah %s", LocaleStorage.translate("command.reload", Config.locale));
                MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.clear_all_data", Config.locale))) {
            if (args.length == 1) {
                if (isConsole) {
                    Auctions.clearAllData(sender);
                } else {
                    MessageUtil.sendMessage(sender, "warning.command.console_only", Config.locale);
                }
            } else {
                String syntax = String.format("ah %s", LocaleStorage.translate("command.clear_all_data", Config.locale));
                MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.create_test_data", Config.locale))) {
            if (args.length == 1) {
                if (isConsole) {
                    Auctions.createTestData(sender, 450);
                } else {
                    MessageUtil.sendMessage(sender, "warning.command.console_only", Config.locale);
                }
            } else if (args.length == 2) {
                String arg1 = args[1].trim();
                try {
                    int entries = Config.numberFormat.parse(arg1).intValue();
                    Auctions.createTestData(sender, entries);
                }
                catch (NumberFormatException | ParseException ignored) {
                    String syntax = String.format("ah %s [%s]", LocaleStorage.translate("command.create_test_data", Config.locale), LocaleStorage.translate("parameter.count", Config.locale));
                    MessageUtil.sendMessage(sender, "warning.command.invalid_count", Config.locale, syntax);
                }
            } else {
                String syntax = String.format("ah %s [%s]", LocaleStorage.translate("command.create_test_data", Config.locale), LocaleStorage.translate("parameter.count", Config.locale));
                MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.debug", Config.locale))) {
            if (args.length == 1) {
                if (isConsole) {
                    Config.debug = !Config.debug;
                    MessageUtil.sendMessage(sender, "message.debug", Config.locale, Config.debug ? "true" : "false");
                } else {
                    MessageUtil.sendMessage(sender, "warning.command.console_only", Config.locale);
                }
            } else {
                String syntax = String.format("ah %s", LocaleStorage.translate("command.debug", Config.locale));
                MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.help", Config.locale))) {
            if (isPlayer && this.isDisabledWorld(player)) {
                MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
            } else if (args.length == 1) {
                this.showHelp(sender);
            } else {
                String syntax = String.format("ah %s", LocaleStorage.translate("command.help", Config.locale));
                MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.show", Config.locale))) {
            if (isPlayer && !player.hasPermission("auctionhouse.show") && !isOp) {
                MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
            } else if (args.length == 1) {
                if (isPlayer) {
                    Auctions.filter = null;
                    Auctions.menu_mode = false;
                    Auctions.openActiveListingsMenu(player);
                } else {
                    MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
                }
            } else if (args.length == 2) {
                String arg1 = args[1].trim();
                Player p = PlayerUtil.getPlayer(arg1);
                if (p != null && p.isOnline()) {
                    if (this.isDisabledWorld(p)) {
                        MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                        return true;
                    }
                    Auctions.filter = null;
                    Auctions.menu_mode = false;
                    Auctions.openActiveListingsMenu(p);
                } else {
                    String syntax = String.format("ah %s [%s]", LocaleStorage.translate("command.show", Config.locale), LocaleStorage.translate("parameter.player", Config.locale));
                    MessageUtil.sendMessage(sender, "warning.command.invalid_player", Config.locale, syntax);
                }
            } else {
                String syntax = String.format("ah %s [%s]", LocaleStorage.translate("command.show", Config.locale), LocaleStorage.translate("parameter.player", Config.locale));
                MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.menu", Config.locale))) {
            if (isPlayer && !player.hasPermission("auctionhouse.menu") && !isOp) {
                MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
            } else if (args.length == 1) {
                if (isPlayer) {
                    Auctions.filter = null;
                    Auctions.menu_mode = true;
                    Auctions.openActiveListingsMenu(player);
                } else {
                    MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
                }
            } else if (args.length == 2) {
                String arg1 = args[1].trim();
                Player p = PlayerUtil.getPlayer(arg1);
                if (p != null && p.isOnline()) {
                    if (this.isDisabledWorld(p)) {
                        MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                        return true;
                    }
                    Auctions.filter = null;
                    Auctions.menu_mode = true;
                    Auctions.openActiveListingsMenu(p);
                } else {
                    String syntax = String.format("ah %s [%s]", LocaleStorage.translate("command.menu", Config.locale), LocaleStorage.translate("parameter.player", Config.locale));
                    MessageUtil.sendMessage(sender, "warning.command.invalid_player", Config.locale, syntax);
                }
            } else {
                String syntax = String.format("ah %s [%s]", LocaleStorage.translate("command.menu", Config.locale), LocaleStorage.translate("parameter.player", Config.locale));
                MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.search", Config.locale))) {
            if (isPlayer) {
                if (this.isDisabledWorld(player)) {
                    MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                } else if (player.hasPermission("auctionhouse.search") || isOp) {
                    if (args.length > 1) {
                        Auctions.filter = StringUtils.join(Arrays.asList(Arrays.copyOfRange(args, 1, args.length)), (String)" ");
                        Auctions.menu_mode = false;
                        Auctions.openActiveListingsMenu(player);
                    } else {
                        String syntax = String.format("ah %s <%s>...", LocaleStorage.translate("command.search", Config.locale), LocaleStorage.translate("parameter.keyword", Config.locale));
                        MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
                    }
                }
            } else {
                MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.sell", Config.locale))) {
            if (isPlayer) {
                if (this.isDisabledWorld(player)) {
                    MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                } else if (player.hasPermission("auctionhouse.sell") || isOp) {
                    if (args.length > 1 && args.length < 4) {
                        float price;
                        if (Auctions.lock.contains(uuid)) {
                            MessageUtil.sendMessage(sender, "warning.command.in_progress", Config.locale);
                            return true;
                        }
                        String arg1 = args[1].trim();
                        try {
                            price = Config.numberFormat.parse(arg1).floatValue();
                            price = Config.integer_price ? (float)Math.round(price) : price;
                        }
                        catch (NumberFormatException | ParseException ignored) {
                            String syntax = String.format("ah %s <%s> [%s]", LocaleStorage.translate("command.sell", Config.locale), LocaleStorage.translate("parameter.price", Config.locale), LocaleStorage.translate("parameter.count", Config.locale));
                            MessageUtil.sendMessage(sender, "warning.command.invalid_price", Config.locale, syntax);
                            return true;
                        }
                        if (args.length == 2) {
                            Auctions.sellItemInHand(player, price, ListingType.PLAYER_LISTING);
                        } else {
                            int count;
                            String arg2 = args[2].trim();
                            try {
                                count = Config.numberFormat.parse(arg2).intValue();
                            }
                            catch (NumberFormatException | ParseException ignored) {
                                String syntax = String.format("ah %s <%s> [%s]", LocaleStorage.translate("command.sell", Config.locale), LocaleStorage.translate("parameter.price", Config.locale), LocaleStorage.translate("parameter.count", Config.locale));
                                MessageUtil.sendMessage(sender, "warning.command.invalid_count", Config.locale, syntax);
                                return true;
                            }
                            Auctions.sellItemInHand(player, price, count, ListingType.PLAYER_LISTING);
                        }
                    } else {
                        String syntax = String.format("ah %s <%s> [%s]", LocaleStorage.translate("command.sell", Config.locale), LocaleStorage.translate("parameter.price", Config.locale), LocaleStorage.translate("parameter.count", Config.locale));
                        MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
                    }
                } else {
                    MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
                }
            } else {
                MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.list", Config.locale))) {
            if (isPlayer) {
                if (this.isDisabledWorld(player)) {
                    MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                } else if (player.hasPermission("auctionhouse.list") || isOp) {
                    if (args.length > 1 && args.length < 4) {
                        float price;
                        if (Auctions.lock.contains(uuid)) {
                            MessageUtil.sendMessage(sender, "warning.command.in_progress", Config.locale);
                            return true;
                        }
                        String arg1 = args[1].trim();
                        try {
                            price = Config.numberFormat.parse(arg1).floatValue();
                            price = Config.integer_price ? (float)Math.round(price) : price;
                        }
                        catch (NumberFormatException | ParseException ignored) {
                            String syntax = String.format("ah %s <%s> [%s]", LocaleStorage.translate("command.list", Config.locale), LocaleStorage.translate("parameter.price", Config.locale), LocaleStorage.translate("parameter.count", Config.locale));
                            MessageUtil.sendMessage(sender, "warning.command.invalid_price", Config.locale, syntax);
                            return true;
                        }
                        if (args.length == 2) {
                            Auctions.sellItemInHand(player, price, ListingType.SERVER_LISTING);
                        } else {
                            int count;
                            String arg2 = args[2].trim();
                            try {
                                count = Config.numberFormat.parse(arg2).intValue();
                            }
                            catch (NumberFormatException | ParseException ignored) {
                                String syntax = String.format("ah %s <%s> [%s]", LocaleStorage.translate("command.list", Config.locale), LocaleStorage.translate("parameter.price", Config.locale), LocaleStorage.translate("parameter.count", Config.locale));
                                MessageUtil.sendMessage(sender, "warning.command.invalid_count", Config.locale, syntax);
                                return true;
                            }
                            Auctions.sellItemInHand(player, price, count, ListingType.SERVER_LISTING);
                        }
                    } else {
                        String syntax = String.format("ah %s <%s> [%s]", LocaleStorage.translate("command.list", Config.locale), LocaleStorage.translate("parameter.price", Config.locale), LocaleStorage.translate("parameter.count", Config.locale));
                        MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
                    }
                } else {
                    MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
                }
            } else {
                MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.ulist", Config.locale))) {
            if (isPlayer) {
                if (this.isDisabledWorld(player)) {
                    MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                } else if (player.hasPermission("auctionhouse.ulist") || isOp) {
                    if (args.length > 1 && args.length < 4) {
                        float price;
                        if (Auctions.lock.contains(uuid)) {
                            MessageUtil.sendMessage(sender, "warning.command.in_progress", Config.locale);
                            return true;
                        }
                        String arg1 = args[1].trim();
                        try {
                            price = Config.numberFormat.parse(arg1).floatValue();
                            price = Config.integer_price ? (float)Math.round(price) : price;
                        }
                        catch (NumberFormatException | ParseException ignored) {
                            String syntax = String.format("ah %s <%s> [%s]", LocaleStorage.translate("command.ulist", Config.locale), LocaleStorage.translate("parameter.price", Config.locale), LocaleStorage.translate("parameter.count", Config.locale));
                            MessageUtil.sendMessage(sender, "warning.command.invalid_price", Config.locale, syntax);
                            return true;
                        }
                        if (args.length == 2) {
                            Auctions.sellItemInHand(player, price, ListingType.SERVER_LISTING_UNLIMITED);
                        } else {
                            int count;
                            String arg2 = args[2].trim();
                            try {
                                count = Config.numberFormat.parse(arg2).intValue();
                            }
                            catch (NumberFormatException | ParseException ignored) {
                                String syntax = String.format("ah %s <%s> [%s]", LocaleStorage.translate("command.ulist", Config.locale), LocaleStorage.translate("parameter.price", Config.locale), LocaleStorage.translate("parameter.count", Config.locale));
                                MessageUtil.sendMessage(sender, "warning.command.invalid_count", Config.locale, syntax);
                                return true;
                            }
                            Auctions.sellItemInHand(player, price, count, ListingType.SERVER_LISTING_UNLIMITED);
                        }
                    } else {
                        String syntax = String.format("ah %s <%s> [%s]", LocaleStorage.translate("command.ulist", Config.locale), LocaleStorage.translate("parameter.price", Config.locale), LocaleStorage.translate("parameter.count", Config.locale));
                        MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
                    }
                } else {
                    MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
                }
            } else {
                MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.selling", Config.locale))) {
            if (isPlayer) {
                if (this.isDisabledWorld(player)) {
                    MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                } else if (player.hasPermission("auctionhouse.selling") || isOp) {
                    if (args.length == 1) {
                        Auctions.openPlayerListingsMenu(player);
                    } else {
                        String syntax = String.format("ah %s", LocaleStorage.translate("command.selling", Config.locale));
                        MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
                    }
                } else {
                    MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
                }
            } else {
                MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.sold", Config.locale))) {
            if (isPlayer) {
                if (this.isDisabledWorld(player)) {
                    MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                } else if (player.hasPermission("auctionhouse.sold") || isOp) {
                    if (args.length == 1) {
                        Auctions.openSoldItemsMenu(player);
                    } else {
                        String syntax = String.format("ah %s", LocaleStorage.translate("command.sold", Config.locale));
                        MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
                    }
                } else {
                    MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
                }
            } else {
                MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.expired", Config.locale))) {
            if (isPlayer) {
                if (this.isDisabledWorld(player)) {
                    MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                } else if (player.hasPermission("auctionhouse.expired") || isOp) {
                    if (args.length == 1) {
                        Auctions.openExpiredListingsMenu(player);
                    } else {
                        String syntax = String.format("ah %s", LocaleStorage.translate("command.expired", Config.locale));
                        MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
                    }
                } else {
                    MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
                }
            } else {
                MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.cancel", Config.locale))) {
            if (isPlayer) {
                if (this.isDisabledWorld(player)) {
                    MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                } else if (player.hasPermission("auctionhouse.cancel") || isOp) {
                    if (args.length == 1) {
                        Auctions.cancelAllItems(player);
                    } else {
                        String syntax = String.format("ah %s", LocaleStorage.translate("command.cancel", Config.locale));
                        MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
                    }
                } else {
                    MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
                }
            } else {
                MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.return", Config.locale))) {
            if (isPlayer) {
                if (this.isDisabledWorld(player)) {
                    MessageUtil.sendMessage(sender, "warning.command.disabled_world", Config.locale);
                } else if (player.hasPermission("auctionhouse.return") || isOp) {
                    if (args.length == 1) {
                        Auctions.returnAllItems(player);
                    } else {
                        String syntax = String.format("ah %s", LocaleStorage.translate("command.return", Config.locale));
                        MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
                    }
                } else {
                    MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
                }
            } else {
                MessageUtil.sendMessage(sender, "warning.command.player_only", Config.locale);
            }
            return true;
        }
        if (arg0.equalsIgnoreCase(LocaleStorage.translate("command.purge", Config.locale))) {
            if (isPlayer && !player.hasPermission("auctionhouse.purge") && !isOp) {
                MessageUtil.sendMessage(player, "warning.permission.deny", Config.locale);
            } else if (args.length == 2) {
                String arg1 = args[1].trim();
                OfflinePlayer p = PlayerUtil.getOfflinePlayer(arg1);
                if (p != null) {
                    int count = Auctions.purgeAllItems(p);
                    MessageUtil.sendMessage(sender, "message.purge.listings", Config.locale, count, p.getName());
                } else {
                    String syntax = String.format("ah %s <%s>", LocaleStorage.translate("command.purge", Config.locale), LocaleStorage.translate("parameter.player", Config.locale));
                    MessageUtil.sendMessage(sender, "warning.command.invalid_player", Config.locale, syntax);
                }
            } else {
                String syntax = String.format("ah %s <%s>", LocaleStorage.translate("command.purge", Config.locale), LocaleStorage.translate("parameter.player", Config.locale));
                MessageUtil.sendMessage(sender, "warning.command.invalid_syntax", Config.locale, syntax);
            }
            return true;
        }
        MessageUtil.sendMessage(sender, "warning.command.unknown", Config.locale);
        return true;
    }

    private void showHelp(CommandSender sender) {
        if (sender instanceof Player) {
            Player p = (Player)sender;
            boolean isOp = p.isOp();
            if (p.hasPermission("auctionhouse.use") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah", Config.locale);
            }
            if (p.hasPermission("auctionhouse.cancel") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_cancel", Config.locale);
            }
            if (p.hasPermission("auctionhouse.expired") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_expired", Config.locale);
            }
            if (p.hasPermission("auctionhouse.list") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_list", Config.locale);
            }
            if (p.hasPermission("auctionhouse.menu") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_menu", Config.locale);
            }
            if (p.hasPermission("auctionhouse.reload") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_reload", Config.locale);
            }
            if (p.hasPermission("auctionhouse.return") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_return", Config.locale);
            }
            if (p.hasPermission("auctionhouse.search") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_search", Config.locale);
            }
            if (p.hasPermission("auctionhouse.sell") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_sell", Config.locale);
            }
            if (p.hasPermission("auctionhouse.selling") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_selling", Config.locale);
            }
            if (p.hasPermission("auctionhouse.show") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_show", Config.locale);
            }
            if (p.hasPermission("auctionhouse.sold") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_sold", Config.locale);
            }
            if (p.hasPermission("auctionhouse.ulist") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_ulist", Config.locale);
            }
            if (p.hasPermission("auctionhouse.purge") || isOp) {
                MessageUtil.sendMessage(sender, "message.help.ah_purge", Config.locale);
            }
        } else {
            MessageUtil.sendMessage(sender, "message.help.ah_menu", Config.locale);
            MessageUtil.sendMessage(sender, "message.help.ah_show", Config.locale);
            if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
                MessageUtil.sendMessage(sender, "message.help.ah_reload", Config.locale);
            }
        }
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!alias.equalsIgnoreCase("ah") && !alias.equals("/ah")) {
            return null;
        }
        Object buffer = "";
        for (String s : args) {
            buffer = ((String)buffer).isEmpty() ? s : (String)buffer + " " + s;
        }
        LinkedHashMap<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        List<Player> players;
        Object cmd = "";
        if (sender instanceof Player) {
            if (((String)(buffer = "/ah " + (String)buffer)).matches("/ah list [0-9]+ ")) {
                cmd = buffer;
                map.put("[" + LocaleStorage.translate("parameter.count", Config.locale) + "]", Arrays.asList("auctionhouse.list", "true"));
            } else if (((String)buffer).matches("/ah sell [0-9.]+ ")) {
                cmd = buffer;
                map.put("[" + LocaleStorage.translate("parameter.count", Config.locale) + "]", Arrays.asList("auctionhouse.sell", "true"));
            } else if (((String)buffer).matches("/ah ulist [0-9]+ ")) {
                cmd = buffer;
                map.put("[" + LocaleStorage.translate("parameter.count", Config.locale) + "]", Arrays.asList("auctionhouse.ulist", "true"));
            } else if (((String)buffer).equals("/ah list ")) {
                cmd = buffer;
                map.put("<" + LocaleStorage.translate("parameter.price", Config.locale) + ">", Arrays.asList("auctionhouse.list", "true"));
            } else if (((String)buffer).equals("/ah show ") && sender.isOp()) {
                cmd = buffer;
                players = new java.util.ArrayList<>(AuctionHouse.plugin.getServer().getOnlinePlayers()); players.sort(Comparator.comparing(Player::getName));
                for (Player p : players) {
                    map.put(p.getName(), Arrays.asList("auctionhouse.show", "false"));
                }
            } else if (((String)buffer).equals("/ah menu ") && sender.isOp()) {
                cmd = buffer;
                players = new java.util.ArrayList<>(AuctionHouse.plugin.getServer().getOnlinePlayers()); players.sort(Comparator.comparing(Player::getName));
                for (Player p : players) {
                    map.put(p.getName(), Arrays.asList("auctionhouse.menu", "false"));
                }
            } else if (((String)buffer).equals("/ah purge ")) {
                cmd = buffer;
                players = new java.util.ArrayList<>(AuctionHouse.plugin.getServer().getOnlinePlayers()); players.sort(Comparator.comparing(Player::getName));
                for (Player p : players) {
                    map.put(p.getName(), Arrays.asList("auctionhouse.purge", "false"));
                }
            } else if (((String)buffer).equals("/ah sell ")) {
                cmd = buffer;
                map.put("<" + LocaleStorage.translate("parameter.price", Config.locale) + ">", Arrays.asList("auctionhouse.sell", "true"));
            } else if (((String)buffer).equals("/ah ulist ")) {
                cmd = buffer;
                map.put("<" + LocaleStorage.translate("parameter.price", Config.locale) + ">", Arrays.asList("auctionhouse.ulist", "true"));
            } else if (((String)buffer).startsWith("/ah ")) {
                cmd = "/ah ";
                map.put(LocaleStorage.translate("command.cancel", Config.locale), Arrays.asList("auctionhouse.cancel", "false"));
                map.put(LocaleStorage.translate("command.expired", Config.locale), Arrays.asList("auctionhouse.expired", "false"));
                map.put(LocaleStorage.translate("command.help", Config.locale), Arrays.asList("auctionhouse.use", "false"));
                map.put(LocaleStorage.translate("command.list", Config.locale), Arrays.asList("auctionhouse.list", "false"));
                map.put(LocaleStorage.translate("command.show", Config.locale), Arrays.asList("auctionhouse.show", "false"));
                map.put(LocaleStorage.translate("command.menu", Config.locale), Arrays.asList("auctionhouse.menu", "false"));
                map.put(LocaleStorage.translate("command.reload", Config.locale), Arrays.asList("auctionhouse.reload", "false"));
                map.put(LocaleStorage.translate("command.return", Config.locale), Arrays.asList("auctionhouse.return", "false"));
                map.put(LocaleStorage.translate("command.search", Config.locale), Arrays.asList("auctionhouse.search", "false"));
                map.put(LocaleStorage.translate("command.sell", Config.locale), Arrays.asList("auctionhouse.sell", "false"));
                map.put(LocaleStorage.translate("command.selling", Config.locale), Arrays.asList("auctionhouse.selling", "false"));
                map.put(LocaleStorage.translate("command.sold", Config.locale), Arrays.asList("auctionhouse.sold", "false"));
                map.put(LocaleStorage.translate("command.ulist", Config.locale), Arrays.asList("auctionhouse.ulist", "false"));
                map.put(LocaleStorage.translate("command.purge", Config.locale), Arrays.asList("auctionhouse.purge", "false"));
            }
        } else if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
            if (((String)(buffer = "ah " + (String)buffer)).equals("ah menu ") || ((String)buffer).equals("ah show ")) {
                cmd = buffer;
                players = new java.util.ArrayList<>(AuctionHouse.plugin.getServer().getOnlinePlayers()); players.sort(Comparator.comparing(Player::getName));
                for (Player p : players) {
                    map.put(p.getName(), Arrays.asList("", "false"));
                }
            } else if (((String)buffer).equals("ah purge ")) {
                cmd = buffer;
                players = new java.util.ArrayList<>(AuctionHouse.plugin.getServer().getOnlinePlayers()); players.sort(Comparator.comparing(Player::getName));
                for (Player p : players) {
                    map.put(p.getName(), Arrays.asList("", "false"));
                }
            } else if (((String)buffer).startsWith("ah ")) {
                cmd = "ah ";
                map.put(LocaleStorage.translate("command.help", Config.locale), Arrays.asList("", "false"));
                map.put(LocaleStorage.translate("command.menu", Config.locale), Arrays.asList("", "false"));
                map.put(LocaleStorage.translate("command.reload", Config.locale), Arrays.asList("", "false"));
                map.put(LocaleStorage.translate("command.purge", Config.locale), Arrays.asList("", "false"));
            }
        } else if (((String)(buffer = "ah " + (String)buffer)).equals("ah menu ") || ((String)buffer).equals("ah show ")) {
            cmd = buffer;
            players = new java.util.ArrayList<>(AuctionHouse.plugin.getServer().getOnlinePlayers()); players.sort(Comparator.comparing(Player::getName));
            for (Player p : players) {
                map.put(p.getName(), Arrays.asList("", "false"));
            }
        } else if (((String)buffer).startsWith("ah ")) {
            cmd = "ah ";
            map.put(LocaleStorage.translate("command.menu", Config.locale), Arrays.asList("", "false"));
        }
        return this.buildCompletions(sender, (String)buffer, (String)cmd, map);
    }

    public LinkedList<String> buildCompletions(CommandSender sender, String buffer, String cmd, LinkedHashMap<String, List<String>> map) {
        LinkedList<String> completions = new LinkedList<String>();
        for (String key : map.keySet()) {
            List<String> opts = map.get(key);
            String p = opts.get(0);
            boolean parameter = Boolean.parseBoolean(opts.get(1));
            Object c = parameter ? cmd : cmd + key;
            if (!((String)c).contains(buffer)) continue;
            if (sender instanceof Player) {
                Player player = (Player)sender;
                if (!p.isEmpty() && !player.hasPermission(p) && !player.isOp()) continue;
                completions.add(key);
                continue;
            }
            completions.add(key);
        }
        return completions;
    }
}

