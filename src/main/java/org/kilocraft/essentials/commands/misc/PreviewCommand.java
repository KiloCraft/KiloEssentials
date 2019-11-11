package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.api.command.SuggestArgument;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PreviewCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> previewCommand = dispatcher.register(literal("preview")
            .executes(ctx -> KiloCommands.executeSmartUsageFor("preview", ctx.getSource()))
            .then(argument("string", greedyString()).suggests(CommandSuggestions::textformatChars).executes(PreviewCommand::execute))
        );

        dispatcher.getRoot().addChild(previewCommand);
        dispatcher.register(literal("stringpeeker").redirect(previewCommand));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        String arg = getString(ctx, "string");
        KiloChat.sendMessageToSource(ctx.getSource(), new ChatMessage("&eString preview:\n" + arg, true));

        return KiloCommands.SUCCESS();
    }
}
