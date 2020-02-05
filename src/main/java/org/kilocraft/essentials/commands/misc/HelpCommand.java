package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;

@Deprecated
public class HelpCommand extends EssentialCommand {
    public HelpCommand() {
        super("ke_help");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    public int execute(CommandContext<ServerCommandSource> context) {
        Text text = TextFormat.translateToNMSText(messages.commands().helpMessage);
        context.getSource().sendFeedback(text, false);

        return SINGLE_SUCCESS;
    }

}
