package org.kilocraft.essentials.util.commands.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.CommandPermission;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.commands.arguments.TimeArgument.time;

public class TimeCommand extends EssentialCommand {
    public TimeCommand() {
        super("ke_time", CommandPermission.TIME);
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> addArg = this.literal("add")
                .then(this.argument("time", time()).executes(context -> executeAdd(context, getInteger(context, "time"))));

        LiteralArgumentBuilder<CommandSourceStack> setArg = this.literal("set")
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

        LiteralArgumentBuilder<CommandSourceStack> queryArg = this.literal("query")
                .then(
                        this.literal("daytime").executes(context -> executeQuery(context, getDayTime(context.getSource().getLevel()), "daytime"))
                ).then(
                        this.literal("gametime").executes(context -> executeQuery(context, (int) (context.getSource().getLevel().getGameTime() % 2147483647L), "gametime"))
                ).then(
                        this.literal("day").executes(context -> executeQuery(context, (int) (context.getSource().getLevel().getDayTime() / 24000L % 2147483647L), "day"))
                ).then(
                        this.literal("timedate").executes(context -> executeQuery(context, (int) (context.getSource().getLevel().getDayTime()), "time"))
                );

        this.commandNode.addChild(setArg.build());
        this.commandNode.addChild(queryArg.build());
        this.commandNode.addChild(addArg.build());
    }

    private static String getFormattedTime(ServerLevel world) {
        return String.format("%02d:%02d", (int) (world.getDayTime() % 24000 / 1000) + 6, (int) (world.getDayTime() % 1000 / 16.6));
    }

    //    private static int getMinute(ServerWorld world){return (int)(world.getTimeOfDay() %1000 / 16.6);}
//    private static int getHour(ServerWorld world){return (int)world.getTimeOfDay() %24000 / 1000;}
    private static int getDay(ServerLevel world) {
        return (int) world.getDayTime() / 24000;
    }

    private static int getDayTime(ServerLevel serverWorld) {
        return (int) (serverWorld.getDayTime() % 24000L);
    }

    private static int executeQuery(CommandContext<CommandSourceStack> context, int time, String query) {
        ServerLevel w = context.getSource().getLevel();
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

    public static int executeSet(CommandContext<CommandSourceStack> context, int time, String timeName) {
        CommandSourceUser user = CommandSourceServerUser.of(context);
        for (ServerLevel world : context.getSource().getServer().getAllLevels()) {
            world.setDayTime(world.getDayTime() - (world.getDayTime() % 24000) + time);
        }

        user.sendLangMessage("template.#2", "Server time", timeName + " &8(&d" + time + "&8)&r");

        return SUCCESS;
    }

    public static int executeAdd(CommandContext<CommandSourceStack> context, int timeToAdd) {
        CommandSourceUser user = CommandSourceServerUser.of(context);
        for (ServerLevel world : context.getSource().getServer().getAllLevels()) {
            world.setDayTime(world.getDayTime() + timeToAdd);
        }

        user.sendLangMessage("template.#2", "Server time", context.getSource().getLevel().getDayTime());
        return SUCCESS;
    }

}
