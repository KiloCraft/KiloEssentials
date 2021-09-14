package org.kilocraft.essentials.util.commands.locate;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.CommandPermission;

public class LocateCommand extends EssentialCommand {
    public LocateCommand() {
        super("ke_locate", CommandPermission.LOCATE);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LocateBiomeCommand.registerAsChild(this.argumentBuilder);
        LocateStructureCommand.registerAsChild(this.argumentBuilder);
    }

}
