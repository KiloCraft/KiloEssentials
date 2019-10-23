package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.craft.KiloCommands;
import java.util.Iterator;

public class TimeCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){

        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("ke_time")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("time"),2));
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder1 = CommandManager.literal("ke_time")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("time"),2));
        RequiredArgumentBuilder<ServerCommandSource, String> argSet = CommandManager.argument("set", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("time.set"),2));
        RequiredArgumentBuilder<ServerCommandSource, String> argGet = CommandManager.argument("get", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("time.get"),2));


        argSet.executes(c -> executeSet(c)).suggests((context, builder) -> builder.suggest("set").buildFuture());
        argGet.executes(c -> executeGet(c, getTime(c.getSource().getWorld()))).suggests((context, builder) -> builder.suggest("get").buildFuture());
        argSet.then(CommandManager.argument("time", StringArgumentType.string())
                .suggests((context, builder) -> builder.suggest("noon").suggest("day").suggest("night").suggest("midnight", () -> new String("midnight")).suggest("day").buildFuture())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("time.set"),2))
                .executes(c -> executeSet(c))
        );

        argumentBuilder.then(argGet);
        argumentBuilder1.then(argGet);
        argumentBuilder.then(argSet);
        argumentBuilder1.then(argSet);

        dispatcher.register(argumentBuilder);
    }

    private static int getTime(ServerWorld world) {
        return (int)(world.getTimeOfDay() % 24000L);
    }


    public static int executeSet(CommandContext<ServerCommandSource> context){
        if(StringArgumentType.getString(context, "time") != null){
            String arg = StringArgumentType.getString(context, "time");
            Iterator world = context.getSource().getMinecraftServer().getWorlds().iterator();
            while(world.hasNext()){
                ServerWorld kcworld = (ServerWorld) world.next();
                switch (arg){
                    case "day": kcworld.setTimeOfDay(0);break;
                    case "night": kcworld.setTimeOfDay(12000);break;
                    case "midnight": kcworld.setTimeOfDay(18000);break;
                    case "noon": kcworld.setTimeOfDay(6000);break;
                    default: break;
                }
            }
        }
        return 1;
    }
    public static int executeGet(CommandContext<ServerCommandSource> context, int time) throws CommandSyntaxException {
        context.getSource().getPlayer().sendMessage(LangText.getFormatter(true, "command.time.get",time));
        return time;
    }

}
