package com.spawnchunk.auctionhouse.menus;

import com.spawnchunk.auctionhouse.menus.Menu;
import com.spawnchunk.auctionhouse.util.PlayerUtil;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

public class MenuManager {
    private final Map<String, Menu> menus = new HashMap<String, Menu>();
    private final Map<UUID, Menu> activeMenus = new HashMap<UUID, Menu>();

    public String createMenu(UUID uuid, String title, int size) {
        String key = uuid.toString() + title;
        String id = Base64.getEncoder().encodeToString(key.getBytes());
        this.updateMenu(id, title, size);
        return id;
    }

    public void updateMenu(String id, String title, int size) {
        if (this.menus.containsKey(id)) {
            this.menus.replace(id, new Menu(title, size));
        } else {
            this.menus.put(id, new Menu(title, size));
        }
    }

    public Menu openMenu(UUID uuid, String id) {
        if (this.menus.containsKey(id)) {
            Menu menu = this.menus.get(id);
            Player player = PlayerUtil.getPlayer(uuid);
            if (player != null) {
                InventoryView view = player.openInventory(menu.getInventory());
                if (view != null) {
                    this.activeMenus.put(uuid, menu);
                }
                return menu;
            }
        }
        return null;
    }

    public void closeMenu(UUID uuid, String id) {
        if (this.menus.containsKey(id)) {
            Menu menu = this.menus.get(id);
            Player player = PlayerUtil.getPlayer(uuid);
            if (player != null) {
                if (menu.getViewers().contains(player)) {
                    player.closeInventory();
                }
                this.activeMenus.remove(uuid);
            }
        }
    }

    public void removeMenu(String id) {
        this.menus.remove(id);
    }

    public String getId(UUID uuid) {
        Menu menu = this.getActiveMenu(uuid);
        if (menu != null) {
            return this.getId(uuid, menu);
        }
        return null;
    }

    public String getId(UUID uuid, Menu menu) {
        String title = menu.getTitle();
        String key = uuid.toString() + title;
        String id = Base64.getEncoder().encodeToString(key.getBytes());
        if (this.menus.containsKey(id)) {
            return id;
        }
        return null;
    }

    public Map<String, Menu> getMenus() {
        return this.menus;
    }

    public Menu getActiveMenu(UUID uuid) {
        return this.activeMenus.get(uuid);
    }

    public Menu getMenu(String id) {
        return this.menus.get(id);
    }

    public Menu getMenu(UUID uuid, String title) {
        String key = uuid.toString() + title;
        String id = Base64.getEncoder().encodeToString(key.getBytes());
        return this.menus.get(id);
    }

    public boolean isMenu(UUID uuid, String title) {
        String key = uuid.toString() + title;
        String id = Base64.getEncoder().encodeToString(key.getBytes());
        return this.menus.containsKey(id);
    }

    public void closeAllMenus() {
        String id;
        ArrayList<UUID> toRemove = new ArrayList<UUID>();
        for (UUID uuid : this.activeMenus.keySet()) {
            id = this.getId(uuid);
            if (id == null) continue;
            toRemove.add(uuid);
        }
        for (UUID uuid : toRemove) {
            id = this.getId(uuid);
            this.closeMenu(uuid, id);
            this.removeMenu(id);
        }
        this.activeMenus.clear();
        this.menus.clear();
    }
}

