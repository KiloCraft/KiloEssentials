package org.kilocraft.essentials.commands.user;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

public class LastseenCommand extends EssentialCommand {
    public LastseenCommand() {
        super("lastseen", CommandPermission.LASTSEEN);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> userArgument = this.getUserArgument("user")
                .executes(this::execute);

        this.commandNode.addChild(userArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);

        essentials.getUserThenAcceptAsync(src, this.getUserArgumentInput(ctx, "user"), (user) -> {
            if (user.isOnline()) {
                src.sendLangError("command.lastseen.online", user.getFormattedDisplayName());
                return;
            }

            if (user.getLastOnline() == null) {
                src.sendError(ExceptionMessageNode.NO_VALUE_SET_USER, "lastOnline");
                return;
            }

            String string = TimeDifferenceUtil.formatDateDiff(user.getLastOnline().getTime());
            src.sendLangMessage("command.lastseen", user.getNameTag(), string);
        });

        return AWAIT_RESPONSE;
    }
}
