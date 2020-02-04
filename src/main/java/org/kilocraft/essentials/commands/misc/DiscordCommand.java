package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config_old.KiloConfig;

import static net.minecraft.server.command.CommandManager.literal;

public class DiscordCommand implements ConfigurableFeature {

    @Override
    public boolean register() {
        KiloCommands.getDispatcher().register(literal("discord").executes(DiscordCommand::execute));
        return true;
    }

    public DiscordCommand() {
    }

    public static int execute(CommandContext<ServerCommandSource> context) {
        String jsonText = KiloConfig.getMessage("commands.discord");
        Text text = TextFormat.translateToNMSText(jsonText);
        KiloChat.sendMessageToSource(context.getSource(), text);

        return 1;
    }

}
