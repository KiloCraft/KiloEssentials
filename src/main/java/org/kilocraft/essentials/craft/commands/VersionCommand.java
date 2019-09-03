package org.kilocraft.essentials.craft.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.Mod;
import org.kilocraft.essentials.craft.utils.LangText;

public class VersionCommand {
	
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        String mcVer = MinecraftVersion.create().getName();

        dispatcher.register(
                CommandManager.literal("version").executes(context -> {
                    context.getSource().sendFeedback(
                            LangText.getFormatter(true, "commands.version.info", mcVer, Mod.getLoaderVersion(), Mod.getMappingsVersion(), Mod.getVersion()),
                            false);
                    return 1;
                })
        );
    }
    
}
