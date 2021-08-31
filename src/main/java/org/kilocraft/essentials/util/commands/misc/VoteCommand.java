package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.commands.KiloCommands;

import static net.minecraft.server.command.CommandManager.literal;

public class VoteCommand implements ConfigurableFeature {
    @Override
    public boolean register() {
        KiloCommands.getDispatcher().register(literal("vote").executes(this::execute));
        return true;
    }

    public int execute(CommandContext<ServerCommandSource> ctx) {
        ((CommandSourceUser) new CommandSourceServerUser(ctx.getSource())).sendMessage(ComponentText.toComponent(KiloConfig.messages().commands().voteMessage));
        return 1;
    }

}
