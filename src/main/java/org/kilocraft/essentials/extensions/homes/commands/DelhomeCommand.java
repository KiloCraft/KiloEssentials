package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CmdUtils;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.io.IOException;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class DelhomeCommand extends EssentialCommand {
    public DelhomeCommand() {
        super("delhome", CommandPermission.HOME_SELF_REMOVE);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> homeArgument = argument("name", word())
                .suggests(UserHomeHandler::suggestHomes)
                .executes(this::executeSelf);

        RequiredArgumentBuilder<ServerCommandSource, String> targetArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, CommandPermission.HOME_OTHERS_REMOVE))
                .executes(this::executeOthers);

        homeArgument.then(targetArgument);
        commandNode.addChild(homeArgument.build());
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        OnlineUser user = getOnlineUser(player);
        UserHomeHandler homeHandler = user.getHomesHandler();
        String input = getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

        if (!homeHandler.hasHome(name)) {
            user.sendMessage(KiloConfig.messages().commands().playerHomes().invalidHome);
            return SINGLE_FAILED;
        }

        if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
            KiloChat.sendMessageTo(player, getConfirmationText(name, ""));
            return AWAIT_RESPONSE;
        } else {
            homeHandler.removeHome(name);
        }

        user.sendMessage(new ChatMessage(KiloConfig.messages().commands().playerHomes().homeRemoved
                .replace("{HOME_NAME}", name), true));

        return SINGLE_SUCCESS;
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        OnlineUser source = getOnlineUser(player);
        String inputName = getString(ctx, "user");
        String input = getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

        essentials.getUserThenAcceptAsync(player, inputName, (user) -> {
            UserHomeHandler homeHandler = user.getHomesHandler();

            if (!homeHandler.hasHome(name)) {
                if (CmdUtils.areTheSame(source, user))
                    source.sendMessage(messages.commands().playerHomes().noHome);
                else
                    source.sendMessage(messages.commands().playerHomes().admin().noHome
                            .replace("{TARGET_TAG}", user.getNameTag()));
                return;
            }

            if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
                KiloChat.sendMessageTo(player, getConfirmationText(name, user.getUsername()));
                return;
            } else {
                homeHandler.removeHome(name);
            }

            try {
                user.saveData();
            } catch (IOException e) {
                source.sendError(ExceptionMessageNode.USER_CANT_SAVE, user.getNameTag(), e.getMessage());
            }

            if (CmdUtils.areTheSame(source, user))
                source.sendMessage(messages.commands().playerHomes().homeRemoved
                        .replace("{HOME_NAME}", name));
            else source.sendMessage(messages.commands().playerHomes().admin().homeRemoved
                    .replace("{HOME_NAME}", name)
                    .replace("{TARGET_TAG}", user.getNameTag()));
        });

        return AWAIT_RESPONSE;
    }

    private Text getConfirmationText(String homeName, String user) {
        return new LiteralText("")
                .append(LangText.get(true, "command.delhome.confirmation_message")
                        .formatted(Formatting.YELLOW))
                .append(new LiteralText(" [").formatted(Formatting.GRAY)
                        .append(new LiteralText("Click here to Confirm").formatted(Formatting.GREEN))
                        .append(new LiteralText("]").formatted(Formatting.GRAY))
                        .styled((style) -> {
                            style.setColor(Formatting.GRAY);
                            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Confirm").formatted(Formatting.YELLOW)));
                            style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/delhome -confirmed-" + homeName + " " + user));
                        }));
    }
}
