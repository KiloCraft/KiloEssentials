package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.ServerChat;

public class MessageCommand extends EssentialCommand {
    public MessageCommand() {
        super("message", new String[]{"ke_msg", "ke_tell", "ke_whisper", "dm", "directmessage"});
        this.withUsage("command.message.usage", "target", "message");
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> target = this.getOnlineUserArgument("target");

        RequiredArgumentBuilder<ServerCommandSource, String> message = this.argument("message", StringArgumentType.greedyString())
                .executes(this::execute);

        target.then(message);
        this.commandNode.addChild(target.build());
    }

    private int execute(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return ServerChat.sendDirectMessage(ctx.getSource(), this.getOnlineUser(ctx, "target"), StringArgumentType.getString(ctx, "message"));
    }

}
