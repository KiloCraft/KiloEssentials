package org.kilocraft.essentials.api.command.types;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.KiloCommand;

public interface CmdType$LiteralArgumentBuilder extends KiloCommand {
    void commandBuilder(LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder);
}
