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
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandSuggestions {

    private static PlayerManager playerManager = KiloServer.getServer().getPlayerManager();

    public static CompletableFuture<Suggestions> allPlayers(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(playerManager.getPlayerNames(), builder);
    }

    public static CompletableFuture<Suggestions> allPlayersExceptSource(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((p) -> {
            try {
                return !p.equals(context.getSource().getPlayer());
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return false;
        }).map((p) -> p.getName().asString()), builder);
    }

    public static CompletableFuture<Suggestions> dimensions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(new String[]{"overworld", "the_nether", "the_end"}, builder);
    }

    public static CompletableFuture<Suggestions> usableCommands(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(
                KiloCommands.getDispatcher().getRoot().getChildren().stream().filter((child) -> canSourceUse(child, context.getSource())).map(CommandNode::getName),
                builder
        );
    }

    public static CompletableFuture<Suggestions> commands(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(
                KiloCommands.getDispatcher().getRoot().getChildren().stream().filter((child) -> !child.getName().startsWith(Commands.vanillaCommandsPrefix)).map(CommandNode::getName),
                builder
        );
    }

    public static CompletableFuture<Suggestions> textformatChars(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        int cursor = context.getInput().length();
        SuggestionsBuilder sug = new SuggestionsBuilder(context.getInput(), cursor);
        List<String> str = new ArrayList<>(Arrays.asList(TextFormat.getList()));

        return CommandSource.suggestMatching(str.stream().filter((it) -> context.getInput().charAt((cursor - 1)) == '&'), sug);
    }


    public static CompletableFuture<Suggestions> allNonOperators(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((p) -> !playerManager.isOperator(p.getGameProfile())).map((p) -> p.getGameProfile().getName()), builder);
    }

    public static CompletableFuture<Suggestions> allOperators(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CommandSource.suggestMatching(playerManager.getOpNames(), builder);
    }

    public static SuggestionProvider<ServerCommandSource> getDateArguments = ((context, builder) ->
            CommandSource.suggestMatching(new String[]{"year", "month", "day", "minute", "second"}, builder)
    );

    public static <S> boolean canSourceUse(CommandNode<S> commandNode, S source) {
        if (KiloConfig.getProvider().getMain().getBooleanSafely("commands.suggestions.require_permission")) {
            if (commandNode.canUse(source)) {
                if (Commands.isCustomCommand(commandNode.getName()))
                    return true;
                else
                    return true;
            } else
                return false;
        }
        return !Commands.isVanillaCommand(commandNode.getName().replace(Commands.vanillaCommandsPrefix, ""))
                || Commands.isCustomCommand(Commands.customCommandsPrefix + commandNode.getName());
    }
}

