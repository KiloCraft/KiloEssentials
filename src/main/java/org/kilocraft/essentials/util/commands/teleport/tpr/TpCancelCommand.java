package org.kilocraft.essentials.util.commands.teleport.tpr;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.player.UserUtils;

public class TpCancelCommand extends EssentialCommand {
    public TpCancelCommand() {
        super("tpcancel", TpaCommand.PERMISSION);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> selectorArgument = this.getOnlineUserArgument("target")
                .executes(this::cancel);

        this.commandNode.addChild(selectorArgument.build());
    }

    private int cancel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        OnlineUser target = this.getOnlineUser(ctx, "target");

        if (!UserUtils.TpaRequests.hasRequest(src, target)) {
            src.sendLangError("command.tpa.no_requests", target.getFormattedDisplayName());
            return FAILED;
        }

        UserUtils.TpaRequests.remove(src);
        src.sendLangError("command.tpa.cancel");
        target.sendLangError("command.tpa.cancel.announce", src.getFormattedDisplayName());

        return SUCCESS;
    }
}
