package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;

import java.io.IOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class DelhomeCommand extends EssentialCommand {
    public DelhomeCommand() {
        super("delhome", CommandPermission.HOME_SELF_REMOVE);
        this.withUsage("command.delhome.usage", "name");
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> homeArgument = this.argument("name", word())
                .suggests(UserHomeHandler::suggestHomes)
                .executes(this::executeSelf);

        RequiredArgumentBuilder<CommandSourceStack, String> targetArgument = this.getUserArgument("user")
                .requires(src -> this.hasPermission(src, CommandPermission.HOME_OTHERS_REMOVE))
                .executes(this::executeOthers);

        homeArgument.then(targetArgument);
        this.commandNode.addChild(homeArgument.build());
    }

    private int executeSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        OnlineUser user = this.getOnlineUser(player);
        UserHomeHandler homeHandler = user.getHomesHandler();
        String input = getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

        if (!homeHandler.hasHome(name)) {
            user.sendLangMessage("command.home.invalid_home");
            return FAILED;
        }

        if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
            user.sendLangMessage("command.delhome.notice", name, "/delhome -confirmed-" + name);
            return AWAIT;
        } else {
            homeHandler.removeHome(name);
        }

        user.sendLangMessage("command.delhome.self", name);

        return SUCCESS;
    }

    private int executeOthers(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        OnlineUser source = this.getOnlineUser(player);
        String inputName = getString(ctx, "user");
        String input = getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

        this.getUserManager().getUserThenAcceptAsync(player, inputName, (user) -> {
            UserHomeHandler homeHandler = user.getHomesHandler();

            if (!homeHandler.hasHome(name)) {
                if (CommandUtils.areTheSame(source, user))
                    source.sendLangMessage("command.home.no_home.self");
                else
                    source.sendLangMessage("command.home.no_home.other", user.getDisplayName());
                return;
            }

            if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
                source.sendLangMessage("command.delhome.notice", name, "/delhome -confirmed-" + name + " " + user.getUsername());
                return;
            } else {
                homeHandler.removeHome(name);
            }

            try {
                user.saveData();
            } catch (IOException e) {
                source.sendLangError("exception.user_cant_save", user.getNameTag(), e.getMessage());
            }

            if (CommandUtils.areTheSame(source, user))
                source.sendLangMessage("command.delhome.self", name);
            else
                source.sendLangMessage("command.delhome.other", name, user.getDisplayName());
        });

        return AWAIT;
    }

}
