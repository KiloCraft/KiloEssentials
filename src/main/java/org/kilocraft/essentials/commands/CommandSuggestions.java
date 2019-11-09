package org.kilocraft.essentials.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.commands.Commands;

import java.util.concurrent.CompletableFuture;

public class CommandSuggestions {

    private static PlayerManager playerManager = KiloServer.getServer().getPlayerManager();

    public static CompletableFuture<Suggestions> allPlayers(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CommandSource.suggestMatching(playerManager.getPlayerNames(), builder);
    }

    public static SuggestionProvider<ServerCommandSource> suggestInput = ((context, builder) -> {
        builder.suggest(context.getNodes().get(0).getNode().getName());
        return builder.buildFuture();
    });

    public static CompletableFuture<Suggestions> allNonOperators(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((p) -> !playerManager.isOperator(p.getGameProfile())).map((p) -> p.getGameProfile().getName()), builder);
    }

    public static CompletableFuture<Suggestions> allOperators(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CommandSource.suggestMatching(playerManager.getOpNames(), builder);
    }

    public static SuggestionProvider<ServerCommandSource> getDateArguments = ((context, builder) ->
            CommandSource.suggestMatching(new String[]{"year", "month", "day", "minute", "second"}, builder)
    );

    public static <S> boolean buildForSource(CommandNode<S> commandNode, S source) {
        if (commandNode.canUse(source)) {
            if (Commands.isVanillaCommand(commandNode.getName().replace(Commands.vanillaCommandsPrefix, ""))
                    && !Commands.isCustomCommand(Commands.customCommandsPrefix + commandNode.getName()))
                return false;
            if (Commands.isCustomCommand(commandNode.getName()))
                return true;
            else
                return true;
        } else
            return false;
    }
}
