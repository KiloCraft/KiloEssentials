package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.commands.LiteralCommandModified;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ArgumentCompletions {

    private static final PlayerManager playerManager = KiloServer.getServer().getPlayerManager();

    public static CompletableFuture<Suggestions> noSuggestions(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return new CompletableFuture<>();
    }

    public static CompletableFuture<Suggestions> allPlayers(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ArgumentCompletions.playerManager.getPlayerList().stream()
                .map(PlayerEntity::getEntityName), builder);
    }

    public static CompletableFuture<Suggestions> allPlayersExceptSource(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ArgumentCompletions.playerManager.getPlayerList().stream().filter(p -> {
            try {
                return !p.equals(context.getSource().getPlayer());
            } catch (CommandSyntaxException ignored) {}
            return false;
        }).map(PlayerEntity::getEntityName), builder);
    }

    public static CompletableFuture<Suggestions> allPlayerNicks(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        final List<String> nicks = new ArrayList<>();
        for (final OnlineUser user : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
            nicks.add(TextFormat.removeAlternateColorCodes('&', user.getDisplayName()));
            nicks.add(user.getUsername());
        }

        return CommandSource.suggestMatching(nicks, builder);
    }

    public static CompletableFuture<Suggestions> dimensions(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        final List<String> dims = new ArrayList<>();
        Registry.DIMENSION_TYPE.forEach(dimType -> dims.add(Objects.requireNonNull(DimensionType.getId(dimType)).getPath()));
        return CommandSource.suggestMatching(dims.stream(), builder);
    }

    public static CompletableFuture<Suggestions> usableCommands(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return KiloCommands.toastSuggestions(context, builder);
    }

    public static CompletableFuture<Suggestions> commands(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(
                KiloCommands.getDispatcher().getRoot().getChildren().stream().filter(child -> !child.getName().startsWith(LiteralCommandModified.getNMSCommandPrefix())).map(CommandNode::getName),
                builder
        );
    }

    public static CompletableFuture<Suggestions> textformatChars(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return ArgumentCompletions.suggestAtCursor(Arrays.stream(TextFormat.getList()).filter(it -> context.getInput().charAt(ArgumentCompletions.getPendingCursor(context)) == '&'), context);
    }


    public static CompletableFuture<Suggestions> allNonOperators(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ArgumentCompletions.playerManager.getPlayerList().stream().filter(p -> !ArgumentCompletions.playerManager.isOperator(p.getGameProfile())).map(PlayerEntity::getEntityName), builder);
    }

    public static CompletableFuture<Suggestions> allOperators(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ArgumentCompletions.playerManager.getOpNames(), builder);
    }

    public static SuggestionProvider<ServerCommandSource> getDateArguments = (context, builder) ->
            CommandSource.suggestMatching(new String[]{"year", "month", "day", "minute", "second"}, builder);

    public static CompletableFuture<Suggestions> timeSuggestions(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        final String[] values = {"s", "m", "h", "d"};
        final String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        final Integer in = Integer.parseInt(builder.getInput());

        if (builder.getInput().equals(String.valueOf(in))) {
            return CommandSource.suggestMatching(values, builder);
        }

        return null;
    }

    public static CompletableFuture<Suggestions> stateSuggestions(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(new String[]{"on", "off", "toggle"}, builder);
    }

    public static CompletableFuture<Suggestions> suggestAtArg(final int arg, final String[] strings, final CommandContext<ServerCommandSource> context) {
        return ArgumentCompletions.suggestAt(ArgumentCompletions.getCursorAtArg(arg, context), strings, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(final Stream<String> stream, final CommandContext<ServerCommandSource> context) {
        return ArgumentCompletions.suggestAt(context.getInput().length(), stream, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(final String string, final CommandContext<ServerCommandSource> context) {
        return ArgumentCompletions.suggestAt(context.getInput().length(), new String[]{string}, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(final String[] strings, final CommandContext<ServerCommandSource> context) {
        return ArgumentCompletions.suggestAt(context.getInput().length(), strings, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(final Iterable<String> iterable, final CommandContext<ServerCommandSource> context) {
        return ArgumentCompletions.suggestAt(context.getInput().length(), iterable, context);
    }

    public static CompletableFuture<Suggestions> suggestAt(final int position, final Stream<String> stream, final CommandContext<ServerCommandSource> context) {
        return CommandSource.suggestMatching(stream, new SuggestionsBuilder(context.getInput(), position));
    }

    public static CompletableFuture<Suggestions> suggestAt(final int position, final String[] strings, final CommandContext<ServerCommandSource> context) {
        return CommandSource.suggestMatching(strings, new SuggestionsBuilder(context.getInput(), position));
    }

    public static CompletableFuture<Suggestions> suggestAt(final int position, final Iterable<String> iterable, final CommandContext<ServerCommandSource> context) {
        return CommandSource.suggestMatching(iterable, new SuggestionsBuilder(context.getInput(), position));
    }

    private static String getInput(final CommandContext<ServerCommandSource> context) {
        return context.getInput().replace("/" + context.getNodes().get(0) + " ", "");
    }

    public static int getPendingCursor(final CommandContext<ServerCommandSource> context) {
        return context.getInput().length() - 1;
    }

    private static int getCursor(final CommandContext<ServerCommandSource> context) {
        return context.getInput().length();
    }

    private static int getCursorAtArg(final int pos, final CommandContext<ServerCommandSource> context) {
        return ArgumentCompletions.getInput(context).split(" ").length;
    }

    public static class Factory {
        private String[] args;
        private Map<Integer, String[]> suggestions;
        private SuggestionsBuilder builder;

        public Factory(SuggestionsBuilder builder) {
            this.args = builder.getInput().split(" ");
            this.builder = builder;
            this.suggestions = new HashMap<>();
        }

        public Factory suggest(int arg, String... strings) {
            this.suggestions.put(arg, strings);
            return this;
        }

        public <E> Factory suggest(int arg, List<E> list) {
            String[] strings = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                strings[i] = String.valueOf(list.get(i));
            }

            return this.suggest(arg, strings);
        }

        public CompletableFuture<Suggestions> completeFuture() {
            if (args.length > this.suggestions.size() || this.suggestions.isEmpty() || this.args.length == 0) {
                return this.builder.buildFuture();
            }

            String[] strings = this.suggestions.get(this.args.length - 1);
            if (strings != null) {

                for (String string : strings) {
                    if (string.toLowerCase(Locale.ROOT).startsWith(this.args[this.args.length - 1].toLowerCase(Locale.ROOT))) {
                        this.builder.suggest(string);
                    }
                }
            }

            return this.builder.buildFuture();
        }

        public Iterable<String> complete() {
            if (args.length > this.suggestions.size() || this.suggestions.isEmpty() || this.args.length == 0) {
                return Collections.emptyList();
            }

            String[] strings = this.suggestions.get(this.args.length - 1);
            if (strings != null) {
                final List<String> suggestions = new ArrayList<>();

                for (String string : strings) {
                    if (string.toLowerCase(Locale.ROOT).startsWith(this.args[this.args.length - 1].toLowerCase(Locale.ROOT))) {
                        suggestions.add(string);
                    }
                }

                return suggestions.isEmpty() ? Collections.emptyList() : suggestions;
            }

            return Collections.emptyList();
        }

    }
}
