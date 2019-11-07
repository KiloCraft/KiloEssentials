package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.kilocraft.essentials.api.util.CommandHelper;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.chat.KiloChat;

import java.util.Collection;
import java.util.Collections;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayers;
import static net.minecraft.command.arguments.EntityArgumentType.players;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GamemodeCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> fullLiteral = literal("ke_gamemode");
        LiteralArgumentBuilder<ServerCommandSource> shortLiteral = literal("gm");
        KiloCommands.getCommandPermission("gamemode");
        KiloCommands.getCommandPermission("gamemode.self");
        KiloCommands.getCommandPermission("gamemode.others");
        for (GameMode gameMode: GameMode.values()) {
            if (gameMode.getName().isEmpty()) continue;
            KiloCommands.getCommandPermission("gamemode.self." + gameMode.getName());
            KiloCommands.getCommandPermission("gamemode.others." + gameMode.getName());
        }
        build(fullLiteral);
        build(shortLiteral);

        dispatcher.register(fullLiteral);
        dispatcher.register(shortLiteral);
    }

    private static GameMode[] gameModes = GameMode.values();
    private static int var = gameModes.length;

    private static void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("gamemode"), 2));
        for (int i = 0; i < var; ++i) {
            GameMode mode = gameModes[i];
            if (!mode.equals(GameMode.NOT_SET)) {
                builder.then(literal(mode.getName())
                        .then(argument("targets", players())
                                        .suggests((context, builder1) -> {
                                            return CommandSuggestions.allPlayers.getSuggestions(context, builder1);
                                        })
                                        .then(
                                                literal("-silent").executes(context -> {
                                                    return execute(getPlayers(context, "targets"), mode, context.getSource(), false);
                                                })
                                        )
                                        .requires(source -> Thimble.hasPermissionOrOp(source, KiloCommands.getCommandPermission("gamemode.others." + mode.getName()), 2))
                                        .executes(context -> execute(getPlayers(context, "targets"), mode, context.getSource(), true))
                        )
                        .requires(source -> Thimble.hasPermissionOrOp(source, KiloCommands.getCommandPermission("gamemode.self." + mode.getName()), 2))
                        .executes(context -> execute(Collections.singletonList(context.getSource().getPlayer()), mode, context.getSource(), true))
                );

            }
        }

        builder.then(argument("gameType", integer(0, 3))
                .then(argument("targets", players())
                        .suggests((context, builder1) -> {
                            return CommandSuggestions.allPlayers.getSuggestions(context, builder1);
                        })
                        .then(
                                literal("-silent").executes(context -> {
                                    return executeByInteger(getPlayers(context, "targets"), getInteger(context, "gameType"), context.getSource(), false);
                                })
                        )
                        .requires(source -> Thimble.hasPermissionOrOp(source, KiloCommands.getCommandPermission("gamemode.others"), 2))
                        .executes(context -> executeByInteger(Collections.singletonList(context.getSource().getPlayer()), getInteger(context, "gameType"), context.getSource(), true))
                )
                .requires(source -> Thimble.hasPermissionOrOp(source, KiloCommands.getCommandPermission("gamemode.self"), 2))
                .executes(context -> executeByInteger(Collections.singletonList(context.getSource().getPlayer()), getInteger(context, "gameType"), context.getSource(), true))
        );

    }

    private static int execute(Collection<ServerPlayerEntity> playerEntities, GameMode gameMode, ServerCommandSource source, boolean log) {
        if (playerEntities.size() == 1) {
            playerEntities.forEach((playerEntity) -> {
                playerEntity.setGameMode(gameMode);

                if (CommandHelper.areTheSame(source, playerEntity)) {
                    KiloChat.sendLangMessageTo(source, "template.#1", "gamemode", gameMode.getName(), source.getName());
                } else {
                    KiloChat.sendLangMessageTo(playerEntity, "template.#1.announce", source.getName(), "gamemode", gameMode.getName());
                    KiloChat.sendLangMessageTo(source, "template.#1", "gamemode", gameMode.getName(), playerEntity.getName().asString());
                }
            });
        } else {
            playerEntities.forEach((playerEntity) -> {
                playerEntity.setGameMode(gameMode);
                if (log)
                    KiloChat.sendLangMessageTo(playerEntity, "template.#1.announce", source.getName(), "gamemode", gameMode.getName());
            });

            KiloChat.sendLangMessageTo(source, "template.#1", "gamemode", gameMode.getName(), playerEntities.size() + " players");
        }

        return 1;
    }

    private static int executeByInteger(Collection<ServerPlayerEntity> playerEntities, int i, ServerCommandSource source, boolean log) {
        GameMode gameMode = GameMode.byId(i);

        if (playerEntities.size() == 1) {
            playerEntities.forEach((playerEntity) -> {
                if (CommandHelper.areTheSame(source, playerEntity)) {
                    if (Thimble.hasPermissionOrOp(source, KiloCommands.getCommandPermission("gamemode.self." + gameMode.getName()), 2)) {
                        execute(playerEntities, gameMode, source, log);
                    } else
                        source.sendFeedback(KiloCommands.getPermissionError(KiloCommands.getCommandPermission("gamemode.self." + gameMode.getName())), false);
                } else {
                    if (Thimble.hasPermissionOrOp(source, KiloCommands.getCommandPermission("gamemode.others." + gameMode.getName()), 2)) {
                        execute(playerEntities, gameMode, source, log);
                    } else
                        source.sendFeedback(KiloCommands.getPermissionError(KiloCommands.getCommandPermission("gamemode.others." + gameMode.getName())), false);
                }
            });

        } else {
            if (Thimble.hasPermissionOrOp(source, KiloCommands.getCommandPermission("gamemode.others.multiple" + gameMode.getName()), 2)) {
                execute(playerEntities, gameMode, source, log);
            } else
                source.sendFeedback(KiloCommands.getPermissionError(KiloCommands.getCommandPermission("gamemode.others.multiple" + gameMode.getName())), false);
        }

        return 1;
    }
}
