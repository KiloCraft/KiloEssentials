package org.kilocraft.essentials.util.commands.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.CommandPermission;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.command.argument.TimeArgumentType.time;

public class TimeCommand extends EssentialCommand {
    public TimeCommand() {
        super("ke_time", CommandPermission.TIME);
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> addArg = this.literal("add")
                .then(this.argument("time", time()).executes(context -> executeAdd(context, getInteger(context, "time"))));

        LiteralArgumentBuilder<ServerCommandSource> setArg = this.literal("set")
                .then(
                        this.argument("time", time()).executes(context -> executeAdd(context, getInteger(context, "time")))
                ).then(
                        this.literal("day").executes(context -> executeSet(context, 1000, "Day"))
                ).then(
                        this.literal("noon").executes(context -> executeSet(context, 6000, "noon"))
                ).then(
                        this.literal("night").executes(context -> executeSet(context, 13000, "Night"))
                ).then(
                        this.literal("midnight").executes(context -> executeSet(context, 18000, "Midnight"))
                );

        LiteralArgumentBuilder<ServerCommandSource> queryArg = this.literal("query")
                .then(
                        this.literal("daytime").executes(context -> executeQuery(context, getDayTime(context.getSource().getWorld()), "daytime"))
                ).then(
                        this.literal("gametime").executes(context -> executeQuery(context, (int) (context.getSource().getWorld().getTime() % 2147483647L), "gametime"))
                ).then(
                        this.literal("day").executes(context -> executeQuery(context, (int) (context.getSource().getWorld().getTimeOfDay() / 24000L % 2147483647L), "day"))
                ).then(
                        this.literal("timedate").executes(context -> executeQuery(context, (int) (context.getSource().getWorld().getTimeOfDay()), "time"))
                );

        this.commandNode.addChild(setArg.build());
        this.commandNode.addChild(queryArg.build());
        this.commandNode.addChild(addArg.build());
    }

    private static String getFormattedTime(ServerWorld world) {
        return String.format("%02d:%02d", (int) (world.getTimeOfDay() % 24000 / 1000) + 6, (int) (world.getTimeOfDay() % 1000 / 16.6));
    }

    //    private static int getMinute(ServerWorld world){return (int)(world.getTimeOfDay() %1000 / 16.6);}
//    private static int getHour(ServerWorld world){return (int)world.getTimeOfDay() %24000 / 1000;}
    private static int getDay(ServerWorld world) {
        return (int) world.getTimeOfDay() / 24000;
    }

    private static int getDayTime(ServerWorld serverWorld) {
        return (int) (serverWorld.getTimeOfDay() % 24000L);
    }

    private static int executeQuery(CommandContext<ServerCommandSource> context, int time, String query) {
        ServerWorld w = context.getSource().getWorld();
        CommandSourceUser user = CommandSourceServerUser.of(context);
        switch (query) {
            case "daytime":
                user.sendLangMessage("command.time.query.daytime", time);
                break;
            case "gametime":
                user.sendLangMessage("command.time.query.gametime", time);
                break;
            case "day":
                user.sendLangMessage("command.time.query.day", time);
                break;
            case "time":
                user.sendLangMessage("command.time.query.time", getDay(w), getFormattedTime(w));
                break;
        }

        return time;
    }

    public static int executeSet(CommandContext<ServerCommandSource> context, int time, String timeName) {
        CommandSourceUser user = CommandSourceServerUser.of(context);
        for (ServerWorld world : context.getSource().getServer().getWorlds()) {
            world.setTimeOfDay(world.getTimeOfDay() - (world.getTimeOfDay() % 24000) + time);
        }

        user.sendLangMessage("template.#2", "Server time", timeName + " &8(&d" + time + "&8)&r");

        return SUCCESS;
    }

    public static int executeAdd(CommandContext<ServerCommandSource> context, int timeToAdd) {
        CommandSourceUser user = CommandSourceServerUser.of(context);
        for (ServerWorld world : context.getSource().getServer().getWorlds()) {
            world.setTimeOfDay(world.getTimeOfDay() + timeToAdd);
        }

        user.sendLangMessage("template.#2", "Server time", context.getSource().getWorld().getTimeOfDay());
        return SUCCESS;
    }

}
