package org.kilocraft.essentials.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class VersionCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalCommandNode_1 = CommandManager.literal("version").executes(context -> {
           context.getSource().sendFeedback(new LiteralText("This server is running Fabric with KiloEssentials VER?")
                   .setStyle(new Style().setColor(Formatting.AQUA))
                   , false);
            return 1;
        });

        dispatcher.register(literalCommandNode_1);
    }
}
