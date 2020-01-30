package org.kilocraft.essentials.extensions.emoticles;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class MagicalparticlesCommand extends EssentialCommand {
    public MagicalparticlesCommand() {
        super("magicalparticles", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.MAGICALPARTICLES_SELF), new String[]{"mp"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {

    }
}
