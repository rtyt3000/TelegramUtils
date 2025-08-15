package in.ppsh.goidaworld.telegramUtils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ConfigManager {
    private final File file;
    private final YamlConfiguration config;
    private final Logger logger;

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

    public Component getMiniMessage(String path, String def) {
        String message = config.getString(path, def);
        return MiniMessage.miniMessage().deserialize(message);
    }

    public String getMessage(String path, String def) {
        return config.getString(path, def);
    }
}
