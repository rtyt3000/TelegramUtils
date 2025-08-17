package in.ppsh.goidaworld.telegramUtils.telegram;

import in.ppsh.goidaworld.telegramUtils.TelegramUtils;
import in.ppsh.goidaworld.telegramUtils.database.AuthUser;
import in.ppsh.goidaworld.telegramUtils.database.DatabaseManager;
import in.ppsh.goidaworld.telegramUtils.database.LogInStatus;
import in.ppsh.goidaworld.telegramUtils.database.Login;
import in.ppsh.goidaworld.telegramUtils.utils.ConfigManager;
import in.ppsh.goidaworld.telegramUtils.utils.FreezeManager;
import io.github.natanimn.BotContext;
import io.github.natanimn.enums.ParseMode;
import io.github.natanimn.types.keyboard.InlineKeyboardMarkup;
import io.github.natanimn.types.updates.CallbackQuery;
import io.github.natanimn.types.updates.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public record BotHandlers(Logger logger, DatabaseManager databaseManager, FreezeManager freezeManager,
                          ConfigManager langConfig, TelegramUtils plugin) {

    public void handleRegister(BotContext context, Message message) {
        long loginId = Long.parseLong(message.text.split("\\s+")[1]);

        Login login = databaseManager.loginService.getLogin(loginId);

        if (login == null) { return; }

        AuthUser authUser = login.getUser();

        if (authUser == null || authUser.getTelegramId() != 0) {
            context.sendMessage(message.chat.id, langConfig.getConfig().getString("bot.failed")).exec();
            return;
        }

        authUser.setTelegramId(message.from.id);
        databaseManager.userService.updateUser(authUser);
        freezeManager.unfreezePlayer(authUser.getUuid());

        login.setStatus(LogInStatus.ACCEPTED);
        databaseManager.loginService.updateLogin(login);

        context.sendMessage(message.chat.id, langConfig.getConfig().getString("bot.success")).exec();
    }

    public void handleNewIpCallback(BotContext context, CallbackQuery callback) {
        String action = callback.data.split(":")[0];
        long loginId = Long.parseLong(callback.data.split(":")[1]);

        context.editMessageReplyMarkup(callback.message.chat.id, callback.message.message_id)
                .replyMarkup(new InlineKeyboardMarkup()).exec();

        Login login = databaseManager.loginService.getLogin(loginId);

        if (login == null) {
            context.answerCallbackQuery(callback.id, "Login not found").exec();
            return;
        }


        if (action.equals("accept")) {
            freezeManager.unfreezePlayer(login.getUser().getUuid());
            login.setStatus(LogInStatus.ACCEPTED);
            databaseManager.loginService.updateLogin(login);
        } else if (action.equals("reject")) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player player = Bukkit.getPlayer(login.getUser().getUuid());
                if (player != null) { player.kick(); }
            });
            login.setStatus(LogInStatus.BANNED);
            databaseManager.loginService.updateLogin(login);
            context.sendMessage(
                    callback.message.chat.id, langConfig.getMessage("bot.access_denied", "Blocked").replace("{ip}", login.getIp())
            ).parseMode(ParseMode.HTML).exec();
        }
    }

    public void handleBanCallback(BotContext context, CallbackQuery callbackQuery) {

    }
}
