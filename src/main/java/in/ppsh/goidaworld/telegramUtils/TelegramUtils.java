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
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TelegramUtils extends JavaPlugin {
    Logger logger;
    DatabaseManager databaseManager;
    BotManager botManager;
    AuthManager authManager;
    FreezeManager freezeManager;
    private ConfigManager mainConfig;

    @Override
    public void onEnable() {
        logger = getLogger();

        saveDefaultConfig();
        saveResource("lang.yml", false);
        
        // Initialize enhanced config manager for main config
        mainConfig = ConfigManager.getMainConfig(getDataFolder());

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
        
        // Clear cached config instances for fresh reload
        ConfigManager.clearAllInstances();
        mainConfig = ConfigManager.getMainConfig(getDataFolder());

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

    @SneakyThrows
    private void initializeDatabase() {
        databaseManager = new DatabaseManager(getDataFolder());
    }

    private void initializeBot() {
        botManager = new BotManager(
                mainConfig.getString("bot.token"),
                mainConfig.getString("bot.username"),
                mainConfig.getLong("bot.group_id"),
                getDataFolder(), logger, databaseManager.loginService, databaseManager.userService, freezeManager, this
        );
        CompletableFuture.runAsync(() -> botManager.start());
    }

    private void initializeAuthManager() {
        authManager = new AuthManager(databaseManager.loginService, databaseManager.userService, freezeManager, botManager, getDataFolder());
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new JoinListener(authManager, logger), this);
        Bukkit.getPluginManager().registerEvents(new PreventListener(freezeManager, logger), this);
    }

    private void initializeFreezeManager() { freezeManager = new FreezeManager(logger); }

    private void registerCommands() {
        LiteralCommandNode<CommandSourceStack> tgUtilsCommand = new TgUtilsCommand(ConfigManager.getLangConfig(getDataFolder()), this)
                .createCommand()
                .build();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, command -> command.registrar().register(tgUtilsCommand));
    }

}
