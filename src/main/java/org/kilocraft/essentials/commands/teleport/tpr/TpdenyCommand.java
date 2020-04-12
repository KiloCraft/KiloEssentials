package org.kilocraft.essentials.commands.teleport.tpr;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.UserUtils;

public class TpdenyCommand extends EssentialCommand {
    public TpdenyCommand() {
        super("tpdeny", CommandPermission.TELEPORTREQUEST);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> selectorArgument = this.getOnlineUserArgument("target")
                .executes(this::deny);

        this.commandNode.addChild(selectorArgument.build());
    }

    private int deny(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        OnlineUser target = this.getOnlineUser(ctx, "target");

        if (!UserUtils.TpaRequests.hasRequest(src, target)) {
            return src.sendLangError("command.tpa.no_requests", target.getFormattedDisplayName());
        }

        UserUtils.TpaRequests.remove(src);
        src.sendLangError("command.tpa.denied", target.getFormattedDisplayName());
        target.sendLangError("command.tpa.denied.announce", src.getFormattedDisplayName());

        return SINGLE_SUCCESS;
    }
}
