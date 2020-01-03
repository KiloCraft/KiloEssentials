package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.commands.LiteralCommandModified;
import org.kilocraft.essentials.modsupport.VanishModSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class TabCompletions {

    private static PlayerManager playerManager = KiloServer.getServer().getPlayerManager();

    public static CompletableFuture<Suggestions> allPlayers(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((it) ->
                KiloEssentials.hasPermissionNode(it.getCommandSource(), EssentialPermission.STAFF) || !VanishModSupport.isVanished(it.getUuid()))
                .map(PlayerEntity::getEntityName), builder);
    }

    public static CompletableFuture<Suggestions> allPlayersExceptSource(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((p) -> {
            try {
                return !p.equals(context.getSource().getPlayer());
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
            return false;
        }).map(PlayerEntity::getEntityName), builder);
    }

    public static CompletableFuture<Suggestions> allPlayerNicks(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ArrayList<String> nicks = new ArrayList<String>();
        for (int i = 0; i < playerManager.getCurrentPlayerCount(); i++) {
            ServerPlayerEntity player = playerManager.getPlayerList().get(i);
            OnlineUser user = KiloServer.getServer().getUserManager().getOnline(player);
            if (user.hasNickname()) {
                nicks.add(user.getUsername());
            }
        }

        return CommandSource.suggestMatching(nicks, builder);
    }

    public static CompletableFuture<Suggestions> dimensions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> dims = new ArrayList<>();
        Registry.DIMENSION.forEach(dimType -> dims.add(DimensionType.getId(dimType).toString()));
        return CommandSource.suggestMatching(dims.stream(), builder);
    }

    public static CompletableFuture<Suggestions> usableCommands(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(
                KiloCommands.getDispatcher().getRoot().getChildren().stream().filter(
                        (child) -> LiteralCommandModified.canSourceUse(child, context.getSource())
                                && child instanceof LiteralCommandNode)
                        .map(CommandNode::getName),
                builder);
    }

    public static CompletableFuture<Suggestions> commands(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(
                KiloCommands.getDispatcher().getRoot().getChildren().stream().filter((child) -> !child.getName().startsWith(LiteralCommandModified.getNMSCommandPrefix())).map(CommandNode::getName),
                builder
        );
    }

    public static CompletableFuture<Suggestions> textformatChars(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return suggestAtCursor(Arrays.stream(TextFormat.getList()).filter((it) -> context.getInput().charAt(getPendingCursor(context)) == '&'), context);
    }


    public static CompletableFuture<Suggestions> allNonOperators(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((p) -> !playerManager.isOperator(p.getGameProfile())).map(PlayerEntity::getEntityName), builder);
    }

    public static CompletableFuture<Suggestions> allOperators(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(playerManager.getOpNames(), builder);
    }

    public static SuggestionProvider<ServerCommandSource> getDateArguments = ((context, builder) ->
            CommandSource.suggestMatching(new String[]{"year", "month", "day", "minute", "second"}, builder)
    );

    public static CompletableFuture<Suggestions> timeSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        String[] values = {"s", "m", "h", "d"};
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        Integer in = Integer.parseInt(builder.getInput());

        if (builder.getInput().equals(String.valueOf(in))) {
            return CommandSource.suggestMatching(values, builder);
        }

        return null;
    }

    public static CompletableFuture<Suggestions> suggestAtArg(int arg, String[] strings, CommandContext<ServerCommandSource> context) {
        return suggestAt(getCursorAtArg(arg, context), strings, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(Stream<String> stream, CommandContext<ServerCommandSource> context) {
        return suggestAt(context.getInput().length(), stream, context);
    }

    public static CompletableFuture<Suggestions> suggestAtCursor(String string, CommandContext<ServerCommandSource> context) {
        return suggestAt(context.getInput().length(), new String[]{string}, context);
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

    private static String getInput(CommandContext<ServerCommandSource> context) {
        return context.getInput().replace("/" + context.getNodes().get(0) + " ", "");
    }

    public static int getPendingCursor(CommandContext<ServerCommandSource> context) {
        return (context.getInput().length() - 1);
    }

    private static int getCursor(CommandContext<ServerCommandSource> context) {
        return context.getInput().length();
    }

    private static int getCursorAtArg(int pos, CommandContext<ServerCommandSource> context) {

        return getInput(context).split(" ").length;
    }

}
