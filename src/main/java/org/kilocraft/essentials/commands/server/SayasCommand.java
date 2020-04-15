package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.chat.TextMessage;

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
                .suggests(this::playerSuggestions)
                .build();

        ArgumentCommandNode<ServerCommandSource, String> channelArg = argument("channel", word())
                .suggests(this::channelIdSuggestions)
                .build();

        ArgumentCommandNode<ServerCommandSource, MessageFormat> messageArg = argument("message", message())
                .suggests(ArgumentCompletions::noSuggestions)
                .executes(this::execute)
                .build();

        channelArg.addChild(messageArg);
        selectorArg.addChild(channelArg);
        commandNode.addChild(selectorArg);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String inputTarget = getString(ctx, "target");
        Text message = getMessage(ctx, "message");
        ServerChat.Channel channel = ServerChat.Channel.getById(StringArgumentType.getString(ctx, "channel"));
        OnlineUser src = this.getOnlineUser(ctx);
        OnlineUser target = this.getOnlineUser(inputTarget);

        if (channel == null) {
            src.sendLangError("channel.invalid");
            return SINGLE_FAILED;
        }

        if (inputTarget.equalsIgnoreCase("-Server")) {
            KiloServer.getServer().getPlayerManager().sendToAll(new TranslatableText("chat.type.announcement", "Server", message));
            return SINGLE_SUCCESS;
        }

        ServerChat.sendSafely(target, new TextMessage(message.asFormattedString()), channel);
        return SINGLE_SUCCESS;
    }

    private CompletableFuture<Suggestions> playerSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>(Arrays.asList(KiloServer.getServer().getPlayerManager().getPlayerNames()));
        strings.add("-server");
        return CommandSource.suggestMatching(strings, builder);
    }

    private CompletableFuture<Suggestions> channelIdSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        for (ServerChat.Channel value : ServerChat.Channel.values()) {
            strings.add(value.getId());
        }
        return CommandSource.suggestMatching(strings, builder);
    }
}
