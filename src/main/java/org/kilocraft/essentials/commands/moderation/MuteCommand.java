package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.TabCompletions;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class MuteCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> rootCommand = literal("mute")
                .requires(src -> hasPermission(src, "mute", 3))
                .executes(KiloCommands::executeSmartUsage);

        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArg = argument("target", EntityArgumentType.player())
                .suggests(TabCompletions::allPlayers)
                .executes(ctx -> execute(ctx.getSource(), null));

        RequiredArgumentBuilder<ServerCommandSource, String> reasonArg = argument("reason", greedyString())
                .executes(ctx -> execute(ctx.getSource(), getString(ctx, "reason")));

        selectorArg.then(reasonArg);
        rootCommand.then(selectorArg);
        dispatcher.register(rootCommand);
    }

    private static int execute(ServerCommandSource source, String reason) {

        return 1;
    }

}
