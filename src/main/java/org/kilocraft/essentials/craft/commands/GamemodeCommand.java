package org.kilocraft.essentials.craft.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;

import java.util.Collection;
import java.util.Collections;

public class GamemodeCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> fullLiteral = CommandManager.literal("ke_gamemode");
        LiteralArgumentBuilder<ServerCommandSource> shortLiteral = CommandManager.literal("gm");

        build(fullLiteral);
        build(shortLiteral);

        dispatcher.register(fullLiteral);
        dispatcher.register(shortLiteral);
    }

    private static GameMode[] gameModes = GameMode.values();
    private static int var = gameModes.length;
    private static String pNode = "kiloessentials.command.gamemode";

    private static void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        builder.requires(s -> Thimble.hasPermissionChildOrOp(s, pNode, 2));
        for (int i = 0; i < var; ++i) {
            GameMode mode = gameModes[i];
            if (!mode.equals(GameMode.NOT_SET)) {
                builder.then(CommandManager.literal(mode.getName())
                        .then(
                                CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                                        .suggests((context, builder1) -> {
                                            return CommandSuggestions.getAllPlayersWithSelfSelector.getSuggestions(context, builder1);
                                        })
                                        .then(
                                                CommandManager.literal("-silent").executes(context -> {
                                                    return execute(GameProfileArgumentType.getProfileArgument(context, "gameProfile"), mode, context.getSource(), false);
                                                })
                                        )
                                        .requires(source -> Thimble.hasPermissionChildOrOp(source, pNode + ".others." + mode.getName(), 2))
                                        .executes(context -> execute(GameProfileArgumentType.getProfileArgument(context, "gameProfile"), mode, context.getSource(), true))
                        )
                        .requires(source -> Thimble.hasPermissionChildOrOp(source, pNode + ".self." + mode.getName(), 2))
                        .executes(context -> execute(Collections.singleton(context.getSource().getPlayer().getGameProfile()), mode, context.getSource(), true))
                );

            }
        }

        builder.then(CommandManager.argument("gameType", IntegerArgumentType.integer(0, 3))
                .then(CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                        .suggests((context, builder1) -> {
                            return CommandSuggestions.getAllPlayersWithSelfSelector.getSuggestions(context, builder1);
                        })
                        .then(
                                CommandManager.literal("-silent").executes(context -> {
                                    return executeByInteger(GameProfileArgumentType.getProfileArgument(context, "gameProfile"), IntegerArgumentType.getInteger(context, "gameType"), context.getSource(), false);
                                })
                        )
                        .requires(source -> Thimble.hasPermissionChildOrOp(source, pNode + ".others", 2))
                        .executes(context -> executeByInteger(GameProfileArgumentType.getProfileArgument(context, "gameProfile"), IntegerArgumentType.getInteger(context, "gameType"), context.getSource(), true))
                )
                .requires(source -> Thimble.hasPermissionChildOrOp(source, pNode + ".self", 2))
                .executes(context -> executeByInteger(Collections.singleton(context.getSource().getPlayer().getGameProfile()), IntegerArgumentType.getInteger(context, "gameType"), context.getSource(), true))
        );

    }

    private static int execute(Collection<GameProfile> gameProfiles, GameMode gameMode, ServerCommandSource source, boolean log) {
        if (gameProfiles.size() == 1) {
            gameProfiles.forEach((gameProfile) -> {
                ServerPlayerEntity target = KiloServer.getServer().getPlayer(gameProfile.getId());
                target.setGameMode(gameMode);

                if (source.getName().equals(target.getName().asString())) {
                    LangText.sendToUniversalSource(source, "command.gamemode.self.update", false, gameMode.getName());
                } else {
                    target.addChatMessage(LangText.getFormatter(true, "command.gamemode.others.announce", gameMode.getName(), source.getName()), false);
                    LangText.sendToUniversalSource(source, "command.gamemode.others.success", false, gameMode.getName(), target.getName().asString(), "");
                }
            });
        } else {
            gameProfiles.forEach((gameProfile) -> {
                ServerPlayerEntity target = KiloServer.getServer().getPlayer(gameProfile.getId());
                target.setGameMode(gameMode);
                if (!log)
                    target.addChatMessage(LangText.getFormatter(true, "command.gamemode.others.announce", gameMode.getName(), source.getName()), false);
            });

            LangText.sendToUniversalSource(source, "command.gamemode.others.multiple", false, gameMode.getName(), gameProfiles.size());
        }

        return 0;
    }

    private static int executeByInteger(Collection<GameProfile> gameProfiles, int i, ServerCommandSource source, boolean log) {
        GameMode gameMode = GameMode.byId(i);

        if (gameProfiles.size() == 1) {
            gameProfiles.forEach((gameProfile) -> {
                ServerPlayerEntity player = KiloServer.getServer().getPlayer(gameProfile.getId());
                if (player.getName().equals(source.getName())) {
                    if (Thimble.hasPermissionChildOrOp(source, pNode + ".self." + gameMode.getName(), 2)) {
                        execute(gameProfiles, gameMode, source, log);
                    } else
                        source.sendFeedback(KiloCommands.getPermissionError(pNode + ".self." + gameMode.getName()), false);
                } else {
                    if (Thimble.hasPermissionChildOrOp(source, pNode + ".others." + gameMode.getName(), 2)) {
                        execute(gameProfiles, gameMode, source, log);
                    } else
                        source.sendFeedback(KiloCommands.getPermissionError(pNode + ".self." + gameMode.getName()), false);
                }
            });

        } else {
            if (Thimble.hasPermissionChildOrOp(source, pNode + ".others.multiple" + gameMode.getName(), 2)) {
                execute(gameProfiles, gameMode, source, log);
            } else
                source.sendFeedback(KiloCommands.getPermissionError(pNode + ".self." + gameMode.getName()), false);
        }

        return 1;
    }
}
