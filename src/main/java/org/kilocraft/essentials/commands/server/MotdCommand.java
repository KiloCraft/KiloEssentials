package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NonNls;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class MotdCommand extends EssentialCommand {

    private static final Pattern COMPILE = Pattern.compile("\n");

    public MotdCommand() {
        super("motd", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SERVER_MANAGE_MOTD));
    }

    @Override
    public final void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, Integer> line = this.argument("line", IntegerArgumentType.integer(1, 2));
        final RequiredArgumentBuilder<ServerCommandSource, String> text = this.argument("text", StringArgumentType.string())
                .suggests(this::suggestions)
                .executes(this::execute);

        line.then(text);
        this.commandNode.addChild(line.build());
        this.withUsage("command.motd.usage", "line", "text");
    }

    private int execute(final CommandContext<ServerCommandSource> ctx) {
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
            this.server.getMetaManager().setDescription(TextUtils.toText(finalmotd));
        } catch (final IOException e) {
            this.getServerUser(ctx).sendError("Can not save the value \"motd\" in server.properties\n" + e.getMessage());

            KiloEssentials.getLogger().error("Can not save the value \"motd\" in server.properties");
            e.printStackTrace();
        }

        final String[] motd = this.server.getMetaManager().getDescription().asFormattedString().split(MotdCommand.COMPILE.pattern());

        ctx.getSource().sendFeedback(LangText.getFormatter(true, "command.motd.set", TextFormat.translate(motd[0]), TextFormat.translate(motd[1])), true);
        return this.SINGLE_SUCCESS;
    }

    private CompletableFuture<Suggestions> suggestions(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        final Collection<String> strings = new ArrayList<>();
        final int line = IntegerArgumentType.getInteger(context, "line");

        try {
            @NonNls final String desc = TextFormat.reverseTranslate(
                    this.server.getMetaManager().getDescription().asFormattedString().split(MotdCommand.COMPILE.pattern())[line - 1], '&');
            strings.add('"' + desc + '"');
        } catch (final ArrayIndexOutOfBoundsException ignored) {}

        return CommandSource.suggestMatching(strings, builder);
    }

}
