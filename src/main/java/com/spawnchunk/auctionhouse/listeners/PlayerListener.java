package com.spawnchunk.auctionhouse.listeners;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.events.DropUnclaimedEvent;
import com.spawnchunk.auctionhouse.modules.Auctions;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import java.util.List;
import java.util.UUID;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener
implements Listener {
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player;
        if (!Config.per_world_listings && (player = event.getPlayer()).isOnline()) {
            Auctions.returnUnclaimedItems(player);
            Auctions.checkExpiredItems(player);
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        AuctionHouse.playerCooldowns.remove(uuid);
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player;
        if (Config.per_world_listings && Config.unclaimed_check_on_world_change && (player = event.getPlayer()).isOnline()) {
            Auctions.returnUnclaimedItems(player);
            Auctions.checkExpiredItems(player);
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onDropUnclaimed(DropUnclaimedEvent event) {
        List<Player> players = Auctions.checkUnclaimedItems();
        for (Player player : players) {
            if (!player.isOnline()) continue;
            Auctions.returnUnclaimedItems(player);
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        String customName = entity.getCustomName();
        if (customName != null && Config.entity_name_triggers != null && !Config.entity_name_triggers.isEmpty() && Config.entity_name_triggers.contains(customName) && (player.hasPermission("auctionhouse.trigger.entity") || player.isOp())) {
            Auctions.filter = null;
            Auctions.menu_mode = false;
            Auctions.openActiveListingsMenu(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action;
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();
        if (block != null && (action = event.getAction()) == Action.RIGHT_CLICK_BLOCK) {
            Nameable nameable;
            String name;
            BlockState bs = block.getState();
            if (!(Config.sign_trigger == null || Config.sign_trigger.isEmpty() || item != null && AuctionHouse.nms.isDye(item) || !(bs instanceof Sign) || !player.hasPermission("auctionhouse.trigger.sign") && !player.isOp())) {
                String[] lines;
                Sign sign = (Sign)bs;
                for (String line : lines = sign.getLines()) {
                    if (!MessageUtil.nocolor(line).contains(MessageUtil.nocolor(Config.sign_trigger))) continue;
                    Auctions.filter = null;
                    Auctions.menu_mode = false;
                    Auctions.openActiveListingsMenu(player);
                    event.setCancelled(true);
                }
            }
            if (Config.block_name_triggers != null && !Config.block_name_triggers.isEmpty() && bs instanceof Nameable && (name = (nameable = (Nameable)bs).getCustomName()) != null && Config.block_name_triggers.contains(name) && (player.hasPermission("auctionhouse.trigger.block") || player.isOp())) {
                Auctions.filter = null;
                Auctions.menu_mode = false;
                Auctions.openActiveListingsMenu(player);
                event.setCancelled(true);
            }
        }
    }
}

