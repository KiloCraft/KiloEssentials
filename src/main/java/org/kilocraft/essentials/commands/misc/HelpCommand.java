package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;

@Deprecated
public class HelpCommand extends EssentialCommand {
    public HelpCommand() {
        super("ke_help");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    public int execute(CommandContext<ServerCommandSource> context) {
        String message = "@Deprecated";
        message = TextFormat.translate(message);
        KiloChat.sendMessageToSource(context.getSource(), TextFormat.translateToNMSText(message));

        return SINGLE_SUCCESS;
    }

}
