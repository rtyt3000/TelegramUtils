package in.ppsh.goidaworld.telegramUtils.utils;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {
    @Getter
    private final YamlConfiguration config;

    public ConfigManager(String name, File dataFolder) {
        File file = new File(dataFolder, name);
        config = YamlConfiguration.loadConfiguration(file);
    }

    public Component getMiniMessage(String path, String def) {
        String message = config.getString(path, def);
        return MiniMessage.miniMessage().deserialize(message);
    }

    public String getMessage(String path, String def) {
        return config.getString(path, def);
    }
}
