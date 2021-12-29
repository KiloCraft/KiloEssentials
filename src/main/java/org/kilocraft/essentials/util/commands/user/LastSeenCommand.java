package org.kilocraft.essentials.util.commands.user;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

public class LastSeenCommand extends EssentialCommand {
    public LastSeenCommand() {
        super("lastseen", CommandPermission.LASTSEEN);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> userArgument = this.getUserArgument("user")
                .executes(this::execute);

        this.commandNode.addChild(userArgument.build());
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);

        this.getUserManager().getUserThenAcceptAsync(src, this.getUserArgumentInput(ctx, "user"), (user) -> {
            if (user.isOnline()) {
                src.sendLangError("command.lastseen.online", user.getFormattedDisplayName());
                return;
            }

            if (user.getLastOnline() == null) {
                src.sendLangError("exception.no_value_set_user", "lastOnline");
                return;
            }

            String string = TimeDifferenceUtil.formatDateDiff(user.getLastOnline().getTime());
            src.sendLangMessage("command.lastseen", user.getNameTag(), string);
        });

        return AWAIT;
    }
}
