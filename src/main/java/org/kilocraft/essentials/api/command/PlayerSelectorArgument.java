package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;

public class PlayerSelectorArgument {
    public static SuggestionProvider<ServerCommandSource> getSuggestions() {
        return provideSuggestion;
    }

    public static ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> context, String argumentName) {
        return serverPlayerEntityFinder(StringArgumentType.getString(context, argumentName));
    }

    private static SuggestionProvider<ServerCommandSource> provideSuggestion = (context, builder) -> {
        KiloServer.getServer().getPlayerManager().getPlayerList().forEach((player) -> {
            builder.suggest(player.getName().asString());
        });

        return builder.buildFuture();
    };

    private static ServerPlayerEntity serverPlayerEntityFinder(String name) {
        ServerPlayerEntity player;
        player = KiloServer.getServer().getPlayerManager().getPlayer(name);
        return player;
    }

}
