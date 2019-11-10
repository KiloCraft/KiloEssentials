package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.commands.CommandSuggestions;
import org.kilocraft.essentials.user.ServerUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.github.indicode.fabric.permissions.Thimble.hasPermissionOrOp;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.getCommandPermission;

public class NicknameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> commandNode = dispatcher.register(literal("nickname")
                .requires(src -> hasPermissionOrOp(src, getCommandPermission("nickname"), 2))
                .then(argument("args", StringArgumentType.greedyString())
                        .suggests(NicknameCommand::argsSuggestions)
                )
        );

        dispatcher.register(literal("nick").requires(src -> hasPermissionOrOp(src, getCommandPermission("nickname"), 2)).redirect(commandNode));
    }

    private static int execute(ServerUser source, ServerUser target, String arg) {
        // Empty?
        // r: YES Empty for now :p

        return 1;
    }

    private static CompletableFuture<Suggestions> argsSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSuggestions.suggestAtCursor(new String[]{"?"}, context);
    }

    private static SuggestionProvider<ServerCommandSource> suggestionProvider = ((context, builder) -> {
        List<String> suggestions = new ArrayList<String>(){{
            add("reset");
            add(ServerUser.of(context.getSource().getPlayer()).getNickname());
        }};

        if (hasPermissionOrOp(context.getSource(), getCommandPermission("nick.others"), 2)) {
            KiloServer.getServer().getPlayerManager().getPlayerList().forEach((player) -> suggestions.add(player.getGameProfile().getName()));
        }

        return CommandSource.suggestMatching(suggestions, builder);
    });
}
