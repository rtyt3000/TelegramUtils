package in.ppsh.goidaworld.telegramUtils.telegram;

import in.ppsh.goidaworld.telegramUtils.TelegramUtils;
import in.ppsh.goidaworld.telegramUtils.database.DatabaseManager;
import in.ppsh.goidaworld.telegramUtils.database.Login;
import in.ppsh.goidaworld.telegramUtils.utils.ConfigManager;
import in.ppsh.goidaworld.telegramUtils.utils.FreezeManager;
import io.github.natanimn.BotClient;
import io.github.natanimn.enums.ParseMode;
import io.github.natanimn.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.types.keyboard.InlineKeyboardMarkup;

import java.io.File;
import java.util.logging.Logger;

public class BotManager {
    private final BotClient bot;
    private final DatabaseManager databaseManager;
    private final ConfigManager langConfig;
    public final String username;

    public BotManager(String token, String username, File workingDir, Logger logger, DatabaseManager databaseManager, FreezeManager freezeManager, TelegramUtils plugin) {
        langConfig = new ConfigManager("lang.yml", workingDir, logger);
        bot = new BotClient(token);
        this.username = username;
        this.databaseManager = databaseManager;

        BotHandlers handlers = new BotHandlers(logger, databaseManager, freezeManager, langConfig, plugin);

        bot.onMessage(filter -> filter.regex("^/start\\s+(\\d+)$"), handlers::handleRegister);

        bot.onCallback(filter -> filter.customFilter(new CallbackQueryButtonFilter()), handlers::handleCallback );
    }

    public void start() { bot.startPolling();}

    public void stop() { bot.stop(); }

    public void sendRequest(long telegramId, String ip, String nickname, long loginId) {
        String message = langConfig.getMessage("bot.reauth_request", "Its you ({nickname}): {ip}?")
                .replace("{ip}", ip)
                .replace("{nickname}", nickname);

        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();

        inlineMarkup.addKeyboard(
                new InlineKeyboardButton(langConfig.getMessage("bot.confirm_button", "Yes"), "accept:" + loginId),
                new InlineKeyboardButton(langConfig.getMessage("bot.deny_button", "No"), "reject:" + loginId)
        );
        bot.context.sendMessage(telegramId, message)
                .replyMarkup(inlineMarkup)
                .parseMode(ParseMode.HTML)
                .exec();
    }

    public void sendBanAsk(Login login) {
        String message = langConfig.getMessage("bot.ban_ask", "An attempt to log into your account from the confirmed IP address {ip} has been detected. You can block this IP address at any time if it is not you.")
                .replace("{ip}", login.getIp());
        String buttonText = langConfig.getMessage("bot.ban_button", "Ban");

        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
        inlineMarkup.addKeyboard(new InlineKeyboardButton(buttonText, "ban:" + login.getId()));


        bot.context.sendMessage(login.getUser().getTelegramId(), message)
                .replyMarkup(inlineMarkup)
                .parseMode(ParseMode.HTML)
                .exec();
    }
}
