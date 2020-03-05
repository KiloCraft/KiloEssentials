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

public class SocialspyCommand extends EssentialCommand {
    public SocialspyCommand() {
        super("socialspy", src -> !CmdUtils.isConsole(src) && KiloEssentials.hasPermissionNode(src, EssentialPermission.SPY_CHAT));
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }
    
    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        if (ServerChat.isSocialSpy(ctx.getSource().getPlayer())) {
            ServerChat.removeSocialSpy(ctx.getSource().getPlayer());
            ctx.getSource().sendFeedback(new LiteralText("SocialSpy is now inactive").formatted(Formatting.YELLOW), false);
            return SINGLE_SUCCESS;
        }

        ServerChat.addSocialSpy(ctx.getSource().getPlayer());
        ctx.getSource().sendFeedback(new LiteralText("SocialSpy is now active").formatted(Formatting.YELLOW), false);
        return SINGLE_SUCCESS;
    }
}
