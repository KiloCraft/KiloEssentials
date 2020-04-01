package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentCompletions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.arguments.MessageArgumentType.*;

public class SayasCommand extends EssentialCommand {
    public SayasCommand() {
        super("sayas", CommandPermission.SAYAS_OTHERS, 4);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        ArgumentCommandNode<ServerCommandSource, String> selectorArg = argument("target", word())
                .suggests(SayasCommand::playerSuggestions)
                .executes(SayasCommand::execute)
                .build();

        ArgumentCommandNode<ServerCommandSource, MessageFormat> messageArg = argument("message", message())
                .suggests(ArgumentCompletions::noSuggestions)
                .executes(SayasCommand::execute)
                .build();

        selectorArg.addChild(messageArg);
        commandNode.addChild(selectorArg);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String inputTarget = getString(ctx, "target");
        Text message = getMessage(ctx, "message");

        if (inputTarget.equalsIgnoreCase("-Server")) {
            KiloServer.getServer().getPlayerManager().sendToAll(
                    new TranslatableText("chat.type.announcement", "Server", message));
            return 1;
        }

        KiloServer.getServer().getChatManager().getChannel(GlobalChat.getChannelId())
                .onChatMessage(KiloServer.getServer().getPlayer(inputTarget), message.asFormattedString());

        return 1;
    }

    private static CompletableFuture<Suggestions> playerSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>(Arrays.asList(KiloServer.getServer().getPlayerManager().getPlayerNames()));
        strings.add("-server");
        return CommandSource.suggestMatching(strings, builder);
    }
}
