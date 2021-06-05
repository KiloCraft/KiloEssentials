package org.kilocraft.essentials.commands.help;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;

public class HelpCommand extends EssentialCommand {
    public HelpCommand() {
        super("help", new String[]{"?"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.then(UsageCommand.getCommandArgument());
        argumentBuilder.executes(this::execute);
    }

    public int execute(CommandContext<ServerCommandSource> context) {
        String message = messages.commands().helpMessage;
        Text text = ComponentText.toText(message);
        context.getSource().sendFeedback(text, false);

        return SUCCESS;
    }

}
