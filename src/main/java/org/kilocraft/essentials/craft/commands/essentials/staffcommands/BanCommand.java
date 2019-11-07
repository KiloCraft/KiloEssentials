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

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.arguments.GameProfileArgumentType.gameProfile;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BanCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("ke_ban")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("staff.ban"), 2))
                .executes(context -> KiloCommands.executeUsageFor("command.ban.usage", context.getSource()));

        LiteralArgumentBuilder<ServerCommandSource> addArg = literal("add")
                .then(
                        argument("gameProfile", gameProfile())
                            .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                        .then(
                                literal("permanent")
                                        .then(
                                                literal("username")
                                                        .then(
                                                                argument("reason", greedyString())
                                                        )
                                        )
                                        .then(
                                                literal("ip")
                                                        .then(
                                                                argument("reason", greedyString())
                                                        )
                                        )
                        )
                        .then(
                                literal("temporary").then(
                                        argument("time", integer(0)).then(
                                                argument("date", string())
                                                        .suggests((context, builder) -> CommandSuggestions.getDateArguments.getSuggestions(context, builder))
                                                        .then(
                                                                literal("username")
                                                                        .then(
                                                                                argument("reason", greedyString())
                                                                        )
                                                        )
                                                        .then(
                                                                literal("ip")
                                                                        .then(
                                                                                argument("reason", greedyString())
                                                                        )
                                                        )
                                        )
                                )
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> removeArg = literal("remove")
                .then(
                        argument("gameProfile", gameProfile())
                                .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                .then(
                                        literal("username")
                                                .then(
                                                        argument("reason", greedyString())
                                                )
                                )
                                .then(
                                        literal("ip")
                                                .then(
                                                        argument("reason", greedyString())
                                                )
                                )
                );

        LiteralArgumentBuilder<ServerCommandSource> listArg = literal("list");

        LiteralArgumentBuilder<ServerCommandSource> checkArg = literal("check")
                .then(
                        argument("gameProfile", gameProfile())
                                .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                );

        argumentBuilder.then(addArg);
        argumentBuilder.then(removeArg);
        argumentBuilder.then(listArg);
        argumentBuilder.then(checkArg);

        dispatcher.register(argumentBuilder);
    }
}
