package in.ppsh.goidaworld.telegramUtils.telegram;

import in.ppsh.goidaworld.telegramUtils.AuthManager;
import in.ppsh.goidaworld.telegramUtils.database.AuthUser;
import in.ppsh.goidaworld.telegramUtils.database.DatabaseManager;
import in.ppsh.goidaworld.telegramUtils.database.LogInStatus;
import in.ppsh.goidaworld.telegramUtils.database.Login;
import in.ppsh.goidaworld.telegramUtils.utils.ConfigManager;
import io.github.natanimn.BotClient;
import io.github.natanimn.enums.ParseMode;
import io.github.natanimn.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.types.keyboard.InlineKeyboardMarkup;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotManager {
    BotClient bot;
    ConfigManager langConfig;
    public String username;
    DatabaseManager databaseManager;
    AuthManager authManager;

    public BotManager(String token, String username, File workingDir, Logger logger, DatabaseManager databaseManager, AuthManager authManager) {
        langConfig = new ConfigManager("lang.yml", workingDir, logger);
        bot = new BotClient(token);
        this.username = username;

        bot.onMessage(filter -> filter.regex("^/start\\s+([0-9a-fA-F\\-]{36})$"), (context, message) -> {
            UUID uuid = UUID.fromString(message.text.split("\\s+")[1]);
            AuthUser authUser = databaseManager.getAuthUser(uuid);

            if (authUser == null || authUser.getTelegramId() != 0) {
                context.sendMessage(message.chat.id, langConfig.getConfig().getString("bot.failed")).exec();
                return;
            }

            authUser.setTelegramId(message.from.id);
            try {
                databaseManager.getAuthDao().update(authUser);
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error updating auth user", e);
            }
            context.sendMessage(message.chat.id, langConfig.getConfig().getString("bot.success")).exec();
            authManager.unfreezePlayer(authUser.getUuid());
        });

        bot.onCallback(filter -> filter.customFilter(new CallbackQueryButtonFilter()), (context, callback) -> {
            String action = callback.data.split(":")[0];
            long loginId = Long.parseLong(callback.data.split(":")[1]);

            context.editMessageReplyMarkup(callback.message.chat.id, callback.message.message_id)
                    .replyMarkup(new InlineKeyboardMarkup()).exec();

            Login login;
            try {
                login = databaseManager.getLoginDao().queryForId(loginId);
            } catch (SQLException e) {
                logger.warning("Error fetching login with ID " + loginId + ": " + e.getMessage());
                return;
            }

            if (login == null) {
                context.answerCallbackQuery(callback.id, "Login not found").exec();
                return;
            }

            if (action.equals("accept")) {
                authManager.unfreezePlayer(login.getUser().getUuid());
                login.setStatus(LogInStatus.ACCEPTED);
                try {
                    databaseManager.getLoginDao().update(login);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (action.equals("reject")) {
                try {
                    Objects.requireNonNull(Bukkit.getPlayer(login.getUser().getUuid())).kick();
                    login.setStatus(LogInStatus.BANNED);
                    databaseManager.getLoginDao().update(login);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void start() {
        bot.startPolling();
    }

    public void stop() {
        bot.stop();
    }

    public void sendRequest(long telegramId, String ip, String nickname, long loginId) {
        String message = langConfig.getConfig().getString("bot.reauth_request", "accept {ip}")
                .replace("{ip}", ip)
                .replace("{nickname}", nickname);

        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();

        inlineMarkup.addKeyboard(
                new InlineKeyboardButton(langConfig.getConfig().getString("bot.confirm_button", "Да"), "accept:" + loginId),
                new InlineKeyboardButton(langConfig.getConfig().getString("bot.deny_button", "Нет"), "reject:" + loginId)
        );
        bot.context.sendMessage(telegramId, message).replyMarkup(inlineMarkup).parseMode(ParseMode.HTML).exec();
    }
}
