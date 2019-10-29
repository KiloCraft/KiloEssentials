package org.kilocraft.essentials.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.util.SomeGlobals;

public class TpsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloAPICommands.getCommandPermission("tps");
        dispatcher.register(CommandManager.literal("tps")
                .requires(source -> Thimble.hasPermissionOrOp(source, KiloAPICommands.getCommandPermission("tps"), 2))
                .executes(TpsCommand::run)
        );
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        TextFormat.sendToUniversalSource(
                context.getSource(),
                String.format(
                        "&6TPS &8(&71m&8/&75m&8/&715m&8)&%s %s&8,&%s %s&8,&%s %s&r",
                        tpstoColorCode(SomeGlobals.tps1.getAverage()),
                        SomeGlobals.tps1.getShortAverage(),
                        tpstoColorCode(SomeGlobals.tps5.getAverage()),
                        SomeGlobals.tps5.getShortAverage(),
                        tpstoColorCode(SomeGlobals.tps15.getAverage()),
                        SomeGlobals.tps15.getShortAverage()
                ),
                false);

        return 1;
    }

    public static char tpstoColorCode(double tps){
        if(tps > 15){
            return 'a';
        }else if(tps > 10){
            return 'e';
        }else{
            return 'c';
        }
    }

}
