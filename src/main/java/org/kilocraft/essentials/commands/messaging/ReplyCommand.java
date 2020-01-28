package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;

import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class ReplyCommand extends EssentialCommand {
    public ReplyCommand() {
        super("reply", new String[]{"r", "respond"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> messageArgument = argument("message", greedyString())
                .suggests(TabCompletions::noSuggestions)
                .executes(this::execute);

        commandNode.addChild(messageArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getUserManager().getOnline(ctx.getSource());
        String message = getString(ctx, "message");
        UUID lastPMGetter = user.getLastPrivateMessageSender();

        if (lastPMGetter == null)
            throw NO_MESSAGES_EXCEPTION.create();

        return ServerChat.executeSend(ctx.getSource(), KiloServer.getServer().getPlayer(lastPMGetter), message);
    }

    private static final SimpleCommandExceptionType NO_MESSAGES_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You don't have any messages to reply to!"));
}
