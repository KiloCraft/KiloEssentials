package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.ServerChat;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;

public class MessageCommand extends EssentialCommand {
    public MessageCommand() {
        super("message", new String[]{"ke_msg", "ke_tell", "ke_whisper"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetArgument = argument("target", player())
                .suggests(TabCompletions::allPlayers);

        RequiredArgumentBuilder<ServerCommandSource, String> messageArgument = argument("message", greedyString())
                .suggests(TabCompletions::noSuggestions)
                .executes(this::execute);

        targetArgument.then(messageArgument);
        commandNode.addChild(targetArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return ServerChat.executeSend(ctx.getSource(), getPlayer(ctx, "target"), getString(ctx, "message"));
    }

}
