package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.GameMode;
import org.kilocraft.essentials.api.command.PlayerSelectorArgument;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class GamemodeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> fullLiteral = CommandManager.literal("ke_gamemode");
        LiteralArgumentBuilder<ServerCommandSource> shortLiteral = CommandManager.literal("gm");

        buildFullLiteral(fullLiteral);
        buildFullLiteral(shortLiteral);
        buildShortCommand(dispatcher);

        dispatcher.register(fullLiteral);
        dispatcher.register(shortLiteral);
    }

    private static GameMode[] gameModes = GameMode.values();
    private static int var = gameModes.length;

    private static void buildFullLiteral(LiteralArgumentBuilder<ServerCommandSource> builder) {
        for (int i = 0; i < var; ++i) {
            GameMode mode = gameModes[i];
            if (!mode.equals(GameMode.NOT_SET)) {

                builder.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.gamemode", 2));
                builder.then(CommandManager.literal(mode.getName())
                        .then(
                                CommandManager.argument("target(s)", StringArgumentType.string())
                                        .suggests(PlayerSelectorArgument.getSuggestions())
                                        .requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.gamemode.others." + mode.getName(), 2))
                                        .executes(context -> execute(Collections.singletonList(PlayerSelectorArgument.getPlayer(context, "target(s)")), mode, context.getSource()))
                        )
                    .requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.gamemode." + mode.getName() + ".self", 2))
                    .executes(context -> execute(Collections.singleton(context.getSource().getPlayer()), mode, context.getSource()))
                );

            }
        }

        builder.then(CommandManager.argument("GameType", IntegerArgumentType.integer(0, 3))
                    .then(CommandManager.argument("target(s)", StringArgumentType.string())
                            .suggests(PlayerSelectorArgument.getSuggestions())
                            .requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.gamemode.others", 2))
                            .executes(context -> executeByInteger(Collections.singletonList(PlayerSelectorArgument.getPlayer(context, "target(s)")), IntegerArgumentType.getInteger(context, "GameType"), context.getSource()))
                    )
                .requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.gamemode", 2))
                .executes(context -> executeByInteger(Collections.singleton(context.getSource().getPlayer()), IntegerArgumentType.getInteger(context, "GameType"), context.getSource()))
        );

    }

    private static void buildShortCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        HashMap<String, GameMode> hashMap = new HashMap<String, GameMode>(){{
            put("gms", GameMode.SURVIVAL);
            put("gmc", GameMode.CREATIVE);
            put("gma", GameMode.ADVENTURE);
            put("gmsp", GameMode.SPECTATOR);
        }};

        hashMap.forEach((name, mode) -> {
            dispatcher.register(
                    CommandManager.literal(name)
                            .then(
                                    CommandManager.argument("target(s)", EntityArgumentType.players())
                                        .requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.gamemode.others." + mode.getName(), 2))
                                        .executes(context -> execute(EntityArgumentType.getPlayers(context, "target(s)"), mode, context.getSource()))
                            )
                        .requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.gamemode.self." + mode.getName(), 2))
                        .executes(context -> execute(Collections.singleton(context.getSource().getPlayer()), mode, context.getSource()))
            );

        });
    }

    private static int execute(Collection<ServerPlayerEntity> playerEntities, GameMode gameMode, ServerCommandSource source) {
        playerEntities.forEach((player) -> {
            player.setGameMode(gameMode);
            player.addChatMessage(new LiteralText("You have set the game type to: " + gameMode.getName()), false);
        });
        return 0;
    }

    private static int executeByInteger(Collection<ServerPlayerEntity> playerEntities, int int_1, ServerCommandSource source) {
        GameMode gameMode = GameMode.byId(int_1);
        execute(playerEntities, gameMode, source);
        return 0;
    }

}
