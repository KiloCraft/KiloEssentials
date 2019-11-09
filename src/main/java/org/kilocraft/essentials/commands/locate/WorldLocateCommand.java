package org.kilocraft.essentials.commands.locate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;

import static net.minecraft.server.command.CommandManager.literal;

public class WorldLocateCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("locate");
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("ke_locate")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("locate"), 2));

        LocateBiomeCommand.registerAsChild(builder);
        LocateStructureCommand.registerAsChild(builder);
        dispatcher.register(builder);
    }


}
