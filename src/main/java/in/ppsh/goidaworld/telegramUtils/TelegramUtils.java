package in.ppsh.goidaworld.telegramUtils;

import in.ppsh.goidaworld.telegramUtils.database.DatabaseManager;
import in.ppsh.goidaworld.telegramUtils.listeners.JoinListener;
import in.ppsh.goidaworld.telegramUtils.listeners.PreventListener;
import in.ppsh.goidaworld.telegramUtils.telegram.BotManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TelegramUtils extends JavaPlugin {
    Logger logger;
    DatabaseManager databaseManager;
    BotManager botManager;
    AuthManager authManager;

    @Override
    public void onEnable() {
        logger = getLogger();

        saveDefaultConfig();
        saveResource("lang.yml", false);

        Logger.getLogger("com.j256.ormlite").setLevel(Level.OFF);

        databaseManager = loadDatabase();
        authManager = new AuthManager(databaseManager, getDataFolder(), logger);
        botManager = new BotManager(
                getConfig().getString("bot.token"),
                getConfig().getString("bot.username"),
                getDataFolder(), logger, databaseManager, authManager
        );
        authManager.setBotManager(botManager);
        CompletableFuture.runAsync(() -> botManager.start());
        Bukkit.getPluginManager().registerEvents(new JoinListener(authManager, logger), this);
        Bukkit.getPluginManager().registerEvents(new PreventListener(authManager, logger), this);
    }

    @Override
    public void onDisable() {
        databaseManager.close();
        botManager.stop();

    }

    DatabaseManager loadDatabase() {
        try {
            return new DatabaseManager(getDataFolder());
        } catch (SQLException e) {
            logger.severe("Failed to load database: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return null;
        }
    }
}
