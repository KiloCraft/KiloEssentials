package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.ServerUser;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class IgnoreCommand extends EssentialCommand {
    public IgnoreCommand() {
        super("ignore");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> userArgument = getUserArgument("user")
                .suggests(TabCompletions::allPlayersExceptSource)
                .executes(this::execute);

        commandNode.addChild(userArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = getOnlineUser(ctx);
        String inputName = getUserArgumentInput(ctx, "user");

        AtomicInteger atomicInteger = new AtomicInteger(AWAIT_RESPONSE);
        essentials.getUserThenAcceptAsync(src, inputName, (user) -> {
            if (((ServerUser) user).isStaff() || user.equals(src)) {
                src.sendLangMessage("command.ignore.error");
                return;
            }

            Map<String, UUID> ignoreList = ((ServerUser) src).getIgnoreList();
            if (ignoreList.containsValue(user.getUuid())) {
                ignoreList.remove(user.getUsername(), user.getUuid());
                src.sendLangMessage("command.ignore.remove", user.getNameTag());
                atomicInteger.set(SINGLE_SUCCESS);
                return;
            }

            ignoreList.put(user.getUsername(), user.getUuid());
            src.sendLangMessage("command.ignore.add", user.getNameTag());
            atomicInteger.set(SINGLE_SUCCESS);
        });

        return atomicInteger.get();
    }

}
