package org.kilocraft.essentials.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.ChatColor;
import org.kilocraft.essentials.api.util.SomeGlobals;

public class TpsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tps")
                .requires(source -> Thimble.hasPermissionOrOp(source, "kiloapi.command.tps", 2))
                .executes(TpsCommand::run)
        );
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        ChatColor.sendToUniversalSource(
                context.getSource(),
                String.format(
                        "&6tps &8(&71m&8/&75m&8/&715m&8)&d %s&8,&d %s&8,&d %s&r",
                        SomeGlobals.tps1.getShortAverage(),
                        SomeGlobals.tps5.getShortAverage(),
                        SomeGlobals.tps15.getShortAverage()
                ),
                false);

        return 1;
    }
}
