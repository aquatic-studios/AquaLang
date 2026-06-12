package com.aquaticstudios.aqualang.addon.listener;

import com.aquaticstudios.aqualang.addon.AquaLangAddon;
import com.aquaticstudios.aqualang.addon.menu.LanguageMenu;
import com.aquaticstudios.aqualang.addon.util.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class FirstJoinListener implements Listener {

    private static final long OPEN_DELAY_TICKS = 20L;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) return;

        Scheduler.atEntityLater(
                AquaLangAddon.get(),
                player,
                () -> LanguageMenu.open(player, 1),
                OPEN_DELAY_TICKS
        );
    }
}
