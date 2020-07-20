package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

import java.util.Date;

public class TempBanCommand extends EssentialCommand {
    public TempBanCommand() {
        super("tempban", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> victim = argument("profile", StringArgumentType.string())
                .suggests(ArgumentCompletions::allPlayers);

        RequiredArgumentBuilder<ServerCommandSource, String> time = argument("time", StringArgumentType.word())
                .suggests(TimeDifferenceUtil::listSuggestions)
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), null, false));

        RequiredArgumentBuilder<ServerCommandSource, String> reason = argument("reason", StringArgumentType.greedyString())
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), StringArgumentType.getString(ctx, "reason"), false));

        LiteralArgumentBuilder<ServerCommandSource> silent = literal("silent")
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), StringArgumentType.getString(ctx, "reason"), true));

        reason.then(silent);
        time.then(reason);
        victim.then(time);
        this.argumentBuilder.then(victim);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, @NotNull final String time, @Nullable final String reason, boolean silent) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        Date date = new Date();
        Date expiry = new Date(TimeDifferenceUtil.parse(time, true));

        super.resolveAndGetProfileAsync(ctx, "profile").thenAcceptAsync((victim) -> {
            BannedPlayerEntry entry = new BannedPlayerEntry(victim, date, src.getName(), expiry, reason);
            super.getServer().getMinecraftServer().getPlayerManager().getUserBanList().add(entry);

            if (super.isOnline(victim.getId())) {
                super.getOnlineUser(victim.getId()).asPlayer().networkHandler.disconnect(
                        new TextMessage(
                                ServerUserManager.replaceVariables(super.config.moderation().disconnectReasons().tempBan, entry, false)
                        ).toText()
                );
            }

            this.getServer().getUserManager().onPunishmentPerformed(src, new Punishment(src, EntityIdentifiable.fromGameProfile(victim), reason), Punishment.Type.BAN, time, silent);

        });

        return AWAIT;
    }


}
