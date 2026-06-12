package com.aquaticstudios.aqualang.addon.config;

import com.aquaticstudios.aqualang.addon.AquaLangAddon;
import com.aquaticstudios.aqualang.api.AquaLangAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public final class MenuConfigGenerator {

    private static final String FILE_NAME = AquaLangAPI.DEFAULT_NAMESPACE + ".yml";
    private static final String FALLBACK = "english";

    private MenuConfigGenerator() {}

    public static int generateForAllLanguages() {
        AquaLangAddon plugin = AquaLangAddon.get();
        Set<String> langs = AquaLangAPI.getRegistered();

        File aquaLangFolder = new File(plugin.getServer().getPluginManager()
                .getPlugin("AquaLang").getDataFolder(), "languages");

        int generated = 0;

        for (String lang : langs) {
            File targetFolder = new File(aquaLangFolder, lang);
            if (!targetFolder.exists() && !targetFolder.mkdirs()) continue;

            File targetFile = new File(targetFolder, FILE_NAME);
            if (targetFile.exists()) continue;

            if (copyTemplate(lang, targetFile)) {
                generated++;
            } else if (copyTemplate(FALLBACK, targetFile)) {
                generated++;
                plugin.getLogger().info("No template for '" + lang
                        + "', used English fallback.");
            } else {
                plugin.getLogger().warning("Could not generate menu file for '"
                        + lang + "'.");
            }
        }

        return generated;
    }

    private static boolean copyTemplate(String lang, File target) {
        String resourcePath = "/menu/" + lang + ".yml";

        try (InputStream in = MenuConfigGenerator.class.getResourceAsStream(resourcePath)) {
            if (in == null) return false;
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            AquaLangAddon.get().getLogger().warning(
                    "Failed to copy menu template " + resourcePath + ": " + e.getMessage()
            );
            return false;
        }
    }
}
