package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public interface CommandRegistry {
    void register(CommandDispatcher<ServerCommandSource> dispatcher);
}
