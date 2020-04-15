package org.kilocraft.essentials.commands.teleport.tpr;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.player.UserUtils;

public class TpacceptCommand extends EssentialCommand {
    public TpacceptCommand() {
        super("tpaccept", CommandPermission.TELEPORTREQUEST);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> selectorArgument = this.getOnlineUserArgument("target")
                .executes(this::accept);

        this.commandNode.addChild(selectorArgument.build());
    }

    private int accept(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        OnlineUser target = this.getOnlineUser(ctx, "target");

        if (!UserUtils.TpaRequests.hasRequest(src, target)) {
            src.sendLangError("command.tpa.no_requests", target.getFormattedDisplayName());
            return SINGLE_FAILED;
        }

        boolean toSender = UserUtils.TpaRequests.useRequest(src);
        OnlineUser tpTarget = toSender ? src : target;
        (toSender ? target : src).teleport(tpTarget.getLocation(), true);

        src.sendLangMessage("command.tpa.accepted.announce", target.getFormattedDisplayName());
        target.sendLangMessage("command.tpa.accepted", src.getFormattedDisplayName());

        return SINGLE_SUCCESS;
    }
}
