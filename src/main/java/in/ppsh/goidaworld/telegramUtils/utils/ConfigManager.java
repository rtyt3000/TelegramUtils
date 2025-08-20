package in.ppsh.goidaworld.telegramUtils.utils;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigManager {
    @Getter
    private final YamlConfiguration config;
    private final File configFile;
    private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();
    
    // Static cache for shared instances
    private static final ConcurrentMap<String, ConfigManager> instances = new ConcurrentHashMap<>();

    public ConfigManager(String name, File dataFolder) {
        this.configFile = new File(dataFolder, name);
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Get or create a shared ConfigManager instance for the given config file.
     * This ensures that the same config file uses the same instance across the application.
     */
    public static ConfigManager getInstance(String name, File dataFolder) {
        String key = new File(dataFolder, name).getAbsolutePath();
        return instances.computeIfAbsent(key, k -> new ConfigManager(name, dataFolder));
    }

    /**
     * Convenience method for creating a language config manager.
     */
    public static ConfigManager getLangConfig(File dataFolder) {
        return getInstance("lang.yml", dataFolder);
    }

    /**
     * Convenience method for creating a main config manager.
     */
    public static ConfigManager getMainConfig(File dataFolder) {
        return getInstance("config.yml", dataFolder);
    }

    // Type-safe getters with caching
    public String getString(String path, String def) {
        return (String) cache.computeIfAbsent(path + ":string:" + def, 
            k -> config.getString(path, def));
    }

    public String getString(String path) {
        return getString(path, null);
    }

    public int getInt(String path, int def) {
        return (Integer) cache.computeIfAbsent(path + ":int:" + def, 
            k -> config.getInt(path, def));
    }

    public int getInt(String path) {
        return getInt(path, 0);
    }

    public boolean getBoolean(String path, boolean def) {
        return (Boolean) cache.computeIfAbsent(path + ":boolean:" + def, 
            k -> config.getBoolean(path, def));
    }

    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    public double getDouble(String path, double def) {
        return (Double) cache.computeIfAbsent(path + ":double:" + def, 
            k -> config.getDouble(path, def));
    }

    public double getDouble(String path) {
        return getDouble(path, 0.0);
    }

    public long getLong(String path, long def) {
        return (Long) cache.computeIfAbsent(path + ":long:" + def, 
            k -> config.getLong(path, def));
    }

    public long getLong(String path) {
        return getLong(path, 0L);
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        return (List<String>) cache.computeIfAbsent(path + ":stringlist", 
            k -> config.getStringList(path));
    }

    // Enhanced message methods with caching
    public Component getMiniMessage(String path, String def) {
        String message = getString(path, def);
        return MiniMessage.miniMessage().deserialize(message);
    }

    public Component getMiniMessage(String path) {
        return getMiniMessage(path, "");
    }

    public String getMessage(String path, String def) {
        return getString(path, def);
    }

    public String getMessage(String path) {
        return getMessage(path, "");
    }

    /**
     * Fluent API for message formatting with placeholders.
     */
    public MessageBuilder message(String path) {
        return new MessageBuilder(this, path);
    }

    /**
     * Reload the configuration from file and clear cache.
     */
    public ConfigManager reload() {
        try {
            cache.clear();
            config.load(configFile);
        } catch (Exception e) {
            // Log error but don't throw - maintain graceful degradation
            System.err.println("Failed to reload config file " + configFile.getName() + ": " + e.getMessage());
        }
        return this;
    }

    /**
     * Check if a path exists in the configuration.
     */
    public boolean contains(String path) {
        return config.contains(path);
    }

    /**
     * Check if the configuration file exists.
     */
    public boolean fileExists() {
        return configFile.exists();
    }

    /**
     * Get the configuration file.
     */
    public File getFile() {
        return configFile;
    }

    /**
     * Clear the cache for this instance.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Clear all cached instances (useful for plugin reloads).
     */
    public static void clearAllInstances() {
        instances.clear();
    }

    /**
     * Builder class for fluent message handling with placeholder replacement.
     */
    public static class MessageBuilder {
        private final ConfigManager configManager;
        private final String path;
        private String message;

        private MessageBuilder(ConfigManager configManager, String path) {
            this.configManager = configManager;
            this.path = path;
        }

        /**
         * Set a default value if the path doesn't exist.
         */
        public MessageBuilder withDefault(String defaultValue) {
            this.message = configManager.getString(path, defaultValue);
            return this;
        }

        /**
         * Replace a placeholder in the message.
         */
        public MessageBuilder replace(String placeholder, Object value) {
            if (message == null) {
                message = configManager.getString(path, "");
            }
            message = message.replace(placeholder, String.valueOf(value));
            return this;
        }

        /**
         * Replace multiple placeholders at once.
         */
        public MessageBuilder replace(String placeholder1, Object value1, String placeholder2, Object value2) {
            return replace(placeholder1, value1).replace(placeholder2, value2);
        }

        /**
         * Get the final message as a string.
         */
        public String asString() {
            return message != null ? message : configManager.getString(path, "");
        }

        /**
         * Get the final message as a MiniMessage Component.
         */
        public Component asMiniMessage() {
            return MiniMessage.miniMessage().deserialize(asString());
        }
    }
}
