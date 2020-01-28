package org.kilocraft.essentials.commands.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.Iterator;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.command.arguments.TimeArgumentType.time;

public class TimeCommand extends EssentialCommand {
    public TimeCommand() {
        super("ke_time", CommandPermission.TIME);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> addArg = literal("add")
                .then(argument("time", time()).executes(context -> executeAdd(context, getInteger(context, "time"))));

        LiteralArgumentBuilder<ServerCommandSource> setArg = literal("set")
                .then(
                        argument("time", time()).executes(context -> executeAdd(context, getInteger(context, "time")))
                ).then(
                        literal("day").executes(context -> executeSet(context, 1000, "Day"))
                ).then(
                        literal("noon").executes(context -> executeSet(context, 6000, "noon"))
                ).then(
                        literal("night").executes(context -> executeSet(context, 13000, "Night"))
                ).then(
                        literal("midnight").executes(context -> executeSet(context, 18000, "Midnight"))
                );

        LiteralArgumentBuilder<ServerCommandSource> queryArg = literal("query")
                .then(
                        literal("daytime").executes(context -> executeQuery(context, getDayTime(context.getSource().getWorld()),"daytime"))
                ).then(
                        literal("gametime").executes(context -> executeQuery(context, (int) (context.getSource().getWorld().getTime() % 2147483647L), "gametime"))
                ).then(
                        literal("day").executes(context -> executeQuery(context, (int) (context.getSource().getWorld().getTimeOfDay() / 24000L % 2147483647L),"day"))
                ).then(
                        literal("timedate").executes(context -> executeQuery(context, (int) (context.getSource().getWorld().getTimeOfDay()),"time"))
                );

        commandNode.addChild(setArg.build());
        commandNode.addChild(queryArg.build());
        commandNode.addChild(addArg.build());
    }
    private static String getFormattedTime(ServerWorld world){return String.format("%02d:%02d", (int)(world.getTimeOfDay() %24000 / 1000)+6, (int)(world.getTimeOfDay() %1000 / 16.6));}

//    private static int getMinute(ServerWorld world){return (int)(world.getTimeOfDay() %1000 / 16.6);}
//    private static int getHour(ServerWorld world){return (int)world.getTimeOfDay() %24000 / 1000;}
    private static int getDay(ServerWorld world) {
        return (int)world.getTimeOfDay() / 24000;
    }

    private static int getDayTime(ServerWorld serverWorld) {
        return (int)(serverWorld.getTimeOfDay() % 24000L);
    }

    private static int executeQuery(CommandContext<ServerCommandSource> context, int time, String query) {
        ServerWorld w = context.getSource().getWorld();
        switch (query){
            case "daytime": KiloChat.sendLangMessageTo(context.getSource(), "command.time.query.daytime", time);break;
            case "gametime": KiloChat.sendLangMessageTo(context.getSource(), "command.time.query.gametime", time);break;
            case "day": KiloChat.sendLangMessageTo(context.getSource(), "command.time.query.day", time);break;
            case "time": KiloChat.sendLangMessageTo(context.getSource(), "command.time.query.time", getDay(w), getFormattedTime(w));break;
        }

        return time;
    }

    public static int executeSet(CommandContext<ServerCommandSource> context, int time, String timeName){
        Iterator<ServerWorld> iterator = context.getSource().getMinecraftServer().getWorlds().iterator();

        while (iterator.hasNext()) {
            ServerWorld world = (ServerWorld) iterator.next();
                world.setTimeOfDay(world.getTimeOfDay() - (world.getTimeOfDay() % 24000) + time);
        }

        KiloChat.sendLangMessageTo(context.getSource(), "template.#2", "Server time", timeName + " &8(&d" + time + "&8)&r");

        return SINGLE_SUCCESS;
    }

    public static int executeAdd(CommandContext<ServerCommandSource> context, int timeToAdd) {
        Iterator<ServerWorld> iterator = context.getSource().getMinecraftServer().getWorlds().iterator();

        while (iterator.hasNext()) {
            ServerWorld world = (ServerWorld) iterator.next();
            world.setTimeOfDay(world.getTimeOfDay() + timeToAdd);
        }

        KiloChat.sendLangMessageTo(context.getSource(), "template.#2", "Server time", context.getSource().getWorld().getTimeOfDay());
        return SINGLE_SUCCESS;
    }

    public static int executeGet(CommandContext<ServerCommandSource> context, int time) throws CommandSyntaxException {
        context.getSource().getPlayer().sendMessage(LangText.getFormatter(true, "command.time.get",time));
        return time;
    }

}
