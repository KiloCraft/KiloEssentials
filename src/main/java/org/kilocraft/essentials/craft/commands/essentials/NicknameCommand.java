package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.user.User;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NicknameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> commandNode = dispatcher.register(
                literal("nickname")
                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("nickname"), 2))
                        .then(
                                argument("arg", greedyString())
                                        .suggests((context, builder) -> suggestionProvider.getSuggestions(context, builder))
                                        .executes(context ->
                                                execute(User.of(context.getSource().getPlayer()), User.of(context.getSource().getPlayer()), getString(context, "arg"))
                                        )
                        )
        );

        dispatcher.register(literal("nick").redirect(commandNode));
    }

    private static int execute(User source, User target, String arg) {


        return 1;
    }

    private static SuggestionProvider<ServerCommandSource> suggestionProvider = ((context, builder) -> {
        List<String> suggestions = new ArrayList<String>(){{
            add("reset");
            add(User.of(context.getSource().getPlayer()).getNickname());
        }};

        if (Thimble.hasPermissionOrOp(context.getSource(), KiloCommands.getCommandPermission("nick.others"), 2)) {
            KiloServer.getServer().getPlayerManager().getPlayerList().forEach((player) -> suggestions.add(player.getGameProfile().getName()));
        }

        return CommandSource.suggestMatching(suggestions, builder);
    });
}
