package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.user.preference.Preferences;

import java.util.Map;
import java.util.UUID;

public class IgnoreCommand extends EssentialCommand {
    public IgnoreCommand() {
        super("ignore");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> userArgument = getUserArgument("user")
                .suggests(ArgumentSuggestions::allPlayersExceptSource)
                .executes(this::execute);

        commandNode.addChild(userArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = getOnlineUser(ctx);
        String inputName = getUserArgumentInput(ctx, "user");
        Map<UUID, String> ignoreList = src.getPreference(Preferences.IGNORE_LIST);
        if (inputName.matches("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b")) {
            UUID uuid = UUID.fromString(inputName);
            if (ignoreList.containsKey(uuid)) {
                ignoreList.remove(uuid);
                src.getPreferences().set(Preferences.IGNORE_LIST, ignoreList);
                src.sendLangMessage("command.ignore.remove", uuid);
                return SUCCESS;
            }
        }
        getEssentials().getUserThenAcceptAsync(src, inputName, (user) -> {
            if (((ServerUser) user).isStaff() || user.equals(src)) {
                src.sendLangMessage("command.ignore.error");
                return;
            }

            boolean remove = ignoreList.containsKey(user.getUuid());
            if (remove) ignoreList.remove(user.getUuid());
            else ignoreList.put(user.getUuid(), user.getUsername());
            src.getPreferences().set(Preferences.IGNORE_LIST, ignoreList);
            src.sendLangMessage(remove ? "command.ignore.remove" : "command.ignore.add" , user.getNameTag());

        });

        return AWAIT;
    }

}
