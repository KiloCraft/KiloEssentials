package org.kilocraft.essentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class VersionCommand {
	
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        String mcVer = MinecraftVersion.create().getName();

        dispatcher.register(
                CommandManager.literal("version").executes(context -> {
                    context.getSource().sendFeedback(new LiteralText(
                                    "This server is running Fabric for Minecraft (" + mcVer + ")")
                            .setStyle(new Style().setColor(Formatting.AQUA)),
                            false);
                    return 1;
                })
        );
    }
    
}
