package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;

@Deprecated
public class VoteCommand extends EssentialCommand {
    public VoteCommand() {
        super("vote");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> context) {
        Text text = TextFormat.translateToNMSText(messages.commands().voteMessage);
        context.getSource().sendFeedback(text, false);

        return 1;
    }

}
