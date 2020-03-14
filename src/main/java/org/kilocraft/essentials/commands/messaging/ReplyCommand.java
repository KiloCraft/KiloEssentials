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
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;

import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class ReplyCommand extends EssentialCommand {
    public ReplyCommand() {
        super("reply", new String[]{"r", "respond"});
        this.withUsage("command.message.reply.usage", "message");
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, String> messageArgument = this.argument("message", greedyString())
                .suggests(ArgumentCompletions::noSuggestions)
                .executes(this::execute);

        this.commandNode.addChild(messageArgument.build());
    }

    private int execute(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final OnlineUser user = KiloServer.getServer().getUserManager().getOnline(ctx.getSource());
        final String message = getString(ctx, "message");
        final UUID lastPMGetter = user.getLastPrivateMessageSender();

        if (lastPMGetter == null)
            throw ReplyCommand.NO_MESSAGES_EXCEPTION.create();

        return ServerChat.executeSend(ctx.getSource(), KiloServer.getServer().getPlayer(lastPMGetter), message);
    }

    private static final SimpleCommandExceptionType NO_MESSAGES_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("You don't have any messages to reply to!"));
}
