package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config_old.KiloConfig;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class BroadcastCommand extends EssentialCommand {
    public BroadcastCommand() {
        super("broadcast", CommandPermission.BROADCAST);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> messageArgument = argument("message", greedyString())
                .executes(this::execute);

        commandNode.addChild(messageArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        String format = KiloConfig.getProvider().getMessages().getValue("commands.broadcast_format");
        KiloChat.broadCast(new ChatMessage(
                format.replace("%MESSAGE%", getString(ctx, "message")), true));
        return SINGLE_SUCCESS;
    }
}