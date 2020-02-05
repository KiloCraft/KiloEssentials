package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.chat.KiloChat;

import static net.minecraft.server.command.CommandManager.literal;

@Deprecated
public class VoteCommand implements ConfigurableFeature {

    @Override
    public boolean register() {
        KiloCommands.getDispatcher().register(literal("vote").executes(VoteCommand::execute));
        return true;
    }

    public VoteCommand() {
    }

    public static int execute(CommandContext<ServerCommandSource> context) {
        String jsonText = "@Deprecated";
        Text text = TextFormat.translateToNMSText(jsonText);
        KiloChat.sendMessageToSource(context.getSource(), text);

        return 1;
    }

}
