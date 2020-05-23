package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class FormatPreviewCommand extends EssentialCommand {
    public FormatPreviewCommand() {
        super("formatpreview");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> stringArgument = argument("text", greedyString())
                .suggests(FormatPreviewCommand::staticSuggestion)
                .executes(FormatPreviewCommand::execute);

        commandNode.addChild(stringArgument.build());
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        String text = getString(ctx, "text");
        KiloChat.sendMessageToSource(ctx.getSource(), new TextMessage("&eText preview:&r\n" + text, true));

        return text.length();
    }

    private static CompletableFuture<Suggestions> staticSuggestion(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return ArgumentCompletions.suggestAtCursor("&", context);
    }
}
