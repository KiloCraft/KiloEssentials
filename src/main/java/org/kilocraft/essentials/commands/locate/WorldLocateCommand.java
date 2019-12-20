package org.kilocraft.essentials.commands.locate;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;

import static net.minecraft.server.command.CommandManager.literal;

public class WorldLocateCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("ke_locate")
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.LOCATE));

        LocateBiomeCommand.registerAsChild(builder);
        LocateStructureCommand.registerAsChild(builder);
        dispatcher.register(builder);
    }


}
