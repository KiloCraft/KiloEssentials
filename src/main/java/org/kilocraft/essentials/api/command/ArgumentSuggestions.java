package org.kilocraft.essentials.api.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.StringUtils;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.registry.RegistryUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ArgumentSuggestions {
    private static final PlayerList playerManager = KiloEssentials.getMinecraftServer().getPlayerList();

    public static CompletableFuture<Suggestions> users(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        List<String> list = Lists.newArrayList();
        for (OnlineUser user : KiloEssentials.getUserManager().getOnlineUsersAsList(KiloCommands.hasPermission(context.getSource(), CommandPermission.VANISH))) {
            if (user.hasNickname()) {
                list.add(StringUtils.uniformNickname(user.getDisplayName()));
            }

            list.add(user.getUsername());
        }

        return SharedSuggestionProvider.suggest(list, builder);
    }

    public static CompletableFuture<Suggestions> noSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        return new CompletableFuture<>();
    }

    public static CompletableFuture<Suggestions> allPlayers(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ArgumentSuggestions.playerManager.getPlayers().stream()
                .map(Player::getScoreboardName), builder);
    }

    public static CompletableFuture<Suggestions> allPlayersExceptSource(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ArgumentSuggestions.playerManager.getPlayers().stream().filter(p -> {
            try {
                return !p.equals(context.getSource().getPlayerOrException());
            } catch (CommandSyntaxException ignored) {
            }
            return false;
        }).map(Player::getScoreboardName), builder);
    }

    public static CompletableFuture<Suggestions> allPlayerNicks(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        final List<String> nicks = new ArrayList<>();
        for (final OnlineUser user : KiloEssentials.getUserManager().getOnlineUsersAsList(KiloCommands.hasPermission(context.getSource(), CommandPermission.VANISH))) {
            nicks.add(ComponentText.clearFormatting(user.getDisplayName()));
            nicks.add(user.getUsername());
        }

        return SharedSuggestionProvider.suggest(nicks, builder);
    }

    public static CompletableFuture<Suggestions> dimensions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        final List<String> dims = new ArrayList<>();
        for (ResourceKey<Level> worldRegistryKey : RegistryUtils.getWorldsKeySet()) {
            dims.add(worldRegistryKey.location().getPath());
        }
        return SharedSuggestionProvider.suggest(dims.stream(), builder);
    }

    public static CompletableFuture<Suggestions> allNonOperators(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ArgumentSuggestions.playerManager.getPlayers().stream().filter(p -> !ArgumentSuggestions.playerManager.isOp(p.getGameProfile())).map(Player::getScoreboardName), builder);
    }

    public static CompletableFuture<Suggestions> allOperators(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ArgumentSuggestions.playerManager.getOpNames(), builder);
    }

    public static SuggestionProvider<CommandSourceStack> getDateArguments = (context, builder) ->
            SharedSuggestionProvider.suggest(new String[]{"year", "month", "day", "minute", "second"}, builder);

    public static CompletableFuture<Suggestions> timeSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        final String[] values = {"s", "m", "h", "d"};
        final String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        final Integer in = Integer.parseInt(builder.getInput());

        if (builder.getInput().equals(String.valueOf(in))) {
            return SharedSuggestionProvider.suggest(values, builder);
        }

        return null;
    }

    public static CompletableFuture<Suggestions> stateSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(new String[]{"on", "off", "toggle"}, builder);
    }

    public static CompletableFuture<Suggestions> suggestAtArg(final int arg, final String[] strings, final CommandContext<CommandSourceStack> context) {
        return ArgumentSuggestions.suggestAt(ArgumentSuggestions.getCursorAtArg(arg, context), strings, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(final Stream<String> stream, final CommandContext<CommandSourceStack> context) {
        return ArgumentSuggestions.suggestAt(context.getInput().length(), stream, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(final String string, final CommandContext<CommandSourceStack> context) {
        return ArgumentSuggestions.suggestAt(context.getInput().length(), new String[]{string}, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(final String[] strings, final CommandContext<CommandSourceStack> context) {
        return ArgumentSuggestions.suggestAt(context.getInput().length(), strings, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(final Iterable<String> iterable, final CommandContext<CommandSourceStack> context) {
        return ArgumentSuggestions.suggestAt(context.getInput().length(), iterable, context);
    }

    public static CompletableFuture<Suggestions> suggestAt(final int position, final Stream<String> stream, final CommandContext<CommandSourceStack> context) {
        return SharedSuggestionProvider.suggest(stream, new SuggestionsBuilder(context.getInput(), position));
    }

    public static CompletableFuture<Suggestions> suggestAt(final int position, final String[] strings, final CommandContext<CommandSourceStack> context) {
        return SharedSuggestionProvider.suggest(strings, new SuggestionsBuilder(context.getInput(), position));
    }

    public static CompletableFuture<Suggestions> suggestAt(final int position, final Iterable<String> iterable, final CommandContext<CommandSourceStack> context) {
        return SharedSuggestionProvider.suggest(iterable, new SuggestionsBuilder(context.getInput(), position));
    }

    private static String getInput(final CommandContext<CommandSourceStack> context) {
        return context.getInput().replace("/" + context.getNodes().get(0) + " ", "");
    }

    public static int getPendingCursor(final CommandContext<CommandSourceStack> context) {
        return context.getInput().length() - 1;
    }

    private static int getCursor(final CommandContext<CommandSourceStack> context) {
        return context.getInput().length();
    }

    private static int getCursorAtArg(final int pos, final CommandContext<CommandSourceStack> context) {
        return ArgumentSuggestions.getInput(context).split(" ").length;
    }

    public static class Factory {
        private final String[] args;
        private final Map<Integer, String[]> suggestions;
        private final SuggestionsBuilder builder;

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
            if (this.args.length > this.suggestions.size() || this.suggestions.isEmpty() || this.args.length == 0) {
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
            if (this.args.length > this.suggestions.size() || this.suggestions.isEmpty() || this.args.length == 0) {
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
