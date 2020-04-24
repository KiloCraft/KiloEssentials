package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NonNls;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.chat.LangText;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.util.text.Texter;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class MotdCommand extends EssentialCommand {

    private static final Pattern COMPILE = Pattern.compile("\n");

    public MotdCommand() {
        super("motd", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SERVER_MANAGE_MOTD));
        this.withUsage("command.motd.usage", "line", "text");
    }

    @Override
    public final void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, Integer> line = this.argument("line", IntegerArgumentType.integer(1, 2));
        final RequiredArgumentBuilder<ServerCommandSource, String> text = this.argument("text", StringArgumentType.string())
                .suggests(this::suggestions)
                .executes(this::setMotd);

        line.then(text);
        this.commandNode.addChild(line.build());
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx) {
        final Text description = this.server.getMetaManager().getDescription();
        final String[] lines = description.asFormattedString().split("\n");

        this.getServerUser(ctx).sendLangMessage("command.motd", (lines.length >= 0) ? lines[0] : "", lines.length >= 1 ? lines[1] : "");
        return SUCCESS;
    }

    private int setMotd(final CommandContext<ServerCommandSource> ctx) {
        final int line = IntegerArgumentType.getInteger(ctx, "line");
        final Text description = this.server.getMetaManager().getDescription();
        final String input = MotdCommand.COMPILE.matcher(TextFormat.translate(StringArgumentType.getString(ctx, "text"))).replaceAll("");

        @NonNls String finalmotd = null;

        if (line == 1) {
            final String str = TextFormat.reverseTranslate(description.asFormattedString().split("\n")[1], '&');
            finalmotd = input + MotdCommand.COMPILE.pattern() + str;
        } else {
            final String str = TextFormat.reverseTranslate(description.asFormattedString().split("\n")[0], '&');
            finalmotd = str + MotdCommand.COMPILE.pattern() + input;
        }

        try {
            this.server.getMetaManager().setDescription(Texter.toText(finalmotd));
        } catch (final IOException e) {
            this.getServerUser(ctx).sendError("Can not save the value \"motd\" in server.properties\n" + e.getMessage());

            KiloEssentials.getLogger().error("Can not save the value \"motd\" in server.properties");
            e.printStackTrace();
        }

        final String[] motd = this.server.getMetaManager().getDescription().asFormattedString().split(MotdCommand.COMPILE.pattern());

        ctx.getSource().sendFeedback(LangText.getFormatter(true, "command.motd.set", TextFormat.translate(motd[0]), TextFormat.translate(motd[1])), true);
        return this.SUCCESS;
    }

    private CompletableFuture<Suggestions> suggestions(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        final int line = IntegerArgumentType.getInteger(context, "line");

        if (builder.getRemaining().isEmpty()) {
            builder.suggest("\"");

            try {
                @NonNls final String desc = TextFormat.reverseTranslate(
                        this.server.getMetaManager().getDescription().asFormattedString().split(MotdCommand.COMPILE.pattern())[line - 1], '&');
                builder.suggest('"' + desc + '"');
            } catch (final ArrayIndexOutOfBoundsException ignored) {}

        } else if (context.getInput().charAt(ArgumentCompletions.getPendingCursor(context)) == '&') {
            return ArgumentCompletions.suggestAtCursor(Arrays.stream(TextFormat.getList()), context);
        } else if (!builder.getRemaining().endsWith("\"")) {
            return ArgumentCompletions.suggestAtCursor("\"", context);
        } else if (builder.getRemaining().endsWith("\"")) {
            return ArgumentCompletions.suggestAtCursor("&", context);
        }

        return builder.buildFuture();
    }

}
