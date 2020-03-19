package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.user.PunishmentManager;

import java.util.Collection;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayers;
import static net.minecraft.command.arguments.EntityArgumentType.players;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.SUCCESS;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class KickCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> kickCommand = literal("ke_kick")
                .requires(src -> hasPermission(src, "kick", 3))
                .executes(KiloCommands::executeSmartUsage);

        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetArgument = argument("targets", players())
                .suggests(ArgumentCompletions::allPlayers)
                .executes(ctx -> execute(ctx.getSource(), getPlayers(ctx, "targets"), ""))
                .then(argument("reason", string())
                .executes(ctx -> execute(ctx.getSource(), getPlayers(ctx, "targets"), getString(ctx, "reason"))));

        kickCommand.then(targetArgument);
        dispatcher.register(kickCommand);
    }

    private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> players, String reason) {
        PunishmentManager punishmentManager = KiloServer.getServer().getUserManager().getPunishmentManager();

        for (ServerPlayerEntity player : players) {
            punishmentManager.kick(player, new LiteralText(TextFormat.translate(reason)));
        }

        return SUCCESS();
    }

}
