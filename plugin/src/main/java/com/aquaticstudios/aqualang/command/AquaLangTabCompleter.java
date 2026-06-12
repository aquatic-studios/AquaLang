package com.aquaticstudios.aqualang.command;

import com.aquaticstudios.aqualang.AquaLang;
import com.aquaticstudios.aqualang.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class AquaLangTabCompleter implements TabCompleter {

    private final AquaLang plugin;
    private final LanguageManager languageManager;

    public AquaLangTabCompleter(AquaLang plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            return filter(args[0], getAvailableSubCommands(sender));
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "set":
                case "reset":
                case "info":
                    if (!hasPermission(sender, args[0])) return Collections.emptyList();
                    return filter(args[1], getOnlinePlayers());

                case "github":
                    if (!hasPermission(sender, "github")) return Collections.emptyList();
                    return filter(args[1], Arrays.asList("sync", "status"));

                default:
                    return Collections.emptyList();
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                if (!hasPermission(sender, "set")) return Collections.emptyList();
                return filter(args[2], getLanguages());
            }
        }

        return Collections.emptyList();
    }

    private List<String> getAvailableSubCommands(CommandSender sender) {
        List<String> cmds = new ArrayList<>();
        cmds.add("help");
        addIfPerm(sender, cmds, "list");
        addIfPerm(sender, cmds, "aliases");
        addIfPerm(sender, cmds, "set");
        addIfPerm(sender, cmds, "reset");
        addIfPerm(sender, cmds, "info");
        addIfPerm(sender, cmds, "reload");
        addIfPerm(sender, cmds, "github");
        return cmds;
    }

    private void addIfPerm(CommandSender sender, List<String> list, String cmd) {
        if (hasPermission(sender, cmd)) {
            list.add(cmd);
        }
    }

    private boolean hasPermission(CommandSender sender, String cmd) {
        return sender.hasPermission("aqualang.admin")
                || sender.hasPermission("aqualang." + cmd);
    }

    private List<String> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    private List<String> getLanguages() {
        return new ArrayList<>(languageManager.getLanguageMap().keySet());
    }

    private List<String> filter(String input, List<String> values) {
        if (input.isEmpty()) return values;
        String lower = input.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(v -> v.toLowerCase(Locale.ROOT).startsWith(lower))
                .collect(Collectors.toList());
    }
}
