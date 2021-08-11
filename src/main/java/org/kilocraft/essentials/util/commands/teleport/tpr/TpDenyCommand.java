package org.kilocraft.essentials.util.commands.teleport.tpr;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.player.UserUtils;

public class TpDenyCommand extends EssentialCommand {
    public TpDenyCommand() {
        super("tpdeny", TpaCommand.PERMISSION);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> selectorArgument = this.getOnlineUserArgument("victim")
                .executes(this::deny);

        this.commandNode.addChild(selectorArgument.build());
    }

    private int deny(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        OnlineUser target = this.getOnlineUser(ctx, "victim");

        if (!UserUtils.TpaRequests.hasRequest(target, src)) {
            src.sendLangError("command.tpa.no_requests", target.getFormattedDisplayName());
            return FAILED;
        }

        UserUtils.TpaRequests.remove(src);
        src.sendLangError("command.tpa.denied", target.getFormattedDisplayName());
        target.sendLangError("command.tpa.denied.announce", src.getFormattedDisplayName());

        return SUCCESS;
    }
}
