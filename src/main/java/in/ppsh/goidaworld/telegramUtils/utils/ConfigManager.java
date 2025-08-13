package in.ppsh.goidaworld.telegramUtils.utils;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ConfigManager {
    File file;
    YamlConfiguration config;
    Logger logger;

    public ConfigManager(String name, File dataFolder, Logger pluginLogger) {
        file = new File(dataFolder, name);
        config = YamlConfiguration.loadConfiguration(file);
        logger = pluginLogger;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            logger.warning("Failed to save config file: " + file.getName() + ". Error: " + e.getMessage());
        }
    }
}
