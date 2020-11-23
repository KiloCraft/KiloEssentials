package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.KiloConfig;

import static net.minecraft.server.command.CommandManager.literal;

public class VoteCommand implements ConfigurableFeature {
    @Override
    public boolean register() {
        KiloCommands.getDispatcher().register(literal("vote").executes(this::execute));
        return true;
    }

    public int execute(CommandContext<ServerCommandSource> ctx) {
        KiloServer.getServer().getCommandSourceUser(ctx.getSource()).sendMessage(ComponentText.toComponent(KiloConfig.messages().commands().voteMessage));
        return 1;
    }

}
