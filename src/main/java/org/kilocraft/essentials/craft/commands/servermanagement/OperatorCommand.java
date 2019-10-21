package org.kilocraft.essentials.craft.commands.servermanagement;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.OperatorList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.util.CommandHelper;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;

import java.util.*;

public class OperatorCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("server.manage");
        String pNode = KiloCommands.getCommandPermission("server.manage.operators");
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("operator")
                .requires(s -> Thimble.hasPermissionOrOp(s, pNode, 2));
        LiteralArgumentBuilder<ServerCommandSource> aliasBuilder = CommandManager.literal("ke_op")
                .requires(s -> Thimble.hasPermissionOrOp(s, pNode, 2));

        LiteralArgumentBuilder<ServerCommandSource> addLiteral = CommandManager.literal("add");
        LiteralArgumentBuilder<ServerCommandSource> removeLiteral = CommandManager.literal("remove");
        LiteralArgumentBuilder<ServerCommandSource> listLiteral = CommandManager.literal("list");
        LiteralArgumentBuilder<ServerCommandSource> getLiteral = CommandManager.literal("get");

        RequiredArgumentBuilder<ServerCommandSource, Boolean> boolArg = CommandManager.argument("set", BoolArgumentType.bool());
        RequiredArgumentBuilder<ServerCommandSource, Integer> levelArg = CommandManager.argument("level", IntegerArgumentType.integer(1, 4));
        RequiredArgumentBuilder<ServerCommandSource, Boolean> byPassArg = CommandManager.argument("canBypassPlayerLimit", BoolArgumentType.bool());

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

        getLiteral.then(
                CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                        .suggests((context, builder1) -> {
                            return CommandSuggestions.operators.getSuggestions(context, builder1);
                        })
                        .executes(
                            c -> executeGet(
                                    c.getSource(),
                                    GameProfileArgumentType.getProfileArgument(c, "gameProfile")
                            )
                        )
        );

        byPassArg.executes(
                c -> execute(
                        c.getSource(),
                        GameProfileArgumentType.getProfileArgument(c, "gameProfile"),
                        true,
                        IntegerArgumentType.getInteger(c, "level"),
                        BoolArgumentType.getBool(c, "canBypassPlayerLimit")
                )
        );

        addLiteral.then(
                CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                        .suggests((context, builder1) -> {
                            return CommandSuggestions.nonOperators.getSuggestions(context, builder1);
                        })
                        .then(
                                CommandManager.argument("level", IntegerArgumentType.integer(0, 4))
                                        .then(
                                                CommandManager.argument("canByPassPlayerLimit", BoolArgumentType.bool())
                                                        .executes(
                                                                c -> execute(
                                                                        c.getSource(),
                                                                        GameProfileArgumentType.getProfileArgument(c, "gameProfile"),
                                                                        true,
                                                                        IntegerArgumentType.getInteger(c, "level"),
                                                                        BoolArgumentType.getBool(c, "canByPassPlayerLimit")
                                                                )
                                                        )
                                        )
                        )
        );

        removeLiteral.then(
                CommandManager.argument("gameProfile", GameProfileArgumentType.gameProfile())
                        .suggests(((context, builder1) -> {
                            return CommandSuggestions.operators.getSuggestions(context, builder1);
                        }))
                        .executes(
                            c -> execute(
                                    c.getSource(),
                                    GameProfileArgumentType.getProfileArgument(c, "gameProfile"),
                                    false,
                                    0,
                                    false
                            )
                        )
        );


        levelArg.then(byPassArg);
        boolArg.then(removeLiteral);
        boolArg.then(levelArg);

        builder.then(getLiteral);
        builder.then(listLiteral);
        builder.then(addLiteral);
        builder.then(removeLiteral);

        aliasBuilder.then(getLiteral);
        aliasBuilder.then(listLiteral);
        aliasBuilder.then(addLiteral);
        aliasBuilder.then(removeLiteral);

        dispatcher.register(aliasBuilder);
        dispatcher.register(builder);
    }

    private static int executeList(ServerCommandSource source) {
        String s = Arrays.toString(source.getMinecraftServer().getPlayerManager().getOpList().getNames());
        LiteralText literalText = (LiteralText) new LiteralText(
                "&eOperators&8:&r " + s.replace("[", "").replace("]", "").replaceAll(",", "&7,&r")
        ).setStyle(new Style().setColor(Formatting.GRAY));

        TextFormat.sendToUniversalSource(source, literalText, false);
        return 1;
    }

    private static int executeGet(ServerCommandSource source, Collection<GameProfile> gameProfiles) {
        PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();
        String text = "&eOperator &b%s&e:\n &7-&e Permission level&8: &a%s&e\n &7-&e Can bypass the player limit&8: &6%s&r";

        gameProfiles.forEach((gameProfile) -> {
            OperatorList operatorList = playerManager.getOpList();
            if (playerManager.isOperator(gameProfile)) {
                TextFormat.sendToUniversalSource(source,
                        String.format(
                                text,
                                gameProfile.getName(),
                                Objects.requireNonNull(operatorList.get(gameProfile)).getPermissionLevel(),
                                Objects.requireNonNull(operatorList.get(gameProfile)).canBypassPlayerLimit()
                        ), false);
            }
            else if (!playerManager.isOperator(gameProfile))
                source.sendError(new LiteralText(gameProfile.getName() + " is not a operator!"));
            else
                source.sendError(new LiteralText("Can not the get the info about that operator!"));
        });

        return 1;
    }

    private static int execute(ServerCommandSource source, Collection<GameProfile> gameProfiles, boolean set, int level, boolean byPass) throws CommandSyntaxException {
        PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();
        Iterator v = gameProfiles.iterator();
        OperatorList operatorList = playerManager.getOpList();

        while(v.hasNext()) {
            GameProfile gameProfile = (GameProfile) v.next();
            ServerPlayerEntity p = playerManager.getPlayer(gameProfile.getId());
            int leastPermLevelReq = 0;
            if (CommandHelper.isConsole(source))
                leastPermLevelReq = 5;
            else if (!CommandHelper.isConsole(source)) leastPermLevelReq = operatorList.get(source.getPlayer().getGameProfile()).getPermissionLevel();

            if (set) {
                if (level > leastPermLevelReq) source.sendError(KiloCommands.getPermissionError("Operator permission level " + (leastPermLevelReq + 1)));
                else if (!source.getName().equals(gameProfile.getName())){
                    if (!playerManager.isOperator(gameProfile)) {
                        LangText.sendToUniversalSource(source, "command.operator.success", true, gameProfile.getName(), level);
                        if (CommandHelper.isOnline(p)) p.addChatMessage(LangText.getFormatter(true, "command.operator.announce", source.getName(), level), false);
                        addOperator(gameProfile, level, byPass);
                    }
                    else if (playerManager.isOperator(gameProfile) && operatorList.get(gameProfile).getPermissionLevel() < level) {
                        removeOperator(gameProfile);
                        addOperator(gameProfile, level, byPass);
                        LangText.sendToUniversalSource(source, "command.operator.success", true, gameProfile.getName(), level);
                    }
                    else if (playerManager.isOperator(gameProfile)) {
                        source.sendError(new LiteralText(gameProfile.getName() + " is already a operator!"));
                    }

                }
                else if (source.getName().equals(gameProfile.getName())) {
                    source.sendError(LangText.get(false, "command.operator.exception"));
                }
            }
            else if (!set && playerManager.isOperator(gameProfile) && !source.getName().equals(gameProfile.getName())){
                removeOperator(gameProfile);
                if (CommandHelper.isOnline(p)) p.addChatMessage(LangText.get(true, "command.operator.announce.removed"), false);
                LangText.sendToUniversalSource(source, "command.operator.removed", false, gameProfile.getName());
            }
            else if (!set && !playerManager.isOperator(gameProfile) && !source.getName().equals(gameProfile.getName())) {
                source.sendError(new LiteralText(gameProfile.getName() + " is not a operator!"));
            }
            else if (source.getName().equals(gameProfile.getName()))
                source.sendError(LangText.get(false, "command.operator.exception"));

            if (CommandHelper.isOnline(p)) playerManager.sendCommandTree(Objects.requireNonNull(playerManager.getPlayer(gameProfile.getId())));
        }

        return level;
    }

    private static void addOperator(GameProfile gameProfile, int level, boolean byPass) {
        KiloServer.getServer().getOperatorList().add(new OperatorEntry(gameProfile, level, byPass));
    }

    private static void removeOperator(GameProfile gameProfile) {
        KiloServer.getServer().getOperatorList().remove(gameProfile);
    }
    

}
