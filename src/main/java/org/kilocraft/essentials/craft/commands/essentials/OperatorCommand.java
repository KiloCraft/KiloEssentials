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

        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("operator")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.server.manage.operators", 2));
        LiteralArgumentBuilder<ServerCommandSource> setLiteral = CommandManager.literal("set");
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
                c -> executeList(c.getSource())
        );

        getLiteral.executes(
                c -> executeGet(c.getSource(), Collections.singleton(c.getSource().getPlayer().getGameProfile()))
        );

        getLiteral.then(
                CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile()).executes(
                        c -> executeGet(c.getSource(), GameProfileArgumentType.getProfileArgument(c, "gameProfile"))
                )
        );

        selectorArg.suggests(PlayerSelectorArgument.getSuggestions());
        boolArg.then(levelArg);
        selectorArg.then(boolArg);
        setLiteral.then(selectorArg);

        builder.then(getLiteral);
        builder.then(setLiteral);
        builder.then(listLiteral);

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
            if (playerManager.getOpList().isOp(gameProfile)){
                int opLevel = playerManager.getOpList().get(gameProfile).getPermissionLevel();
                TextColor.sendToUniversalSource(source, String.format(text, gameProfile.getName(), opLevel), false);
            } else {
                source.sendError(new LiteralText(gameProfile.getName() + " is not a operator!"));
            }
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
            if (CommandHelper.isConsole(source)) leastPermLevelReq = 4;

            if (level < leastPermLevelReq) {
                if (set) {
                    playerManager.getOpList().add(
                            new OperatorEntry(gameProfile, level, playerManager.getOpList().isOp(gameProfile))
                    );

                    p.addChatMessage(LangText.getFormatter(true, "command.operator.announce", source.getName(), level), false);

                    LangText.sendToUniversalSource(source, "command.operator.success", true, gameProfile.getName(), level);

                } else {
                    if (playerManager.isOperator(gameProfile)) {
                        playerManager.getOpList().remove(gameProfile);

                        p.addChatMessage(LangText.get(true, "command.operator.announce.removed"), false);

                        LangText.sendToUniversalSource(source, "command.operator.removed", true, gameProfile.getName());

                    }
                }
            } else {
                source.sendFeedback(KiloCommands.getPermissionError("Operator permission required, Level " + leastPermLevelReq), false);
            }

            playerManager.sendCommandTree(Objects.requireNonNull(playerManager.getPlayer(gameProfile.getId())));
        }

        return level;
    }
}
