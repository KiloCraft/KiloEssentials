package org.kilocraft.essentials.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.user.ServerUser;

import java.util.ArrayList;
import java.util.List;

public class NicknameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> commandNode = dispatcher.register(
                CommandManager.literal("nickname")
                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nickname"), 2))
                        .then(
                                CommandManager.argument("arg", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> suggestionProvider.getSuggestions(context, builder))
                                        .executes(context ->
                                                execute(ServerUser.of(context.getSource().getPlayer()), ServerUser.of(context.getSource().getPlayer()), StringArgumentType.getString(context, "arg"))
                                        )
                        )
        );

        dispatcher.register(CommandManager.literal("nick").redirect(commandNode));
    }

    private static int execute(ServerUser source, ServerUser target, String arg) {


        return 1;
    }

    private static SuggestionProvider<ServerCommandSource> suggestionProvider = ((context, builder) -> {
        List<String> suggestions = new ArrayList<String>(){{
            add("reset");
            add(ServerUser.of(context.getSource().getPlayer()).getNickname());
        }};

        if (Thimble.hasPermissionOrOp(context.getSource(), KiloCommands.getCommandPermission("nick.others"), 2)) {
            KiloServer.getServer().getPlayerManager().getPlayerList().forEach((player) -> suggestions.add(player.getGameProfile().getName()));
        }

        return CommandSource.suggestMatching(suggestions, builder);
    });
}
