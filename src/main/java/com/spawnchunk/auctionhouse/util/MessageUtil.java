package com.spawnchunk.auctionhouse.util;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.storage.LocaleStorage;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nullable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MessageUtil {
    public static final String discord_prefix = ":shopping_cart: ";
    public static final String bold = "**";
    public static final String ansi_reset = "\u001b[0m";
    public static final String ansi_red = "\u001b[31m";
    public static final String ansi_green = "\u001b[32m";
    public static final String ansi_yellow = "\u001b[33m";
    public static final String ansi_bright_red = "\u001b[1;31m";
    public static final String ansi_bright_green = "\u001b[1;32m";
    public static final String ansi_bright_yellow = "\u001b[1;33m";

    public static void logSevere(String message) {
        AuctionHouse.logger.log(Level.SEVERE, String.format("%s%s%s", Config.log_ansi ? ansi_bright_red : "", message, Config.log_ansi ? ansi_reset : ""));
    }

    public static void logWarning(String message) {
        AuctionHouse.logger.log(Level.WARNING, String.format("%s%s%s", Config.log_ansi ? ansi_bright_yellow : "", message, Config.log_ansi ? ansi_reset : ""));
    }

    public static void logTest(String message) {
        AuctionHouse.logger.log(Level.INFO, String.format("%s%s%s", Config.log_ansi ? ansi_bright_green : "", message, Config.log_ansi ? ansi_reset : ""));
    }

    public static String sectionSymbol(String message) {
        String m = message.replaceAll("&#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])", "\u00a7x\u00a7$1\u00a7$2\u00a7$3\u00a7$4\u00a7$5\u00a7$6");
        m = m.replaceAll("&([0-9a-fk-orx])", "\u00a7$1");
        return m;
    }

    public static String nocolor(String message) {
        return message.replaceAll("\u00a7", "&").replaceAll("&#[&0-9a-fA-F]{12}", "").replaceAll("&[0-9a-fA-Fk-orx]", "");
    }

    public static String readable(String string) {
        return WordUtils.capitalizeFully((String)string.replace("_", " "));
    }

    public static String populate(String message, Object ... args) {
        int pos = 0;
        for (Object arg : args) {
            String value = "";
            if (arg instanceof String) {
                value = String.format("%s", arg);
            }
            if (arg instanceof Byte) {
                value = String.format("%d", arg);
            }
            if (arg instanceof Short) {
                value = String.format("%d", arg);
            }
            if (arg instanceof Integer) {
                value = String.format("%d", arg);
            }
            if (arg instanceof Long) {
                value = String.format("%dL", arg);
            }
            if (arg instanceof Float) {
                value = Config.numberFormat.format(arg);
            }
            if (arg instanceof Double) {
                value = Config.numberFormat.format(arg);
            }
            message = message.replace(String.format("{%d}", pos), value + "\u00a7r");
            message = message.replace("\u00a0", " ");
            ++pos;
        }
        return message;
    }

    public static void discordMessage(String key, String locale) {
        Map channels;
        if (AuctionHouse.discord && Config.discord_channel != null && !Config.discord_channel.isEmpty() && (channels = DiscordSRV.getPlugin().getChannels()).containsKey(Config.discord_channel)) {
            String channel_id = (String)channels.get(Config.discord_channel);
            if (Config.debug) {
                AuctionHouse.logger.info(String.format("channel_id = %s", channel_id));
            }
            TextChannel textChannel = DiscordUtil.getJda().getTextChannelById(channel_id);
            String message = MessageUtil.nocolor(LocaleStorage.translate("discord.prefix", locale) + LocaleStorage.translate(key, locale));
            if (textChannel != null) {
                if (!(message = TextComponent.toPlainText((BaseComponent[])MessageUtil.translateItem(message))).isEmpty()) {
                    DiscordUtil.sendMessage((TextChannel)textChannel, (String)message);
                }
            } else if (Config.debug) {
                AuctionHouse.logger.warning(String.format("%s channel ID (%s) was invalid!", Config.discord_channel, channel_id));
            }
        }
    }

    public static void discordMessage(String key, String locale, Object ... args) {
        Map channels;
        if (AuctionHouse.discord && Config.discord_channel != null && !Config.discord_channel.isEmpty() && (channels = DiscordSRV.getPlugin().getChannels()).containsKey(Config.discord_channel)) {
            TextChannel textChannel;
            String channel_id = (String)channels.get(Config.discord_channel);
            if (Config.debug) {
                AuctionHouse.logger.info(String.format("channel_id = %s", channel_id));
            }
            if ((textChannel = DiscordUtil.getJda().getTextChannelById(channel_id)) != null) {
                String message = MessageUtil.nocolor(MessageUtil.populate(LocaleStorage.translate("discord.prefix", locale) + LocaleStorage.translate(key, locale), args));
                if (!(message = TextComponent.toPlainText((BaseComponent[])MessageUtil.translateItem(message))).isEmpty()) {
                    DiscordUtil.sendMessage((TextChannel)textChannel, (String)message);
                }
            } else if (Config.debug) {
                AuctionHouse.logger.warning(String.format("%s channel ID (%s) was invalid!", Config.discord_channel, channel_id));
            }
        }
    }

    public static void logMessage(String key, String locale) {
        String message = MessageUtil.nocolor(LocaleStorage.translate(key, locale));
        if (!message.isEmpty()) {
            AuctionHouse.logger.info(message);
        }
    }

    public static void logMessage(String key, String locale, Object ... args) {
        String message = MessageUtil.nocolor(MessageUtil.populate(LocaleStorage.translate(key, locale), args));
        if (!message.isEmpty()) {
            AuctionHouse.logger.info(message);
        }
    }

    public static void sendMessage(final CommandSender sender, final String key, final String locale) {
        new BukkitRunnable(){

            public void run() {
                String message = MessageUtil.sectionSymbol(LocaleStorage.translate(key, locale));
                if (!(sender instanceof Player)) {
                    message = MessageUtil.nocolor(message);
                }
                if (!message.isEmpty()) {
                    MessageUtil.chatMessage(sender, MessageUtil.translateItem(message));
                }
            }
        }.runTask((Plugin)AuctionHouse.plugin);
    }

    public static void sendMessage(final CommandSender sender, final String key, final String locale, final Object ... args) {
        new BukkitRunnable(){

            public void run() {
                String message = MessageUtil.sectionSymbol(MessageUtil.populate(LocaleStorage.translate(key, locale), args));
                if (!(sender instanceof Player)) {
                    message = MessageUtil.nocolor(message);
                }
                if (!message.isEmpty()) {
                    MessageUtil.chatMessage(sender, MessageUtil.translateItem(message));
                }
            }
        }.runTask((Plugin)AuctionHouse.plugin);
    }

    public static void sendMessage(final Player player, final String key, final String locale) {
        new BukkitRunnable(){

            public void run() {
                String message = MessageUtil.sectionSymbol(LocaleStorage.translate(key, locale));
                if (!message.isEmpty()) {
                    MessageUtil.chatMessage(player, MessageUtil.translateItem(message));
                }
            }
        }.runTask((Plugin)AuctionHouse.plugin);
    }

    public static void sendMessage(String world, Player player, String key, String locale) {
        String w = player.getWorld().getName();
        if (Config.per_world_listings && (Config.group_worlds ? !w.contains(WorldUtil.getWorldPrefix(world)) : !w.equals(world))) {
            return;
        }
        MessageUtil.sendMessage(player, key, locale);
    }

    public static void sendMessage(final Player player, final String key, final String locale, final Object ... args) {
        new BukkitRunnable(){

            public void run() {
                String message = MessageUtil.sectionSymbol(MessageUtil.populate(LocaleStorage.translate(key, locale), args));
                if (!message.isEmpty()) {
                    MessageUtil.chatMessage(player, MessageUtil.translateItem(message));
                }
            }
        }.runTask((Plugin)AuctionHouse.plugin);
    }

    public static void sendMessage(String world, Player player, String key, String locale, Object ... args) {
        String w = player.getWorld().getName();
        if (Config.per_world_listings && (Config.group_worlds ? !w.contains(WorldUtil.getWorldPrefix(world)) : !w.equals(world))) {
            return;
        }
        MessageUtil.sendMessage(player, key, locale, args);
    }

    public static List<String> expand(List<String> in) {
        ArrayList<String> out = new ArrayList<String>();
        for (String s : in) {
            String[] line;
            for (String t : line = s.split("\n")) {
                if (t.isEmpty()) continue;
                out.add(t);
            }
        }
        return out;
    }

    private static BaseComponent[] translateItem(String message) {
        String[] parts;
        ComponentBuilder builder = new ComponentBuilder("");
        for (String part : parts = message.split("::")) {
            if (part.startsWith("{") && part.endsWith("}")) {
                String p = part.substring(1, part.length() - 1);
                builder.append((BaseComponent)new TranslatableComponent(p, new Object[0]), ComponentBuilder.FormatRetention.NONE);
                continue;
            }
            builder.append(TextComponent.fromLegacyText((String)part), ComponentBuilder.FormatRetention.NONE);
        }
        BaseComponent[] components = builder.create();
        return components;
    }

    private static void chatMessage(CommandSender sender, BaseComponent[] components) {
        if (sender == null || components.length == 0) {
            return;
        }
        sender.spigot().sendMessage(components);
    }

    private static void chatMessage(Player player, BaseComponent[] components) {
        if (player == null || components.length == 0) {
            return;
        }
        player.spigot().sendMessage(ChatMessageType.CHAT, components);
    }

    public static void sendActionBar(String world, Player player, @Nullable Long delay, String key, String locale, Object ... args) {
        String w = player.getWorld().getName();
        if (Config.per_world_listings && (Config.group_worlds ? !w.contains(WorldUtil.getWorldPrefix(world)) : !w.equals(world))) {
            return;
        }
        MessageUtil.sendActionBar(player, delay, key, locale, args);
    }

    public static void sendActionBar(String world, Player player, @Nullable Long delay, String message) {
        String w = player.getWorld().getName();
        if (Config.per_world_listings && (Config.group_worlds ? !w.contains(WorldUtil.getWorldPrefix(world)) : !w.equals(world))) {
            return;
        }
        MessageUtil.sendActionBar(player, delay, message);
    }

    public static void sendActionBar(Player player, @Nullable Long delay, String key, String locale, Object ... args) {
        String translation = LocaleStorage.translate(key, locale);
        if (!(translation = MessageUtil.sectionSymbol(MessageUtil.populate(translation, args))).isEmpty()) {
            MessageUtil.actionBar(player, delay, MessageUtil.translateItem(translation));
        }
    }

    public static void sendActionBar(Player player, @Nullable Long delay, String message) {
        MessageUtil.actionBar(player, delay, TextComponent.fromLegacyText((String)MessageUtil.sectionSymbol(message)));
    }

    public static void actionBar(final Player player, @Nullable Long delay, BaseComponent[] component) {
        if (delay == null || delay == 0L) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
        } else {
            final BaseComponent[] components = (BaseComponent[])component.clone();
            new BukkitRunnable(){

                public void run() {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
                }
            }.runTaskLater((Plugin)AuctionHouse.plugin, delay.longValue());
        }
    }

    public static void clearActionBar(String world, Player player, @Nullable Long delay) {
        MessageUtil.sendActionBar(world, player, delay, "");
    }

    public static void clearActionBar(Player player, @Nullable Long delay) {
        MessageUtil.sendActionBar(player, delay, "");
    }
}

