package org.kilocraft.essentials.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class KiloAPICommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        TriggerEventApiCmd.register(dispatcher);
    }
}
