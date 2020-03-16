package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class HelpCommand extends EssentialCommand {
    public HelpCommand() {
        super("help");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    public int execute(CommandContext<ServerCommandSource> context) {
        String message = messages.commands().helpMessage;
        Text text = TextFormat.translateToNMSText(message);
        context.getSource().sendFeedback(text, false);

        return SINGLE_SUCCESS;
    }

}
