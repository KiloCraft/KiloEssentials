package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.GameMode;
import org.kilocraft.essentials.api.command.PlayerSelectorArgument;

import java.util.Collection;
import java.util.Collections;

public class GamemodeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> fullLiteral = CommandManager.literal("gamemode");
        LiteralArgumentBuilder<ServerCommandSource> shortLiteral = CommandManager.literal("gm");
        LiteralArgumentBuilder<ServerCommandSource> shortCommand;

        buildFullLiteral(fullLiteral);
        buildFullLiteral(shortLiteral);
        buildShortCommand(dispatcher);

        dispatcher.register(fullLiteral);
        dispatcher.register(shortLiteral);
    }

    private static GameMode[] gameModes = GameMode.values();
    private static int var = gameModes.length;

    private static void buildFullLiteral(LiteralArgumentBuilder<ServerCommandSource> builder) {
        RequiredArgumentBuilder<ServerCommandSource, String> selectorArgument;
        for (int i = 0; i < var; i++) {
            GameMode mode = gameModes[i];
            if (!mode.equals(GameMode.NOT_SET)) {

                selectorArgument = CommandManager.argument("target", StringArgumentType.string())
                        .requires(s -> Thimble.hasPermissionChildOrOp(s, "es.command." + mode.getName() + ".others", 2))
                        .suggests(PlayerSelectorArgument.getSuggestions())
                        .executes(c -> execute(Collections.singleton(PlayerSelectorArgument.getPlayer(c, "target")), mode, c.getSource()));

                builder.then(CommandManager.literal(mode.getName())
                    .then(selectorArgument)
                    .requires(s -> Thimble.hasPermissionChildOrOp(s, "es.command." + mode.getName(), 2))
                ).executes(c -> execute(Collections.singleton(c.getSource().getPlayer()), mode, c.getSource()));


                builder.then(CommandManager.argument("GameType", IntegerArgumentType.integer(0, 3))
                        .then(selectorArgument)
                    .requires(s -> Thimble.hasPermissionChildOrOp(s, "es.command." + mode.getName(), 2))
                    .executes(c -> execute(Collections.singleton(c.getSource().getPlayer()), mode, c.getSource()))
                );

            }
        }

    }

    private static void buildShortCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        for (int i = 0; i < var; i++) {
            GameMode mode = gameModes[i];
            if (mode != GameMode.NOT_SET) {
                dispatcher.register(CommandManager.literal(String.valueOf(mode.getName().charAt(0)))
                        .then(CommandManager.argument("target", StringArgumentType.string())
                            .requires(s -> Thimble.hasPermissionChildOrOp(s, "es.command." + mode.getName() + ".others", 2))
                            .suggests(PlayerSelectorArgument.getSuggestions())
                            .executes(c -> execute(Collections.singleton(PlayerSelectorArgument.getPlayer(c, "target")), mode, c.getSource())))

                        .requires(s -> Thimble.hasPermissionChildOrOp(s, "es.command." + mode.getName(), 2))
                        .executes(c -> execute(Collections.singleton(c.getSource().getPlayer()), mode, c.getSource()))
                );
            }
        }
    }

    private static int execute(Collection<ServerPlayerEntity> playerEntities, GameMode gameMode, ServerCommandSource source) {
        if (playerEntities.size() > 1) {
            playerEntities.forEach((player) -> {
                player.setGameMode(gameMode);
            });

            source.sendFeedback(new LiteralText("You have set the gamemode to " + gameMode + "for " + playerEntities.size() + " players."), false);
        } else {
            playerEntities.forEach((player) -> {
                player.setGameMode(gameMode);
                player.addChatMessage(new LiteralText("Gamemode set to " + gameMode.getName()), false);
            });

        }

        return 0;
    }

}
