package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.commands.CmdUtils;
import org.kilocraft.essentials.util.TextUtils;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

public class PlaytimeCommand extends EssentialCommand {
    private Predicate<ServerCommandSource> PERMISSION_CHECK_MODIFY = src -> hasPermission(src, CommandPermission.PLAYTIME_MODIFY);

    public PlaytimeCommand() {
        super("playtime", CommandPermission.PLAYTIME_SELF, new String[]{"pt"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> userArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, CommandPermission.PLAYTIME_OTHERS))
                .executes(this::executeOther);

        LiteralArgumentBuilder<ServerCommandSource> increaseArg = literal("increase")
                .requires(PERMISSION_CHECK_MODIFY)
                .then(argument("seconds", integer(0))
                        .executes(ctx -> set(ctx, "increase")));
        LiteralArgumentBuilder<ServerCommandSource> decreaseArg = literal("decrease")
                .requires(PERMISSION_CHECK_MODIFY)
                .then(argument("seconds", integer(0))
                        .executes(ctx -> set(ctx, "decrease")));
        LiteralArgumentBuilder<ServerCommandSource> setArg = literal("set")
                .requires(PERMISSION_CHECK_MODIFY)
                .then(argument("seconds", integer(0))
                        .executes(ctx -> set(ctx, "set")));

        userArgument.then(increaseArg);
        userArgument.then(decreaseArg);
        userArgument.then(setArg);
        argumentBuilder.executes(this::executeSelf);
        commandNode.addChild(userArgument.build());
    }

    private int set(CommandContext<ServerCommandSource> ctx, String type) {
        CommandSourceUser src = getServerUser(ctx);
        int ticks = getInteger(ctx, "seconds") * 20;

        AtomicInteger atomicInteger = new AtomicInteger(AWAIT_RESPONSE);
        essentials.getUserThenAcceptAsync(src, getUserArgumentInput(ctx, "user"), (user) -> {
            try {
                user.setTicksPlayed(
                        type.equals("set") ? ticks :
                                type.equals("increase") ? user.getTicksPlayed() + ticks : user.getTicksPlayed() - ticks
                );

                user.saveData();
            } catch (IOException e) {
                src.sendError(e.getMessage());
            }

            src.sendLangMessage("command.playtime.set", user.getNameTag(),
                    TimeDifferenceUtil.convertSecondsToString(user.getTicksPlayed() / 20, 'e', '6'));
            atomicInteger.set(user.getTicksPlayed());
        });

        return atomicInteger.get();
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return execute(getServerUser(ctx), getOnlineUser(ctx));
    }

    private int executeOther(CommandContext<ServerCommandSource> ctx) {
        CommandSourceUser src = getServerUser(ctx);
        String inputName = getUserArgumentInput(ctx, "user");

        if (getOnlineUser(inputName) != null)
            return execute(src, getOnlineUser(inputName));

        essentials.getUserThenAcceptAsync(src, getUserArgumentInput(ctx, "user"), (user) -> {
            execute(src, user);
        });

        return AWAIT_RESPONSE;
    }

    private int execute(CommandSourceUser src, User target) {
        String pt = target.getTicksPlayed() <= 0 ? tl("general.not_present") :
                TimeDifferenceUtil.convertSecondsToString(target.getTicksPlayed() / 20, '6', 'e');
        String firstJoin = target.getFirstJoin() != null ? TimeDifferenceUtil.formatDateDiff(target.getFirstJoin().getTime()) : tl("general.not_present");

        String title = CmdUtils.areTheSame(src, target) ? tl("command.playtime.title.self") : tl("command.playtime.title.others", target.getNameTag());
        TextUtils.InfoBlockStyle text = TextUtils.InfoBlockStyle.of(title);

        text.append(tl("command.playtime.total"), pt)
                .append(tl("command.playtime.first_join"), firstJoin);

        src.sendMessage(text.get());
        return target.getTicksPlayed();
    }

}
