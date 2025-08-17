package in.ppsh.goidaworld.telegramUtils.telegram;

import in.ppsh.goidaworld.telegramUtils.TelegramUtils;
import in.ppsh.goidaworld.telegramUtils.database.*;
import in.ppsh.goidaworld.telegramUtils.utils.ConfigManager;
import in.ppsh.goidaworld.telegramUtils.utils.FreezeManager;
import io.github.natanimn.BotContext;
import io.github.natanimn.enums.ParseMode;
import io.github.natanimn.types.keyboard.InlineKeyboardMarkup;
import io.github.natanimn.types.updates.CallbackQuery;
import io.github.natanimn.types.updates.Message;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public record BotHandlers(Logger logger, LoginService loginService, UserService userService, FreezeManager freezeManager,
                          ConfigManager langConfig, TelegramUtils plugin) {

    public void handleRegister(BotContext context, Message message) {
        long loginId = Long.parseLong(message.text.split("\\s+")[1]);

        Login login = loginService.getLogin(loginId);

        if (login == null) { return; }

        AuthUser authUser = login.getUser();

        if (authUser == null || authUser.getTelegramId() != 0) {
            context.sendMessage(message.chat.id, langConfig.getConfig().getString("bot.failed")).exec();
            return;
        }

        authUser.setTelegramId(message.from.id);
        userService.updateUser(authUser);
        freezeManager.unfreezePlayer(authUser.getUuid());

        login.setStatus(LogInStatus.ACCEPTED);
        loginService.updateLogin(login);

        context.sendMessage(message.chat.id, langConfig.getConfig().getString("bot.success")).exec();
    }

    public void handleNewIpCallback(BotContext context, CallbackQuery callback) {
        String action = callback.data.split(":")[0];
        long loginId = Long.parseLong(callback.data.split(":")[1]);

        context.editMessageReplyMarkup(callback.message.chat.id, callback.message.message_id)
                .replyMarkup(new InlineKeyboardMarkup()).exec();

        Login login = loginService.getLogin(loginId);

        if (login == null) {
            context.answerCallbackQuery(callback.id, "Login not found").exec();
            return;
        }

        if (action.equals("accept")) {
            freezeManager.unfreezePlayer(login.getUser().getUuid());
            login.setStatus(LogInStatus.ACCEPTED);
            loginService.updateLogin(login);
        } else if (action.equals("reject")) {
            banIp(context, callback, login);
        }
    }

    @SneakyThrows
    private void banIp(BotContext context, CallbackQuery callback, Login login) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(login.getUser().getUuid());
            if (player != null && player.isOnline()) {
                player.kick(langConfig.getMiniMessage("minecraft.auth_denied_kick", "You are banned from using this server."));
            }
        });

        login.setStatus(LogInStatus.BANNED);
        loginService.updateLogin(login);

        context.sendMessage(
                callback.message.chat.id, langConfig.getMessage("bot.access_denied", "Blocked").replace("{ip}", login.getIp())
        ).parseMode(ParseMode.HTML).exec();
    }

    public void handleBanCallback(BotContext context, CallbackQuery callback) {
        var loginId = Long.parseLong(callback.data.split(":")[1]);
        var login = loginService.getLogin(loginId);

        banIp(context, callback, login);

    }

    public void handleListCommand(BotContext context, Message message) {
        String playerList = langConfig.getMessage("bot.player_list", "Online players:\n") +
                Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .map(name-> { return "• " + name;} )
                        .collect(Collectors.joining("\n"));
        context.sendMessage(message.chat.id, playerList).parseMode(ParseMode.HTML).exec();
    }
}
