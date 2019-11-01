package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.chat.LangText;

public class KiloInfoCommands {
	
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> info = CommandManager.literal("essentials")
                .executes(context -> {
                    LangText.sendToUniversalSource(context.getSource(),
                            "command.info",
                            false,
                            Mod.getMinecraftVersion()
                    );
                    return 0;
                });

        LiteralArgumentBuilder<ServerCommandSource> version = CommandManager.literal("version")
                .executes(context -> {
                    LangText.sendToUniversalSource(context.getSource(),
                            "command.info.version",
                            false,
                            Mod.getVersion(),
                            Mod.getLoaderVersion(),
                            Mod.getMappingsVersion(),
                            Mod.getMinecraftVersion()
                    );
                    return 0;
                });

        dispatcher.register(info);
        dispatcher.register(version);
    }
    
}
