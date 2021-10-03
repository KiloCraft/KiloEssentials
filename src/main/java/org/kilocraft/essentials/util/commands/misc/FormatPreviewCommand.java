package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class FormatPreviewCommand extends EssentialCommand {
    public FormatPreviewCommand() {
        super("formatpreview");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> stringArgument = this.argument("text", greedyString())
                .suggests(FormatPreviewCommand::staticSuggestion)
                .executes(FormatPreviewCommand::execute);

        LiteralArgumentBuilder<ServerCommandSource> legacyArgument = this.literal("legacy");
        RequiredArgumentBuilder<ServerCommandSource, String> legacyStringArgument = this.argument("text", greedyString())
                .suggests(FormatPreviewCommand::legacySuggestion)
                .executes(FormatPreviewCommand::executeLegacy);

        legacyArgument.then(legacyStringArgument);
        this.commandNode.addChild(legacyArgument.build());
        this.commandNode.addChild(stringArgument.build());
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        final String text = getString(ctx, "text");
        ctx.getSource().sendFeedback(ComponentText.toText(
                Component.text("Text Preview", NamedTextColor.YELLOW)
                        .append(Component.newline())
                        .append(ComponentText.of(text))
        ), false);

        return text.length();
    }

    private static int executeLegacy(CommandContext<ServerCommandSource> ctx) {
        final String text = getString(ctx, "text");
        ctx.getSource().sendFeedback(
                new LiteralText("Text Preview").formatted(Formatting.YELLOW)
                        .append("\n").append(ComponentText.toText(text))
                , false
        );

        return text.length();
    }


    private static CompletableFuture<Suggestions> staticSuggestion(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return ArgumentSuggestions.suggestAtCursor(new String[]{"<", ">"}, context);
    }

    private static CompletableFuture<Suggestions> legacySuggestion(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return ArgumentSuggestions.suggestAtCursor("&", context);
    }
}
