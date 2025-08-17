package in.ppsh.goidaworld.telegramUtils.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import in.ppsh.goidaworld.telegramUtils.TelegramUtils;
import in.ppsh.goidaworld.telegramUtils.utils.ConfigManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public record TgUtilsCommand(ConfigManager langConfig, TelegramUtils plugin) {

    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("tgutils")
                .requires(sender -> sender.getSender().isOp())
                .executes(this::sendHelp)
                    .then(Commands.literal("help")
                            .requires(sender -> sender.getSender().isOp())
                            .executes(this::sendHelp))
                    .then(Commands.literal("reload")
                            .requires(sender -> sender.getSender().isOp())
                            .requires(Commands.restricted(source -> true))
                            .executes(this::reload)
                    );
    }

    private int reload(CommandContext<CommandSourceStack> ctx) {
        plugin.reload();

        ctx.getSource().getSender().sendMessage(langConfig.getMiniMessage("minecraft.reload", "Plugin reloaded successfully!"));
        return Command.SINGLE_SUCCESS;
    }

    private int sendHelp(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().getSender().sendMessage(langConfig.getMiniMessage("minecraft.help", "tgutils"));
        return Command.SINGLE_SUCCESS;

    }
}
