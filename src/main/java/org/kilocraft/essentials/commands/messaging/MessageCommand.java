package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.chat.ServerChat;

public class MessageCommand extends EssentialCommand {
    public MessageCommand() {
        super("message", new String[]{"ke_msg", "ke_tell", "ke_whisper"});
        this.withUsage("command.message.usage", "target", "message");
    }

    @Override
    public final void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, EntitySelector> target = this.argument("target", EntityArgumentType.player());

        final RequiredArgumentBuilder<ServerCommandSource, String> message = this.argument("message", StringArgumentType.greedyString())
                .suggests(ArgumentCompletions::noSuggestions)
                .executes(MessageCommand::execute);

        target.then(message);
        this.commandNode.addChild(target.build());
    }

    private static int execute(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return ServerChat.executeSend(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "target"), StringArgumentType.getString(ctx, "message"));
    }

}
