package org.kilocraft.essentials.util.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.ServerChat;

public class MessageCommand extends EssentialCommand {
    public MessageCommand() {
        super("message", new String[]{"ke_msg", "ke_tell", "ke_whisper", "dm", "directmessage"});
        this.withUsage("command.message.usage", "target", "message");
    }

    @Override
    public void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> target = this.getOnlineUserArgument("target");

        RequiredArgumentBuilder<CommandSourceStack, String> message = this.argument("message", StringArgumentType.greedyString())
                .executes(this::execute);

        target.then(message);
        this.commandNode.addChild(target.build());
    }

    private int execute(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ServerChat.sendDirectMessage(ctx.getSource(), this.getOnlineUser(ctx, "target"), StringArgumentType.getString(ctx, "message"));
    }

}
