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

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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
                KiloCommands.getDispatcher().getRoot().getChildren().stream().filter((child) -> Commands.canSourceUse(child, context.getSource())).map(CommandNode::getName),
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
        return suggestAtCursor(Arrays.stream(TextFormat.getList()).filter((it) -> context.getInput().charAt(getPendingCursor(context)) == '&'), context);
    }


    public static CompletableFuture<Suggestions> allNonOperators(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((p) -> !playerManager.isOperator(p.getGameProfile())).map((p) -> p.getGameProfile().getName()), builder);
    }

    public static CompletableFuture<Suggestions> allOperators(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(playerManager.getOpNames(), builder);
    }

    public static SuggestionProvider<ServerCommandSource> getDateArguments = ((context, builder) ->
            CommandSource.suggestMatching(new String[]{"year", "month", "day", "minute", "second"}, builder)
    );

    public static CompletableFuture<Suggestions> suggestAtCursor(Stream<String> stream, CommandContext<ServerCommandSource> context) {
        return suggestAt(context.getInput().length(), stream, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(String[] strings, CommandContext<ServerCommandSource> context) {
        return suggestAt(context.getInput().length(), strings, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(Iterable<String> iterable, CommandContext<ServerCommandSource> context) {
        return suggestAt(context.getInput().length(), iterable, context);
    }

    public static CompletableFuture<Suggestions> suggestAt(int position, Stream<String> stream, CommandContext<ServerCommandSource> context) {
        return CommandSource.suggestMatching(stream, new SuggestionsBuilder(context.getInput(), position));
    }

    public static CompletableFuture<Suggestions> suggestAt(int position, String[] strings, CommandContext<ServerCommandSource> context) {
        return CommandSource.suggestMatching(strings, new SuggestionsBuilder(context.getInput(), position));
    }

    public static CompletableFuture<Suggestions> suggestAt(int position, Iterable<String> iterable, CommandContext<ServerCommandSource> context) {
        return CommandSource.suggestMatching(iterable, new SuggestionsBuilder(context.getInput(), position));
    }

    private static int getPendingCursor(CommandContext<ServerCommandSource> context) {
        return (context.getInput().length() - 1);
    }

    private static int getCursor(CommandContext<ServerCommandSource> context) {
        return context.getInput().length();
    }

}
