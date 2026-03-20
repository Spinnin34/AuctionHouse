package com.spawnchunk.auctionhouse.listeners;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.config.Config;
import com.spawnchunk.auctionhouse.events.MenuClickEvent;
import com.spawnchunk.auctionhouse.events.MenuCloseEvent;
import com.spawnchunk.auctionhouse.menus.Menu;
import com.spawnchunk.auctionhouse.menus.MenuClickType;
import com.spawnchunk.auctionhouse.util.InventoryUtil;
import com.spawnchunk.auctionhouse.util.ItemUtil;
import com.spawnchunk.auctionhouse.util.MessageUtil;
import com.spawnchunk.auctionhouse.util.SoundUtil;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class MenuListener
implements Listener {
    private MenuClickType getMenuClickType(ClickType click) {
        if (click.isShiftClick()) {
            if (click.isLeftClick()) {
                return MenuClickType.SHIFT_LEFT;
            }
            if (click.isRightClick()) {
                return MenuClickType.SHIFT_RIGHT;
            }
            if (click == ClickType.MIDDLE) {
                return MenuClickType.SHIFT_MIDDLE;
            }
        } else {
            if (click.isLeftClick()) {
                return MenuClickType.LEFT;
            }
            if (click.isRightClick()) {
                return MenuClickType.RIGHT;
            }
            if (click == ClickType.MIDDLE) {
                return MenuClickType.MIDDLE;
            }
        }
        if (click.isKeyboardClick()) {
            return MenuClickType.KEYBOARD;
        }
        return MenuClickType.UNKNOWN;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        String id;
        Menu menu;
        HumanEntity he = event.getPlayer();
        String title = InventoryUtil.getTitle(he);
        Player player = (Player)he;
        if (!player.isOnline()) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (AuctionHouse.menuManager.isMenu(uuid, title) && !title.isEmpty() && (menu = AuctionHouse.menuManager.getMenu(uuid, title)) != null && (id = AuctionHouse.menuManager.getId(uuid, menu)) != null) {
            MenuCloseEvent menuCloseEvent = new MenuCloseEvent(player, id);
            Bukkit.getPluginManager().callEvent((Event)menuCloseEvent);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity he = event.getWhoClicked();
        String title = InventoryUtil.getTitle(he);
        Player player = (Player)he;
        if (!player.isOnline()) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (AuctionHouse.menuManager.isMenu(uuid, title) && !title.isEmpty()) {
            int slot;
            InventoryAction action = event.getAction();
            ClickType click = event.getClick();
            MenuClickType type = this.getMenuClickType(click);
            int size = event.getInventory().getSize();
            int rawSlot = event.getRawSlot();
            int n = slot = rawSlot > size ? rawSlot - size : rawSlot;
            if (Config.debug) {
                AuctionHouse.logger.info(String.format("RawSlot = %d", rawSlot));
                AuctionHouse.logger.info(String.format("ClickType = %s", click.name()));
                AuctionHouse.logger.info(String.format("InventoryAction = %s", action.name()));
            }
            if (rawSlot == -999) {
                event.setCancelled(true);
                return;
            }
            if (rawSlot < size) {
                MenuClickEvent menuClickEvent;
                String id;
                Menu menu;
                ItemStack item;
                if (action.toString().startsWith("PICKUP") && (item = event.getCurrentItem()) != null && item.getType() != Material.AIR && (menu = AuctionHouse.menuManager.getMenu(uuid, title)) != null && (id = AuctionHouse.menuManager.getId(uuid, menu)) != null) {
                    menuClickEvent = new MenuClickEvent(player, id, slot, type);
                    Bukkit.getPluginManager().callEvent((Event)menuClickEvent);
                }
                if (rawSlot < size - 9 && (type == MenuClickType.SHIFT_LEFT || type == MenuClickType.SHIFT_RIGHT) && (item = event.getCurrentItem()) != null && item.getType() != Material.AIR) {
                    if (player.hasPermission("auctionhouse.cancel.others") || player.isOp()) {
                        menu = AuctionHouse.menuManager.getMenu(uuid, title);
                        if (menu != null && (id = AuctionHouse.menuManager.getId(uuid, menu)) != null) {
                            menuClickEvent = new MenuClickEvent(player, id, slot, type);
                            Bukkit.getPluginManager().callEvent((Event)menuClickEvent);
                        }
                    } else {
                        SoundUtil.failSound(player);
                    }
                }
                if (rawSlot < size - 9 && type == MenuClickType.MIDDLE) {
                    if (action != InventoryAction.CLONE_STACK && player.hasPermission("auctionhouse.pick")) {
                        ItemStack cursor;
                        item = event.getCurrentItem();
                        if (item != null && item.getType() != Material.AIR && (cursor = player.getItemOnCursor()).getType() == Material.AIR) {
                            player.setItemOnCursor(item.clone());
                        }
                    } else {
                        SoundUtil.failSound(player);
                    }
                }
                event.setCancelled(true);
            } else {
                String actionName;
                Material material;
                ItemStack itemStack;
                if (Config.strict && (itemStack = event.getCurrentItem()) != null && (material = itemStack.getType()) != Material.AIR && AuctionHouse.nms.hasNBTLocator(itemStack)) {
                    try {
                        event.setCurrentItem(null);
                        int amount = itemStack.getAmount();
                        String item = ItemUtil.getName(itemStack);
                        String nbt = AuctionHouse.nms.getNBTString(itemStack);
                        MessageUtil.logMessage("message.menu_item.removed", Config.locale, amount, item, nbt, player.getName());
                        event.setCancelled(false);
                        return;
                    }
                    catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                        // empty catch block
                    }
                }
                if ((actionName = action.name()).startsWith("PICKUP") || actionName.startsWith("PLACE")) {
                    return;
                }
                if (actionName.equalsIgnoreCase("CLONE_STACK") && player.hasPermission("auctionhouse.pick")) {
                    return;
                }
                if (Config.debug) {
                    AuctionHouse.logger.info("cancelling action");
                }
            }
            event.setCancelled(true);
        } else if (Config.strict) {
            Material material;
            int size = event.getInventory().getSize();
            int rawSlot = event.getRawSlot();
            int slot = rawSlot > size ? rawSlot - size : rawSlot;
            ItemStack itemStack = event.getCurrentItem();
            if (itemStack != null && (material = itemStack.getType()) != Material.AIR && AuctionHouse.nms.hasNBTLocator(itemStack)) {
                try {
                    event.setCurrentItem(null);
                    int amount = itemStack.getAmount();
                    String item = ItemUtil.getName(itemStack);
                    String nbt = AuctionHouse.nms.getNBTString(itemStack);
                    MessageUtil.logMessage("message.menu_item.removed", Config.locale, amount, item, nbt, player.getName());
                    event.setCancelled(false);
                }
                catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                    // empty catch block
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        block8: {
            Player player;
            InventoryView view;
            block9: {
                String name;
                boolean cancelled = event.isCancelled();
                DragType type = event.getType();
                HumanEntity he = event.getWhoClicked();
                view = he.getOpenInventory();
                player = (Player)he;
                if (!player.isOnline()) {
                    return;
                }
                UUID uuid = player.getUniqueId();
                if (!AuctionHouse.menuManager.isMenu(uuid, name = InventoryUtil.getTitle(he)) || name.isEmpty()) break block9;
                Set<Integer> rawSlots = (Set<Integer>)(Set<?>)event.getRawSlots();
                int size = event.getInventory().getSize();
                for (Integer rawSlot : rawSlots) {
                    if (rawSlot < size) {
                        event.setCancelled(true);
                        break block8;
                    }
                    int slot = rawSlot - size;
                    if (!Config.strict) continue;
                    try {
                        Material material;
                        Inventory bottomInventory = view.getBottomInventory();
                        ItemStack itemStack = bottomInventory.getItem(slot);
                        if (itemStack == null || (material = itemStack.getType()) == Material.AIR || !AuctionHouse.nms.hasNBTLocator(itemStack)) continue;
                        event.setCursor(null);
                        event.getInventory().clear(rawSlot.intValue());
                        int amount = itemStack.getAmount();
                        String item = ItemUtil.getName(itemStack);
                        String nbt = AuctionHouse.nms.getNBTString(itemStack);
                        MessageUtil.logMessage("message.menu_item.removed", Config.locale, amount, item, nbt, player.getName());
                        event.setCancelled(false);
                    }
                    catch (ArrayIndexOutOfBoundsException bottomInventory) {}
                }
                break block8;
            }
            if (!Config.strict) break block8;
            Set<Integer> rawSlots = (Set<Integer>)(Set<?>)event.getRawSlots();
            int size = event.getInventory().getSize();
            boolean cancel = false;
            for (Integer rawSlot : rawSlots) {
                int slot = rawSlot > size ? rawSlot - size : rawSlot;
                try {
                    Material material;
                    Inventory topInventory = view.getTopInventory();
                    Inventory bottomInventory = view.getBottomInventory();
                    ItemStack itemStack = rawSlot < size ? topInventory.getItem(slot) : bottomInventory.getItem(slot);
                    if (itemStack == null || (material = itemStack.getType()) == Material.AIR || !AuctionHouse.nms.hasNBTLocator(itemStack)) continue;
                    event.setCursor(null);
                    event.getInventory().clear(rawSlot.intValue());
                    int amount = itemStack.getAmount();
                    String item = ItemUtil.getName(itemStack);
                    String nbt = AuctionHouse.nms.getNBTString(itemStack);
                    MessageUtil.logMessage("message.menu_item.removed", Config.locale, amount, item, nbt, player.getName());
                    event.setCancelled(false);
                }
                catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {}
            }
        }
    }
}

