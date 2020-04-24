package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

public class IpInfoCommand extends EssentialCommand {
    public IpInfoCommand() {
        super("ipinfo", CommandPermission.IPINFO, 3, new String[]{"ip"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> targetArgument = getUserArgument("user")
                .executes(this::execute);

        commandNode.addChild(targetArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        CommandSourceUser source = getServerUser(ctx);

        essentials.getUserThenAcceptAsync(source.getCommandSource(), getUserArgumentInput(ctx, "user"), (user) -> {
            if (user.getLastSocketAddress() == null) {
                source.sendError(ExceptionMessageNode.NO_VALUE_SET_USER, "lastSocketAddress");
                return;
            }

            source.sendLangMessage("command.ipinfo", user.getUsername(), user.getLastSocketAddress());
        });

        return SUCCESS;
    }

}
