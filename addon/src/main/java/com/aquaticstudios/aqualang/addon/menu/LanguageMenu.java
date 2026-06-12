package com.aquaticstudios.aqualang.addon.menu;

import com.aquaticstudios.aqualang.addon.AquaLangAddon;
import com.aquaticstudios.aqualang.api.AquaLangAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class LanguageMenu {

    public static final NamespacedKey LANG_KEY =
            new NamespacedKey(AquaLangAPI.DEFAULT_NAMESPACE, "language");

    private static final int[] SLOTS = {
            11, 12, 13, 14, 15,
            20, 21, 22, 23, 24,
            29, 30, 31, 32, 33
    };

    private static final int PREVIOUS_SLOT = 18;
    private static final int NEXT_SLOT = 26;
    private static final int PAGE_SIZE = SLOTS.length;
    private static final int INVENTORY_SIZE = 45;

    private LanguageMenu() {}

    public static void open(Player player, int page) {
        String userLang = AquaLangAPI.getLanguage(player);
        YamlConfiguration cfg = loadConfig(userLang);

        List<String> languages = new ArrayList<>(AquaLangAPI.getRegistered());
        if (languages.isEmpty()) return;

        int maxPages = Math.max(1, (int) Math.ceil(languages.size() / (double) PAGE_SIZE));
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        String title = cfg.getString("menu.title", "Languages > <page>/<pages>")
                .replace("<page>", String.valueOf(page))
                .replace("<pages>", String.valueOf(maxPages));

        LanguageMenuHolder holder = new LanguageMenuHolder(page);
        Inventory inv = Bukkit.createInventory(holder, INVENTORY_SIZE, AquaLangAPI.color(title));
        holder.setInventory(inv);

        int start = (page - 1) * PAGE_SIZE;
        for (int i = 0; i < SLOTS.length && start + i < languages.size(); i++) {
            String lang = languages.get(start + i);
            inv.setItem(SLOTS[i], buildLangItem(cfg, lang));
        }

        if (page > 1) inv.setItem(PREVIOUS_SLOT, buildArrow(cfg, "arrows.previous"));
        if (page < maxPages) inv.setItem(NEXT_SLOT, buildArrow(cfg, "arrows.next"));

        player.openInventory(inv);
    }

    private static YamlConfiguration loadConfig(String lang) {
        File aquaLangFolder = AquaLangAddon.get().getServer().getPluginManager()
                .getPlugin("AquaLang").getDataFolder();
        File file = new File(aquaLangFolder,
                "languages/" + lang + "/" + AquaLangAPI.DEFAULT_NAMESPACE + ".yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    private static ItemStack buildLangItem(YamlConfiguration cfg, String lang) {
        Material material = materialOf(
                cfg.getString("menu.language-item.material", "PAPER"),
                Material.PAPER
        );

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String displayLang = capitalize(lang);

        String rawName = cfg.getString("menu.language-item.name", "&#35ADFF<lang>")
                .replace("<lang>", displayLang);
        meta.setDisplayName(AquaLangAPI.color(rawName));

        List<String> lore = new ArrayList<>();
        for (String line : cfg.getStringList("menu.language-item.lore")) {
            lore.add(AquaLangAPI.color(line.replace("<lang>", displayLang)));
        }
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(LANG_KEY, PersistentDataType.STRING, lang);

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildArrow(YamlConfiguration cfg, String path) {
        Material material = materialOf(cfg.getString(path + ".material", "ARROW"), Material.ARROW);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(AquaLangAPI.color(cfg.getString(path + ".name", "&fArrow")));

        List<String> lore = new ArrayList<>();
        for (String line : cfg.getStringList(path + ".lore")) {
            lore.add(AquaLangAPI.color(line));
        }
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private static Material materialOf(String name, Material fallback) {
        if (name == null) return fallback;
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
