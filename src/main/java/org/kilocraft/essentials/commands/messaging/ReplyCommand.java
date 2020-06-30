package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.MessageReceptionist;
import org.kilocraft.essentials.api.user.NeverJoinedUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;

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
                .executes(this::execute);

        this.commandNode.addChild(messageArgument.build());
    }

    private int execute(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getUserManager().getOnline(ctx.getSource());
        String message = getString(ctx, "message");
        MessageReceptionist lastReceptionist = user.getLastMessageReceptionist();

        if (lastReceptionist == null || lastReceptionist.getId() == null) {
            user.sendConfigMessage("chat.privateChat.noMessageToReply");
            return FAILED;
        }

        OnlineUser target = KiloServer.getServer().getOnlineUser(lastReceptionist.getId());

        if (target == null || target instanceof NeverJoinedUser) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        }

        return ServerChat.sendDirectMessage(ctx.getSource(), target, message);
    }
}
