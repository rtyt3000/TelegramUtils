package in.ppsh.goidaworld.telegramUtils;

import in.ppsh.goidaworld.telegramUtils.database.AuthUser;
import in.ppsh.goidaworld.telegramUtils.database.DatabaseManager;
import in.ppsh.goidaworld.telegramUtils.database.LogInStatus;
import in.ppsh.goidaworld.telegramUtils.database.Login;
import in.ppsh.goidaworld.telegramUtils.telegram.BotManager;
import in.ppsh.goidaworld.telegramUtils.utils.ConfigManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;


public class AuthManager {

    private final DatabaseManager databaseManager;
    private BotManager botManager;
    private final Logger logger;
    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final ConfigManager langConfig;


    public AuthManager(DatabaseManager databaseManager, File workingDir, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
        langConfig = new ConfigManager("lang.yml", workingDir, logger);

    }

    public void setBotManager(BotManager botManager) {
        this.botManager = botManager;
    }

    public void auth(Player player, String ip) throws SQLException {
        if (!isAuthorized(player)) {
            registerPlayer(player, ip);
            return;
        }

        AuthUser user = databaseManager.getAuthDao().queryForId(player.getUniqueId());

        Login login = new Login(ip, user);
        databaseManager.getLoginDao().create(login);

        if (user.getTelegramId() == 0) {
            freezePlayer(player.getUniqueId());
            sendRegisterInstructions(player, login.getId());
            return;
        }

        if (hasBannedLogin(ip, user)) {
            login.setStatus(LogInStatus.BANNED);
            databaseManager.getLoginDao().update(login);
            player.kick(MiniMessage.miniMessage().deserialize(langConfig.getConfig().getString("minecraft.auth_denied_kick", "You are banned from using this server.")));
            return;
        }

        if (!hasAcceptedLogin(ip, user)) {
            freezePlayer(player.getUniqueId());
            sendLoginRequest(user, player, ip, login.getId());
            return;
        }

        login.setStatus(LogInStatus.ACCEPTED);
        databaseManager.getLoginDao().update(login);
        botManager.sendBanAsk(login.getId());
    }

    public boolean hasAcceptedLogin(String ip, AuthUser user) throws SQLException {
        return databaseManager.getLoginDao().queryBuilder()
                .where().eq("ip", ip).and().eq("uuid", user.getUuid())
                .and().eq("status", LogInStatus.ACCEPTED).countOf() > 0;
    }

    public boolean hasBannedLogin(String ip, AuthUser user) throws SQLException {
        return databaseManager.getLoginDao().queryBuilder()
                .where().eq("ip", ip).and().eq("uuid", user.getUuid())
                .and().eq("status", LogInStatus.BANNED).countOf() > 0;
    }

    public boolean isAuthorized(Player player) throws SQLException {
        AuthUser user = databaseManager.getAuthDao().queryForId(player.getUniqueId());
        return user != null;
    }

    public void registerPlayer(Player player, String ip) throws SQLException {
        freezePlayer(player.getUniqueId());
        AuthUser user = new AuthUser(player.getUniqueId());
        databaseManager.getAuthDao().create(user);
        Login login = new Login(ip, user);
        databaseManager.getLoginDao().create(login);
        sendRegisterInstructions(player, login.getId());

    }

    public void freezePlayer(UUID uuid) {
        frozenPlayers.add(uuid);
        logger.info("Player " + uuid + " has been frozen.");
    }

    public void unfreezePlayer(UUID uuid) {
        frozenPlayers.remove(uuid);
        logger.info("Player " + uuid + " has been unfrozen.");
    }

    public boolean isPlayerFrozen(UUID uuid) {
        return frozenPlayers.contains(uuid);
    }

    private void sendRegisterInstructions(Player player, long loginId) {
        String botUsername = botManager.username;

        player.sendRichMessage(langConfig.getConfig().getString("minecraft.auth", "auth_required"));
        player.sendRichMessage(langConfig.getConfig().getString("minecraft.auth_click", "auth_instructions")
                .replace("{url}", "https://t.me/" + botUsername + "?start=" + loginId));
    }

    private void sendLoginRequest(AuthUser user, Player player, String ip, long loginId) {
        player.sendRichMessage(langConfig.getConfig().getString("minecraft.auth_reauth", "auth_required"));
        player.sendRichMessage(langConfig.getConfig().getString("minecraft.auth_click", "auth_instructions")
                .replace("{url}", "https://t.me/" + botManager.username));
        botManager.sendRequest(user.getTelegramId(), ip, player.getName(), loginId);

    }

}
