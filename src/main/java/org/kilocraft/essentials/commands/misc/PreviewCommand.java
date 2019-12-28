package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.SUCCESS;
import static org.kilocraft.essentials.KiloCommands.executeSmartUsageFor;

public class PreviewCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> previewCommand = dispatcher.register(literal("colorpreview")
            .executes(ctx -> executeSmartUsageFor("preview", ctx.getSource()))
            .then(argument("string", greedyString())
                    .suggests(TabCompletions::textformatChars)
                    .suggests(PreviewCommand::staticSuggestion)
                    .executes(PreviewCommand::execute)
        ));

        dispatcher.getRoot().addChild(previewCommand);
        dispatcher.register(literal("stringpeeker").redirect(previewCommand));
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        String arg = getString(ctx, "string");
        KiloChat.sendMessageToSource(ctx.getSource(), new ChatMessage("&eString preview:\n" + arg, true));

        return SUCCESS();
    }

    private static CompletableFuture<Suggestions> staticSuggestion(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return TabCompletions.suggestAtCursor("&", context);
    }
}
