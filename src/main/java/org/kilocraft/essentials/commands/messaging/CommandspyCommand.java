package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.commands.CmdUtils;

public class CommandspyCommand extends EssentialCommand {
    public CommandspyCommand() {
        super("commandspy", src -> !CmdUtils.isConsole(src) && KiloEssentials.hasPermissionNode(src, EssentialPermission.SPY_COMMAND));
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        if (ServerChat.isCommandSpy(ctx.getSource().getPlayer())) {
            ServerChat.removeCommandSpy(ctx.getSource().getPlayer());
            ctx.getSource().sendFeedback(new LiteralText("CommandSpy is now inactive").formatted(Formatting.YELLOW), false);
            return SINGLE_SUCCESS;
        }

        ServerChat.addCommandSpy(ctx.getSource().getPlayer());
        ctx.getSource().sendFeedback(new LiteralText("CommandSpy is now active").formatted(Formatting.YELLOW), false);
        return SINGLE_SUCCESS;
    }
}
