package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.api.command.SuggestArgument;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static io.github.indicode.fabric.permissions.Thimble.hasPermissionOrOp;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayers;
import static net.minecraft.command.arguments.EntityArgumentType.players;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.*;

public class GamemodeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> gamemodeCommand = literal("ke_gamemode")
                .requires(src -> hasPermissionOrOp(src, getCommandPermission("gamemode"), 2));

        LiteralArgumentBuilder<ServerCommandSource> gmCommand = literal("gm")
                .requires(src -> hasPermissionOrOp(src, getCommandPermission("gamemode"), 2));

        getCommandPermission("gamemode");
        getCommandPermission("gamemode.self");
        getCommandPermission("gamemode.others");
        for (GameMode gameMode: GameMode.values()) {
            if (gameMode.getName().isEmpty()) continue;
            getCommandPermission("gamemode.self." + gameMode.getName());
            getCommandPermission("gamemode.others." + gameMode.getName());
        }


        build(gamemodeCommand);
        build(gmCommand);
        dispatcher.register(gamemodeCommand);
        dispatcher.register(gmCommand);
    }

    private static void build(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder) {
        RequiredArgumentBuilder<ServerCommandSource, String> gameTypeArgument = argument("gameType", string())
                .suggests(GamemodeCommand::suggestGameModes)
                .executes(ctx -> execute(ctx, Collections.singletonList(ctx.getSource().getPlayer()), null,false));

        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetArgument = argument("target", players())
                .suggests(SuggestArgument::allPlayers)
                .executes(ctx -> execute(ctx, getPlayers(ctx, "target"), null,false))
                .then(literal("-silent")
                        .executes(ctx -> execute(ctx, getPlayers(ctx, "target"), null, true))
                );


        gameTypeArgument.then(targetArgument);
        argumentBuilder.then(gameTypeArgument);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, @Nullable GameMode cValue, boolean silent) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        String arg = cValue == null ? getString(ctx, "gameType") : cValue.getName();
        GameMode selectedMode = getMode(arg);

        if (selectedMode == null)
            throw new SimpleCommandExceptionType(new LiteralText("Please select a valid Game type!")).create();

        if (players.size() == 1 && !hasPermissionOrOp(src, getCommandPermission(getPermission("self", selectedMode)), 2))
            throw new SimpleCommandExceptionType(getPermissionError(getPermission("self", selectedMode))).create();

        if (players.size() > 1 && !hasPermissionOrOp(src, getCommandPermission(getPermission("others", selectedMode)), 3))
            throw new SimpleCommandExceptionType(getPermissionError(getPermission("others", selectedMode))).create();

        for (ServerPlayerEntity player : players) {
            if (!silent && !CommandHelper.areTheSame(src, player))
                KiloChat.sendLangMessageTo(player, "template.#1.announce", src.getName(), "gamemode",selectedMode.getName());
            player.setGameMode(selectedMode);

        }

        KiloChat.sendLangMessageTo(src, "template.#1", "gamemode", selectedMode.getName(),
                (players.size() > 1) ? players.size() + " players" : src.getName()
        );

        return SUCCESS();
    }

    private static GameMode getMode(String arg) {
        if  (arg.startsWith("sp") || arg.startsWith("3"))
            return GameMode.SPECTATOR;
        if  (arg.startsWith("s") || arg.startsWith("0"))
            return GameMode.SURVIVAL;
        if  (arg.startsWith("c") || arg.startsWith("1"))
            return GameMode.CREATIVE;
        if  (arg.startsWith("a") || arg.startsWith("2"))
            return GameMode.ADVENTURE;
        else
            return null;
    }

    private static String getPermission(String type, GameMode gameMode) {
        return "gamemode." + type + "." + (gameMode != null ? gameMode.getName() : null);
    }

    private static CompletableFuture<Suggestions> suggestGameModes(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(new String[]{"survival", "creative", "adventure", "spectator"}, builder);
    }

}
