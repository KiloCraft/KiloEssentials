package org.kilocraft.essentials.api.util;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;

public class CommandSuggestions {

    private static PlayerManager playerManager = KiloServer.getServer().getPlayerManager();

    public static SuggestionProvider<ServerCommandSource> allPlayers = ((context, builder) ->
            CommandSource.suggestMatching(playerManager.getPlayerNames(), builder)
    );

    public static SuggestionProvider<ServerCommandSource> suggestInput = ((context, builder) -> {
        builder.suggest(context.getNodes().get(0).getNode().getName());
        return builder.buildFuture();
    });

    public static SuggestionProvider<ServerCommandSource> nonOperators = ((context, builder) -> {
        return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((p) -> {
            return !playerManager.isOperator(p.getGameProfile());
        }).map((p) -> {
            return p.getGameProfile().getName();
        }), builder);
    });

    public static SuggestionProvider<ServerCommandSource> operators = ((context, builder) -> {
        return CommandSource.suggestMatching(playerManager.getOpNames(), builder);
    });


    public static SuggestionProvider<ServerCommandSource> getDateArguments = ((context, builder) ->
            CommandSource.suggestMatching(new String[]{"year", "month", "day", "minute", "second"}, builder)
    );
}

