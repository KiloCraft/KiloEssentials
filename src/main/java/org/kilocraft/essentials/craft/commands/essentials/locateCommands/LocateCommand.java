package org.kilocraft.essentials.craft.commands.essentials.locateCommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.KiloCommands;

public class LocateCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("locate");
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("ke_locate")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.locate", 2));

        LocateBiomeCommand.registerAsChild(builder);
        LocateStructureCommand.registerAsChild(builder);
        dispatcher.register(builder);
    }


}
