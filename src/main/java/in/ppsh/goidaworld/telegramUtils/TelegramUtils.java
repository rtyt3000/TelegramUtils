package in.ppsh.goidaworld.telegramUtils;

import com.mojang.brigadier.tree.LiteralCommandNode;
import in.ppsh.goidaworld.telegramUtils.commands.TgUtilsCommand;
import in.ppsh.goidaworld.telegramUtils.database.DatabaseManager;
import in.ppsh.goidaworld.telegramUtils.listeners.JoinListener;
import in.ppsh.goidaworld.telegramUtils.listeners.PreventListener;
import in.ppsh.goidaworld.telegramUtils.telegram.BotManager;
import in.ppsh.goidaworld.telegramUtils.utils.ConfigManager;
import in.ppsh.goidaworld.telegramUtils.utils.FreezeManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
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
        registerCommands();
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) { databaseManager.close(); }
        if (botManager != null) { botManager.stop(); }
        if (freezeManager != null) { freezeManager.clear(); }

    }

    public void reload() {
        saveDefaultConfig();

        if (databaseManager != null) {
            databaseManager.close();
        }
        // TODO: Fix bot stopping
        // if (botManager != null) {
        // botManager.stop();
        // }
        if (freezeManager != null) { freezeManager.clear(); }

        initializeFreezeManager();
        initializeDatabase();
//        initializeBot();
        initializeAuthManager();

        registerListeners();

        logger.info("Plugin reloaded successfully!");
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
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            botManager.start();
        });
    }

    private void initializeAuthManager() {
        authManager = new AuthManager(databaseManager, freezeManager, botManager, getDataFolder());
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new JoinListener(authManager, logger), this);
        Bukkit.getPluginManager().registerEvents(new PreventListener(freezeManager, logger), this);
    }

    private void initializeFreezeManager() { freezeManager = new FreezeManager(logger); }

    private void registerCommands() {
        LiteralCommandNode<CommandSourceStack> tgUtilsCommand = new TgUtilsCommand(new ConfigManager("lang.yml", getDataFolder()), this)
                .createCommand()
                .build();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, command -> command.registrar().register(tgUtilsCommand));
    }

}
