package org.kilocraft.essentials.commands.teleport.tpr;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.player.UserUtils;

public class TpacceptCommand extends EssentialCommand {
    public TpacceptCommand() {
        super("tpaccept", TpaCommand.PERMISSION);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> selectorArgument = this.getOnlineUserArgument("victim")
                .executes(this::accept);

        this.commandNode.addChild(selectorArgument.build());
    }

    private int accept(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser victim = this.getOnlineUser(ctx);
        OnlineUser sender = this.getOnlineUser(ctx, "victim");

        if (!UserUtils.TpaRequests.hasRequest(sender, victim)) {
            victim.sendLangError("command.tpa.no_requests", sender.getFormattedDisplayName());
            return FAILED;
        }

        boolean toSender = UserUtils.TpaRequests.useRequestAndGetType(sender);
        OnlineUser tpTarget = toSender ? sender : victim;
        (toSender ? victim : sender).teleport(tpTarget);

        victim.sendLangMessage("command.tpa.accepted", sender.getFormattedDisplayName());
        sender.sendLangMessage("command.tpa.accepted.announce", victim.getFormattedDisplayName());

        return SUCCESS;
    }
}
