package org.kilocraft.essentials.commands.misc;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;

import static net.minecraft.server.command.CommandManager.literal;

public class HelpCommand implements ConfigurableFeature {

    @Override
    public boolean register() {
        KiloCommands.getDispatcher().register(literal("ke_help").executes(HelpCommand::execute));
        return true;
    }

    public HelpCommand() {
    }

    public static int execute(CommandContext<ServerCommandSource> context) {
        FileConfig config = FileConfig.of(KiloConfig.getConfigPath() + "HelpCommandMessage.yaml");
        config.load();
        String message = config.getOrElse("message", "Missing config");
        KiloChat.sendMessageToSource(context.getSource(), new ChatMessage(message, true));

        return 1;
    }

}
