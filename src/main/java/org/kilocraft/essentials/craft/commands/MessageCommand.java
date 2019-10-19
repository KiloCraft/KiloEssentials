package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.chat.KiloChat;

public class MessageCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> node = dispatcher.register(
                CommandManager.literal("ke_msg")
                        .then(
                                CommandManager.argument("player", EntityArgumentType.player())
                                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                                        .then(
                                                CommandManager.argument("message", StringArgumentType.greedyString())
                                                        .executes(MessageCommand::execute)
                                        )
                        )
        );

        dispatcher.register(CommandManager.literal("ke_tell").redirect(node));
        dispatcher.register(CommandManager.literal("ke_message").redirect(node));
        dispatcher.register(CommandManager.literal("ke_whisper").redirect(node));

    }

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        String message = StringArgumentType.getString(context, "message");

        if (target.getName().equals(context.getSource().getPlayer())) {
            source.sendError(new LiteralText("You can't send a message to your self!"));
        } else {
            KiloChat

        }

        return 1;
    }

}
