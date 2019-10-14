package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.GameMode;
import org.kilocraft.essentials.api.chat.LangText;

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

    private static void build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        String pNode = "kiloessentials.command.gamemode";
        builder.requires(s -> Thimble.hasPermissionChildOrOp(s, pNode, 2));
        for (int i = 0; i < var; ++i) {
            GameMode mode = gameModes[i];
            if (!mode.equals(GameMode.NOT_SET)) {
                builder.then(CommandManager.literal(mode.getName())
                        .then(
                                CommandManager.argument("target(s)", EntityArgumentType.players())
                                        .then(
                                                CommandManager.literal("-silent").executes(context -> {
                                                    return execute(EntityArgumentType.getPlayers(context, "target(s)"), mode, context.getSource(), false);
                                                })
                                        )
                                        .requires(source -> Thimble.hasPermissionChildOrOp(source, pNode + ".others." + mode.getName(), 2))
                                        .executes(context -> execute(EntityArgumentType.getPlayers(context, "target(s)"), mode, context.getSource(), true))
                        )
                        .requires(source -> Thimble.hasPermissionChildOrOp(source, pNode + ".self." + mode.getName(), 2))
                        .executes(context -> execute(Collections.singleton(context.getSource().getPlayer()), mode, context.getSource(), true))
                );

            }
        }

        builder.then(CommandManager.argument("gameType", IntegerArgumentType.integer(0, 3))
                .then(CommandManager.argument("target(s)", EntityArgumentType.players())
                        .then(
                                CommandManager.literal("-silent").executes(context -> {
                                    return executeByInteger(EntityArgumentType.getPlayers(context, "target(s)"), IntegerArgumentType.getInteger(context, "gameType"), context.getSource(), false);
                                })
                        )
                        .requires(source -> Thimble.hasPermissionChildOrOp(source, pNode + ".others", 2))
                        .executes(context -> executeByInteger(EntityArgumentType.getPlayers(context, "target(s)"), IntegerArgumentType.getInteger(context, "gameType"), context.getSource(), true))
                )
                .requires(source -> Thimble.hasPermissionChildOrOp(source, pNode + ".self", 2))
                .executes(context -> executeByInteger(Collections.singleton(context.getSource().getPlayer()), IntegerArgumentType.getInteger(context, "GameType"), context.getSource(), true))
        );

    }

    private static int execute(Collection<ServerPlayerEntity> playerEntities, GameMode gameMode, ServerCommandSource source, boolean log) {
        playerEntities.forEach((player) -> {
            player.setGameMode(gameMode);
            player.addChatMessage(new LiteralText("You have set the game type to: " + gameMode.getName()), false);
        });

        if (playerEntities.size() == 1) {
            playerEntities.forEach((player) -> {
                player.setGameMode(gameMode);
                if (player.getName().equals(source.getName())) {
                    player.addChatMessage(LangText.getFormatter(true, "command.gamemode.self.success", gameMode.getName()), false);
                } else {
                    player.addChatMessage(LangText.getFormatter(true, "command.gamemode.others.announce", gameMode.getName()), false);
                    LangText.sendToUniversalSource(source, "command.gamemode.others.success", false, player.getName(), gameMode.getName());
                }
            });
        } else {
            playerEntities.forEach((player) -> {
                player.setGameMode(gameMode);
                if (log) player.addChatMessage(LangText.getFormatter(true, "command.gamemode.others.announce", gameMode.getName()), false);
            });

            LangText.sendToUniversalSource(source, "command.gamemode.others.multiple", false, gameMode.getName(), playerEntities.size());
        }

        return 0;
    }

    private static int executeByInteger(Collection<ServerPlayerEntity> playerEntities, int int_1, ServerCommandSource source, boolean log) {
        GameMode gameMode = GameMode.byId(int_1);
        execute(playerEntities, gameMode, source, log);
        return 0;
    }

}
