package org.kilocraft.essentials.craft.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.util.CommandHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class GamemodeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> fullLiteral = CommandManager.literal("ke_gamemode");
        LiteralArgumentBuilder<ServerCommandSource> shortLiteral = CommandManager.literal("gm");

        buildCommand(fullLiteral);
        buildCommand(shortLiteral);

        dispatcher.register(fullLiteral);
        dispatcher.register(shortLiteral);
    }

    private static GameMode[] gameModes = GameMode.values();
    private static int var = gameModes.length;

    private static void buildCommand(LiteralArgumentBuilder<ServerCommandSource> builder) {
        String pNode = "kiloessentials.command.gamemode";
        builder.requires(s -> Thimble.hasPermissionChildOrOp(s, pNode,2));

        GameMode[] gameModes = GameMode.values();
        int var = gameModes.length;

        for (int i = 0; i < var; ++i) {
            GameMode mode = gameModes[i];
            if (!mode.equals(GameMode.NOT_SET)) {
                builder.then(
                        CommandManager.literal(mode.getName())
                                .then(
                                        CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                                                .requires(s -> Thimble.hasPermissionChildOrOp(s, pNode + ".others." + mode, 2))
                                                .suggests(CommandHelper.getAllPlayers())
                                                .executes(c -> execute(GameProfileArgumentType.getProfileArgument(c, "gameProfile"), mode, c.getSource()))
                                )
                                .requires(s -> Thimble.hasPermissionChildOrOp(s, pNode + ".self." + mode.getName(), 2))
                                .executes(c -> execute(Collections.singleton(c.getSource().getPlayer().getGameProfile()), mode, c.getSource()))
                );
            }

        }

    }

    private static int executeByName(Collection<GameProfile> gameProfiles, String string, ServerCommandSource source) {


        return 1;
    }

    private static int execute(Collection<GameProfile> gameProfiles, GameMode gameMode, ServerCommandSource source) {
        PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();
        String status;

        if (gameProfiles.size() == 1) {
            gameProfiles.forEach((gameProfile) -> {
                ServerPlayerEntity playerEntity = playerManager.getPlayer(gameProfile.getId());

                Objects.requireNonNull(playerEntity).setGameMode(gameMode);

                if (source.getName() != playerEntity.getName().asString()) {
                    playerEntity.addChatMessage(LangText.getFormatter(true, "command.gamemode.announce", gameMode.getName(), source.getName(), "test"), false);
                }

                LangText.sendToUniversalSource(source, "command.gamemode.success", false, gameMode.getName() , gameProfile.getName());
            });
        }

        return 1;
    }

    private static int executeByInteger(Collection<GameProfile> gameProfiles, int int_1, ServerCommandSource source) {
        GameMode gameMode = GameMode.byId(int_1);

        return 1;
    }

}
