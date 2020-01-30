package org.kilocraft.essentials.commands.misc;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config_old.KiloConfigOLD;

public class HelpCommand extends EssentialCommand {
    public HelpCommand() {
        super("ke_help");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    public int execute(CommandContext<ServerCommandSource> context) {
        FileConfig config = FileConfig.of(KiloConfigOLD.getConfigPath() + "help.yml");
        config.load();
        String message = config.getOrElse("message", "Missing config");
        message = TextFormat.translate(message);
        KiloChat.sendMessageToSource(context.getSource(), TextFormat.translateToNMSText(message));
        config.close();

        return SINGLE_SUCCESS;
    }

}
