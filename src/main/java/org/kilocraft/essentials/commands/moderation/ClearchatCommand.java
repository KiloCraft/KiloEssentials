package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.SUCCESS;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class ClearchatCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> clearchatCommand = dispatcher.register(literal("clearchat")
                .requires(src -> hasPermission(src, "clearchat", 3))
                .executes(ClearchatCommand::execute)
                .then(argument("target", StringArgumentType.string())
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes(ClearchatCommand::execute)
                )
        );

        dispatcher.register(literal("cc").redirect(clearchatCommand));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        String arg = ctx.getArgument("target", String.class);


        return SUCCESS();
    }

}
