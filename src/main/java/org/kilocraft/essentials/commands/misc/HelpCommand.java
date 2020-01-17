package org.kilocraft.essentials.commands.misc;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;

import static net.minecraft.server.command.CommandManager.literal;

public class HelpCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("ke_help").executes(HelpCommand::execute));
    }

    public static int execute(CommandContext<ServerCommandSource> context) {
        FileConfig config = FileConfig.of(KiloConfig.getConfigPath() + "HelpMessage.yaml");
        config.load();
        String message = config.getOrElse("message", "Missing config");
        message = TextFormat.translate(message);
        KiloChat.sendMessageToSource(context.getSource(), TextFormat.translateToNMSText(message));
        config.close();

        return 1;
    }

}
