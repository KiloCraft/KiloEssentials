package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.craft.KiloCommands;
import java.util.Iterator;

public class TimeCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){

        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("ke_time")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("time"),2));
        RequiredArgumentBuilder<ServerCommandSource, String> argSet = CommandManager.argument("set", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("time.set"),2));

        argSet.executes(c -> executeSet(c));

        argSet.then(CommandManager.argument("ticks", IntegerArgumentType.integer(1))
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("time.set"),2))
                .executes(c -> executeSet(c))
        );

        argumentBuilder.then(argSet);
        dispatcher.register(argumentBuilder);
    }

    public static int executeSet(CommandContext<ServerCommandSource> context){
        int arg = IntegerArgumentType.getInteger(context, "ticks");
        Iterator world = context.getSource().getMinecraftServer().getWorlds().iterator();
        while(world.hasNext()){
            ServerWorld kcworld = (ServerWorld) world.next();
            kcworld.setTime((long) arg);
        }
        return 1;
    }
    public static int executeGet(CommandContext<ServerCommandSource> context){
        return (int)(context.getSource().getWorld().getTimeOfDay() % 24000L);
    }

}
