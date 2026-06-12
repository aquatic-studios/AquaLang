package com.aquaticstudios.aqualang.addon.command;

import com.aquaticstudios.aqualang.addon.menu.LanguageMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AquaAddonCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("aquaaddon.menu") && !player.hasPermission("aqualang.admin")) {
            return true;
        }

        LanguageMenu.open(player, 1);
        return true;
    }
}
