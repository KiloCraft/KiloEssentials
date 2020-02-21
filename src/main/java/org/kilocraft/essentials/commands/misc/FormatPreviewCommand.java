package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class FormatPreviewCommand extends EssentialCommand {
    public FormatPreviewCommand() {
        super("formatpreview");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> stringArgument = argument("string", greedyString())
                .suggests(TabCompletions::textformatChars)
                .suggests(FormatPreviewCommand::staticSuggestion)
                .executes(FormatPreviewCommand::execute);

        commandNode.addChild(stringArgument.build());
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        String arg = getString(ctx, "string");
        KiloChat.sendMessageToSource(ctx.getSource(), new ChatMessage("&eString preview:\n" + arg, true));

        return SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> staticSuggestion(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return TabCompletions.suggestAtCursor("&", context);
    }
}
