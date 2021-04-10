package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.events.player.PlayerMutedEventImpl;
import org.kilocraft.essentials.util.MutedPlayerEntry;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

import java.util.Date;

public class TempMuteCommand extends EssentialCommand {
    public TempMuteCommand() {
        super("tempmute", CommandPermission.MUTE);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> user = this.getUserArgument("victim");

        RequiredArgumentBuilder<ServerCommandSource, String> time = argument("time", StringArgumentType.word())
                .suggests(TimeDifferenceUtil::listSuggestions)
                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), null, false));

        RequiredArgumentBuilder<ServerCommandSource, String> reason = argument("reason", StringArgumentType.greedyString())
                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), StringArgumentType.getString(ctx, "reason"), false));

        LiteralArgumentBuilder<ServerCommandSource> silent = literal("-silent").then(
                this.getUserArgument("victim").then(
                        argument("time", StringArgumentType.word())
                                .suggests(TimeDifferenceUtil::listSuggestions)
                                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), null, true))
                                .then(
                                        argument("reason", StringArgumentType.greedyString())
                                                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), StringArgumentType.getString(ctx, "reason"), true))
                                )
                )
        );

        time.then(reason);
        user.then(time);
        this.argumentBuilder.then(silent);
        this.argumentBuilder.then(user);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, @NotNull String time, @Nullable String reason, boolean silent) throws CommandSyntaxException {
        CommandSourceUser src = this.getCommandSource(ctx);
        Date date = new Date();
        Date expiry = new Date(TimeDifferenceUtil.parse(time, true));

        super.resolveAndGetProfileAsync(ctx, "victim").thenAcceptAsync((victim) -> {
            MutedPlayerEntry entry = new MutedPlayerEntry(victim, date, src.getName(), expiry, reason);
            super.getServer().getUserManager().getMutedPlayerList().add(entry);
            KiloServer.getServer().triggerEvent(new PlayerMutedEventImpl(EntityIdentifiable.fromGameProfile(victim), src, reason));
            this.getServer().getUserManager().onPunishmentPerformed(src, new Punishment(src, EntityIdentifiable.fromGameProfile(victim), reason), Punishment.Type.MUTE, time, silent);
        });

        return AWAIT;
    }
}
