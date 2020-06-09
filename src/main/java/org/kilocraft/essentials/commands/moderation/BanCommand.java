package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

import java.util.Date;

public class BanCommand extends EssentialCommand {
    public BanCommand() {
        super("ke_ban", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, String> userArgument = this.getUserArgument("victim")
                .executes(ctx -> this.execute(ctx, null, null));
        final RequiredArgumentBuilder<ServerCommandSource, String> reasonArgument = argument("reason", StringArgumentType.string())
                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), null));
        final RequiredArgumentBuilder<ServerCommandSource, String> lengthArgument = argument("length", StringArgumentType.string()).suggests(TimeDifferenceUtil::listSuggestions)
                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), StringArgumentType.getString(ctx, "length")));
        reasonArgument.then(lengthArgument);
        userArgument.then(reasonArgument);
        this.argumentBuilder.then(userArgument);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, @Nullable String reason, String expiryString) throws CommandSyntaxException {
        final CommandSourceUser src = this.getServerUser(ctx);
        final String input = this.getUserArgumentInput(ctx, "victim");
        Date expiry = expiryString == null ? null : new Date(TimeDifferenceUtil.parse(expiryString, true));

        this.essentials.getUserThenAcceptAsync(src, input, (victim) -> {
            Punishment punishment = new Punishment(
                    src,
                    victim,
                    null,
                    reason,
                    expiry
            );
            this.server.getUserManager().performPunishment(punishment, Punishment.Type.DENY_ACCESS, (result) -> {
            });
        });
        return AWAIT;
    }
}
