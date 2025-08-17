package in.ppsh.goidaworld.telegramUtils;

import in.ppsh.goidaworld.telegramUtils.database.AuthUser;
import in.ppsh.goidaworld.telegramUtils.database.DatabaseManager;
import in.ppsh.goidaworld.telegramUtils.database.LogInStatus;
import in.ppsh.goidaworld.telegramUtils.database.Login;
import in.ppsh.goidaworld.telegramUtils.telegram.BotManager;
import in.ppsh.goidaworld.telegramUtils.utils.ConfigManager;
import in.ppsh.goidaworld.telegramUtils.utils.FreezeManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.io.File;


public class AuthManager {

    private final DatabaseManager databaseManager;
    private final BotManager botManager;
    private final ConfigManager langConfig;
    private final FreezeManager freezeManager;


    public AuthManager(DatabaseManager databaseManager, FreezeManager freezeManager, BotManager botManager, File workingDir) {
        this.databaseManager = databaseManager;
        this.freezeManager = freezeManager;
        this.botManager = botManager;

        langConfig = new ConfigManager("lang.yml", workingDir);

    }

    public void auth(Player player, String ip) {
        AuthUser user = databaseManager.userService.getUser(player.getUniqueId());

        if (user == null) {
            registerPlayer(player, ip);
            return;
        }

        Login login = databaseManager.loginService.createLogin(ip, user);

        if (user.getTelegramId() == 0) {
            freezeManager.freezePlayer(player.getUniqueId());
            sendRegisterInstructions(player, login.getId());
            return;
        }

        if (databaseManager.loginService.hasBannedLogin(ip, user)) {
            login.setStatus(LogInStatus.BANNED);
            databaseManager.loginService.updateLogin(login);
            player.kick(langConfig.getMiniMessage("minecraft.auth_denied_kick", "You are banned from using this server."));
            return;
        }

        if (!databaseManager.loginService.hasAcceptedLogin(ip, user)) {
            freezeManager.freezePlayer(player.getUniqueId());
            sendLoginRequest(user, player, ip, login.getId());
            return;
        }

        login.setStatus(LogInStatus.ACCEPTED);
        databaseManager.loginService.updateLogin(login);
        botManager.sendBanAsk(login);
    }


    public void registerPlayer(Player player, String ip) {
        freezeManager.freezePlayer(player.getUniqueId());
        AuthUser user = databaseManager.userService.createUser(player.getUniqueId());
        Login login = databaseManager.loginService.createLogin(ip, user);
        sendRegisterInstructions(player, login.getId());

    }



    private void sendRegisterInstructions(Player player, long loginId) {
        String botUsername = botManager.username;

        player.sendMessage(langConfig.getMiniMessage("minecraft.auth", "auth_required"));

        String messageTemplate = langConfig.getConfig().getString("minecraft.auth_click", "auth_instructions");
        String finalMessage = messageTemplate.replace("{url}", "https://t.me/" + botUsername + "?start=" + loginId);

        player.sendMessage(MiniMessage.miniMessage().deserialize(finalMessage));
    }

    private void sendLoginRequest(AuthUser user, Player player, String ip, long loginId) {
        player.sendRichMessage(langConfig.getConfig().getString("minecraft.auth_reauth", "auth_required"));
        player.sendRichMessage(langConfig.getConfig().getString("minecraft.auth_click", "auth_instructions")
                .replace("{url}", "https://t.me/" + botManager.username));
        botManager.sendRequest(user.getTelegramId(), ip, player.getName(), loginId);

    }

}
