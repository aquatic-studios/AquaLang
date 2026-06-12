package com.aquaticstudios.aqualang.addon.listener;

import com.aquaticstudios.aqualang.addon.menu.LanguageMenu;
import com.aquaticstudios.aqualang.addon.menu.LanguageMenuHolder;
import com.aquaticstudios.aqualang.api.AquaLangAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof LanguageMenuHolder)) return;
        LanguageMenuHolder holder = (LanguageMenuHolder) top.getHolder();

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(top)) return;

        int slot = event.getSlot();
        int page = holder.getPage();

        if (slot == 26) {
            LanguageMenu.open(player, page + 1);
            return;
        }
        if (slot == 18) {
            LanguageMenu.open(player, page - 1);
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String lang = meta.getPersistentDataContainer()
                .get(LanguageMenu.LANG_KEY, PersistentDataType.STRING);
        if (lang == null) return;

        if (AquaLangAPI.setLanguage(player, lang)) {
            AquaLangAPI.api().send(player, "messages.selected", "<lang>", lang);
            player.closeInventory();
        } else {
            AquaLangAPI.api().send(player, "messages.invalid", "<lang>", lang);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof LanguageMenuHolder) {
            event.setCancelled(true);
        }
    }
}
