package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;

import static net.minecraft.server.command.CommandManager.literal;

public class VoteCommand implements ConfigurableFeature {
    @Override
    public boolean register() {
        KiloCommands.getDispatcher().register(literal("vote").executes(this::execute));
        return true;
    }

    public VoteCommand() {
    }

    public int execute(CommandContext<ServerCommandSource> context) {
        String jsonText = KiloConfig.messages().commands().voteMessage;
        Text text = TextFormat.translateToNMSText(jsonText);
        KiloChat.sendMessageToSource(context.getSource(), text);

        return 1;
    }
}
