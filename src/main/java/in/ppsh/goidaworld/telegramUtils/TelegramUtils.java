package in.ppsh.goidaworld.telegramUtils;

import in.ppsh.goidaworld.telegramUtils.database.DatabaseManager;
import in.ppsh.goidaworld.telegramUtils.listeners.JoinListener;
import in.ppsh.goidaworld.telegramUtils.listeners.PreventListener;
import in.ppsh.goidaworld.telegramUtils.telegram.BotManager;
import in.ppsh.goidaworld.telegramUtils.utils.FreezeManager;
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
    FreezeManager freezeManager;

    @Override
    public void onEnable() {
        logger = getLogger();

        saveDefaultConfig();
        saveResource("lang.yml", false);

        Logger.getLogger("com.j256.ormlite").setLevel(Level.SEVERE);

        initializeFreezeManager();

        initializeDatabase();
        initializeBot();
        initializeAuthManager();

        registerListeners();
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) { databaseManager.close(); }
        if (botManager != null) { botManager.stop(); }

    }

    private void initializeDatabase() {
        try {
            databaseManager = new DatabaseManager(getDataFolder());
        } catch (SQLException e) {
            logger.severe("Failed to load database: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void initializeBot() {
        botManager = new BotManager(
                getConfig().getString("bot.token"),
                getConfig().getString("bot.username"),
                getDataFolder(), logger, databaseManager, freezeManager, this
        );
        CompletableFuture.runAsync(() -> botManager.start());
    }

    private void initializeAuthManager() {
        authManager = new AuthManager(databaseManager, freezeManager, botManager, getDataFolder(), logger);
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new JoinListener(authManager, logger), this);
        Bukkit.getPluginManager().registerEvents(new PreventListener(freezeManager, logger), this);
    }

    private void initializeFreezeManager() { freezeManager = new FreezeManager(logger); }

}
