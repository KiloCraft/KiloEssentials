package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextColor;
import org.kilocraft.essentials.api.command.PlayerSelectorArgument;
import org.kilocraft.essentials.api.util.CommandHelper;
import org.kilocraft.essentials.craft.KiloCommands;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

public class OperatorCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        String pNode = "kiloessentials.server.manage.operators";
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("operator")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, pNode, 2));
        LiteralArgumentBuilder<ServerCommandSource> aliasBuilder = CommandManager.literal("ke_op")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, pNode, 2));

        LiteralArgumentBuilder<ServerCommandSource> setLiteral = CommandManager.literal("set");
        LiteralArgumentBuilder<ServerCommandSource> addLiteral = CommandManager.literal("add");
        LiteralArgumentBuilder<ServerCommandSource> removeLiteral = CommandManager.literal("remove");

        LiteralArgumentBuilder<ServerCommandSource> listLiteral = CommandManager.literal("list");
        LiteralArgumentBuilder<ServerCommandSource> getLiteral = CommandManager.literal("get");

        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> selectorArg = CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile());
        RequiredArgumentBuilder<ServerCommandSource, Boolean> boolArg = CommandManager.argument("set", BoolArgumentType.bool());
        RequiredArgumentBuilder<ServerCommandSource, Integer> levelArg = CommandManager.argument("level", IntegerArgumentType.integer(1, 4));

        boolArg.executes(
                c -> execute(
                        c.getSource(),
                        GameProfileArgumentType.getProfileArgument(c, "gameProfile"),
                        BoolArgumentType.getBool(c, "set"),
                        KiloServer.getServer().getVanillaServer().getOpPermissionLevel())
        );

        levelArg.executes(
                c -> execute(
                        c.getSource(),
                        GameProfileArgumentType.getProfileArgument(c, "gameProfile"),
                        BoolArgumentType.getBool(c, "set"),
                        IntegerArgumentType.getInteger(c, "level"))
        );

        listLiteral.executes(
                c -> executeList(
                        c.getSource()
                )
        );

        getLiteral.executes(
                c -> executeGet(
                        c.getSource(),
                        Collections.singleton(c.getSource().getPlayer().getGameProfile())
                )
        );

        addLiteral.then(
                CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                        .suggests(CommandHelper.getAllPlayers())
                        .then(
                                CommandManager.argument("level", IntegerArgumentType.integer(0, 4))
                                        .executes(
                                                c -> execute(
                                                        c.getSource(),
                                                        GameProfileArgumentType.getProfileArgument(c, "gameProfile"),
                                                        true,
                                                        IntegerArgumentType.getInteger(c, "level")
                                                )
                                        )
                        )
        );

        removeLiteral.then(
                CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                        .suggests(CommandHelper.getAllPlayers())
                        .then(
                                CommandManager.argument("level", IntegerArgumentType.integer(0, 4))
                                        .executes(
                                                c -> execute(
                                                        c.getSource(),
                                                        GameProfileArgumentType.getProfileArgument(c, "gameProfile"),
                                                        false,
                                                        IntegerArgumentType.getInteger(c, "level")
                                                )
                                        )
                        )
        );

        addLiteral.then(
                CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                        .suggests(CommandHelper.getAllPlayers())
                        .executes(c -> execute(c.getSource(), GameProfileArgumentType.getProfileArgument(c, "gameProfile"), false, 4))
        );

        selectorArg.suggests(PlayerSelectorArgument.getSuggestions());
        boolArg.then(levelArg);
        selectorArg.then(boolArg);
        setLiteral.then(selectorArg);

        builder.then(getLiteral);
        builder.then(setLiteral);
        builder.then(listLiteral);
        builder.then(addLiteral);
        boolArg.then(removeLiteral);

        aliasBuilder.then(getLiteral);
        aliasBuilder.then(setLiteral);
        aliasBuilder.then(listLiteral);
        aliasBuilder.then(addLiteral);
        aliasBuilder.then(removeLiteral);

        dispatcher.register(aliasBuilder);
        dispatcher.register(builder);
    }

    private static int executeList(ServerCommandSource source) {
        LiteralText literalText = new LiteralText("&6Operators: &fServer");

        for (String name : source.getMinecraftServer().getPlayerManager().getOpList().getNames()) {
            literalText.append("&7, &f" + name);
        }

        TextColor.sendToUniversalSource(source, literalText, false);
        return 1;
    }

    private static int executeGet(ServerCommandSource source, Collection<GameProfile> gameProfiles) {
        PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();
        String text = "&eOperator &b%s&e, Permission level: &a%s&r";

        gameProfiles.forEach((gameProfile) -> {
            if (!playerManager.getOpList().isOp(gameProfile))
                source.sendError(new LiteralText(gameProfile.getName() + " is not a operator!"));
            else
                TextColor.sendToUniversalSource(
                        source,
                        String.format(
                                text,
                                gameProfile.getName(),
                                playerManager.getOpList().get(gameProfile).getPermissionLevel()
                        ),
                        false);
        });

        return 1;
    }

    private static int execute(ServerCommandSource source, Collection<GameProfile> gameProfiles, boolean set, int level) {
        PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();
        int i = 0;
        Iterator v = gameProfiles.iterator();

        while(v.hasNext()) {
            GameProfile gameProfile = (GameProfile) v.next();
            ServerPlayerEntity p = playerManager.getPlayer(gameProfile.getId());
            int leastPermLevelReq = playerManager.getOpList().get(gameProfile).getPermissionLevel();
            if (CommandHelper.isConsole(source)) leastPermLevelReq = 5;

            if (level < leastPermLevelReq && !source.getName().equals(Objects.requireNonNull(p).getName().asString())) {
                if (set) {
                    p.addChatMessage(LangText.getFormatter(true, "command.operator.announce", source.getName(), level), false);
                    LangText.sendToUniversalSource(source, "command.operator.success", true, gameProfile.getName(), level);

                    playerManager.getOpList().add(
                            new OperatorEntry(gameProfile, level, playerManager.getOpList().isOp(gameProfile))
                    );

                } else {
                    if (playerManager.isOperator(gameProfile)) {
                        playerManager.getOpList().remove(gameProfile);

                        p.addChatMessage(LangText.get(true, "command.operator.announce.removed"), false);
                        LangText.sendToUniversalSource(source, "command.operator.removed", true, gameProfile.getName());
                    }
                }
            } else if (source.getName().equals(Objects.requireNonNull(p).getName().asString())) {
                source.sendError(LangText.get(false, "command.operator.exception"));
            } else {
                source.sendFeedback(KiloCommands.getPermissionError("Operator permission required, Level " + leastPermLevelReq), false);
            }

            playerManager.sendCommandTree(Objects.requireNonNull(playerManager.getPlayer(gameProfile.getId())));
        }

        return level;
    }
}
