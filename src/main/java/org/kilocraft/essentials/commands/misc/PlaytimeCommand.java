package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.User;

import java.util.concurrent.atomic.AtomicInteger;

public class PlaytimeCommand extends EssentialCommand {
    public PlaytimeCommand() {
        super("playtime", CommandPermission.PLAYTIME_SELF, new String[]{"pt"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> userArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, CommandPermission.PLAYTIME_OTHERS));

        argumentBuilder.executes(this::executeSelf);
        commandNode.addChild(userArgument.build());
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return execute(getServerUser(ctx), getOnlineUser(ctx));
    }

    private int executeOther(CommandContext<ServerCommandSource> ctx) {
        CommandSourceUser src = getServerUser(ctx);
        String inputName = getUserArgumentInput(ctx, "user");

        if (server.getOnlineUser(inputName) != null)
            return execute(src, server.getOnlineUser(inputName));

        AtomicInteger var = new AtomicInteger(AWAIT_RESPONSE);
        essentials.getUserThenAcceptAsync(src, getUserArgumentInput(ctx, "user"), (user) -> {
            var.set(execute(src, user));
        });

        return var.get();
    }

    private int execute(CommandSourceUser src, User target) {


        return SINGLE_SUCCESS;
    }

}
