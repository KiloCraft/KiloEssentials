package org.kilocraft.essentials.craft.commands.essentials.staffcommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;

public class BanCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("ke_ban")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("staff.ban"), 2))
                .executes(context -> KiloCommands.executeUsageFor("command.ban.usage", context.getSource()));

        LiteralArgumentBuilder<ServerCommandSource> addArg = CommandManager.literal("add")
                .then(
                        CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                            .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                        .then(
                                CommandManager.literal("permanent")
                                        .then(
                                                CommandManager.literal("username")
                                                        .then(
                                                                CommandManager.argument("reason", StringArgumentType.greedyString())
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("ip")
                                                        .then(
                                                                CommandManager.argument("reason", StringArgumentType.greedyString())
                                                        )
                                        )
                        )
                        .then(
                                CommandManager.literal("temporary").then(
                                        CommandManager.argument("time", IntegerArgumentType.integer(0)).then(
                                                CommandManager.argument("date", StringArgumentType.string())
                                                        .suggests((context, builder) -> CommandSuggestions.getDateArguments.getSuggestions(context, builder))
                                                        .then(
                                                                CommandManager.literal("username")
                                                                        .then(
                                                                                CommandManager.argument("reason", StringArgumentType.greedyString())
                                                                        )
                                                        )
                                                        .then(
                                                                CommandManager.literal("ip")
                                                                        .then(
                                                                                CommandManager.argument("reason", StringArgumentType.greedyString())
                                                                        )
                                                        )
                                        )
                                )
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> removeArg = CommandManager.literal("remove")
                .then(
                        CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                                .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                .then(
                                        CommandManager.literal("username")
                                                .then(
                                                        CommandManager.argument("reason", StringArgumentType.greedyString())
                                                )
                                )
                                .then(
                                        CommandManager.literal("ip")
                                                .then(
                                                        CommandManager.argument("reason", StringArgumentType.greedyString())
                                                )
                                )
                );

        LiteralArgumentBuilder<ServerCommandSource> listArg = CommandManager.literal("list");

        LiteralArgumentBuilder<ServerCommandSource> checkArg = CommandManager.literal("check")
                .then(
                        CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                                .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                );

        argumentBuilder.then(addArg);
        argumentBuilder.then(removeArg);
        argumentBuilder.then(listArg);
        argumentBuilder.then(checkArg);

        dispatcher.register(argumentBuilder);
    }
}
