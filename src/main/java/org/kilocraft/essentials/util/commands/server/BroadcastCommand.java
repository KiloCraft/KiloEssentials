package org.kilocraft.essentials.util.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.CommandPermission;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class BroadcastCommand extends EssentialCommand {
    public BroadcastCommand() {
        super("broadcast", CommandPermission.BROADCAST);
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> messageArgument = this.argument("message", greedyString())
                .executes(this::execute);

        this.commandNode.addChild(messageArgument.build());
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        KiloChat.broadCast(ModConstants.translation("command.broadcast", getString(ctx, "message")));
        return SUCCESS;
    }
}