package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
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
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayers;
import static net.minecraft.command.arguments.EntityArgumentType.players;
import static org.kilocraft.essentials.KiloCommands.*;

public class GamemodeCommand extends EssentialCommand {
    public GamemodeCommand() {
        super("ke_gamemode", (src) ->
                KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_SELF_ADVENTURE) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_SELF_SURVIVAL) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_SELF_SPECTATOR) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_SELF_CREATIVE) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_OTHERS_ADVENTURE) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_OTHERS_SURVIVAL) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_OTHERS_SPECTATOR) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_OTHERS_CREATIVE),
                new String[]{"gm"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> gameTypeArgument = argument("gameType", string())
                .suggests(GamemodeCommand::suggestGameModes).executes(ctx -> execute(ctx, Collections.singletonList(ctx.getSource().getPlayer()), null,false));

        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetArgument = argument("target", players())
                .suggests(TabCompletions::allPlayers)
                .executes(ctx -> execute(ctx, getPlayers(ctx, "target"), null,false))
                .then(literal("-silent")
                        .executes(ctx -> execute(ctx, getPlayers(ctx, "target"), null, true)));


        gameTypeArgument.then(targetArgument);
        commandNode.addChild(gameTypeArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> players, @Nullable GameMode cValue, boolean silent) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        String arg = cValue == null ? getString(ctx, "gameType") : cValue.getName();
        GameMode selectedMode = getMode(arg);

        if (selectedMode == null)
            throw new SimpleCommandExceptionType(new LiteralText("Please select a valid Game type!")).create();

        if (players.size() == 1 && !hasPermission(src, getPermission("self", selectedMode)))
            throw new SimpleCommandExceptionType(getPermissionError(getPermission("self", selectedMode).getNode())).create();

        if (players.size() > 1 && !hasPermission(src, getPermission("others", selectedMode)))
            throw new SimpleCommandExceptionType(getPermissionError(getPermission("others", selectedMode).getNode())).create();

        String singletonName = null;
        for (ServerPlayerEntity player : players) {
            if (!silent && !CommandHelper.areTheSame(src, player))
                KiloChat.sendLangMessageTo(player, "template.#1.announce", src.getName(), "gamemode", selectedMode.getName());
            player.setGameMode(selectedMode);
            if (players.size() == 1)
                singletonName = player.getEntityName();
        }

        if (singletonName == null)
            singletonName = src.getName();

        KiloChat.sendLangMessageTo(src, "template.#1", "gamemode",
                selectedMode.getName(), (players.size() == 1) ? singletonName : players.size() + " players");

        return SUCCESS();
    }

    private static GameMode getMode(String arg) {
        if  (arg.startsWith("sp") || arg.equals("3"))
            return GameMode.SPECTATOR;
        if  (arg.startsWith("s") || arg.equals("0"))
            return GameMode.SURVIVAL;
        if  (arg.startsWith("c") || arg.equals("1"))
            return GameMode.CREATIVE;
        if  (arg.startsWith("a") || arg.equals("2"))
            return GameMode.ADVENTURE;

        return null;
    }

    private static CommandPermission getPermission(String type, GameMode mode) {
        return CommandPermission.byName("gamemode." + type + "." + mode.getName().toLowerCase());
    }

    private static CompletableFuture<Suggestions> suggestGameModes(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        for (GameMode value : GameMode.values()) {
            if (value.equals(GameMode.NOT_SET))
                continue;

            strings.add(value.getName());
        }

        return CommandSource.suggestMatching(strings, builder);
    }

}
