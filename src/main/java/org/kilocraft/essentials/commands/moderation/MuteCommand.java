package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.PunishmentManager;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.util.MutedPlayerEntry;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

import java.util.Date;

public class MuteCommand extends EssentialCommand {
    public MuteCommand() {
        super("mute", CommandPermission.MUTE);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> user = this.getUserArgument("victim")
                .executes(ctx -> this.execute(ctx, null, false));
        RequiredArgumentBuilder<ServerCommandSource, String> reason = argument("reason", StringArgumentType.greedyString())
                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), false));

        LiteralArgumentBuilder<ServerCommandSource> silent = literal("-silent")
                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), true));

        user.then(reason);
        silent.then(user);
        this.argumentBuilder.then(silent);
        this.argumentBuilder.then(user);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, @Nullable String reason, boolean silent) throws CommandSyntaxException {
        CommandSourceUser src = this.getServerUser(ctx);
        Date date = new Date();

        super.resolveAndGetProfileAsync(ctx, "victim").thenAcceptAsync((victim) -> {
            MutedPlayerEntry entry = new MutedPlayerEntry(victim, date, src.getName(), null, reason);
            super.getServer().getUserManager().getMutedPlayerList().add(entry);
            this.getServer().getUserManager().onPunishmentPerformed(src, new Punishment(src, EntityIdentifiable.fromGameProfile(victim), reason), Punishment.Type.MUTE, null, silent);
        });

        return AWAIT;
    }
}
