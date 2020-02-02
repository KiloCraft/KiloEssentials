package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

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

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        CommandSourceUser source = getServerUser(ctx);

        essentials.getUserThenAcceptAsync(source.getCommandSource(), getString(ctx, "user"), (user) -> {
            source.sendLangMessage("command.ipinfo", user.getNameTag(), user.getLastSocketAddress());
        });

        return SINGLE_SUCCESS;
    }

}
