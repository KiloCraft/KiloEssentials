package org.kilocraft.essentials.commands.essentials.locateCommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.commands.essentials.locatecommands.LocateStructureCommand;
import org.kilocraft.essentials.commands.essentials.locatecommands.LocateBiomeCommand;

public class WorldLocateCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("locate");
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("ke_locate")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("locate"), 2));

        LocateBiomeCommand.registerAsChild(builder);
        LocateStructureCommand.registerAsChild(builder);
        dispatcher.register(builder);
    }


}
