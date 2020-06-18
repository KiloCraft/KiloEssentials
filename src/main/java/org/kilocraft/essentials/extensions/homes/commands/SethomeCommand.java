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
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.chat.LangText;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.io.IOException;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class SethomeCommand extends EssentialCommand {
    public SethomeCommand() {
        super("sethome", CommandPermission.HOME_SELF_SET);
        this.withUsage("command.sethome.usage", "name");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> homeArgument = argument("name", word())
                .executes(this::executeSelf);

        RequiredArgumentBuilder<ServerCommandSource, String> targetArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, CommandPermission.HOME_OTHERS_SET))
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

        if (!canSet(user) && !homeHandler.hasHome(name)) {
            user.sendMessage(messages.commands().playerHomes().reachedLimit);
            return FAILED;
        }

        if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
            KiloChat.sendMessageTo(player, getConfirmationText(name, ""));
            return AWAIT;
        } else {
            homeHandler.removeHome(name);
        }

        homeHandler.addHome(new Home(player.getUuid(), name, Vec3dLocation.of(player).shortDecimals()));
        user.sendMessage(new TextMessage(HomeCommand.replaceVariables(
                KiloConfig.messages().commands().playerHomes().homeSet, user, user, homeHandler.getHome(name)), user));

        return SUCCESS;
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        OnlineUser source = getOnlineUser(player);
        String inputName = getString(ctx, "user");
        String input = getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

        getEssentials().getUserThenAcceptAsync(player, inputName, (user) -> {
            UserHomeHandler homeHandler = user.getHomesHandler();

            if (CommandUtils.areTheSame(source, user) && canSet(user) && !homeHandler.hasHome(name)) {
                source.sendMessage(messages.commands().playerHomes().reachedLimit
                        .replace("{HOME_SIZE}", String.valueOf(homeHandler.getHomes().size())));
                return;
            }

            if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
                KiloChat.sendMessageTo(player, getConfirmationText(name, user.getUsername()));
                return;
            } else {
                homeHandler.removeHome(name);
            }

            homeHandler.addHome(new Home(user.getUuid(), name, Vec3dLocation.of(player).shortDecimals()));

            try {
                user.saveData();
            } catch (IOException e) {
                source.sendError(ExceptionMessageNode.USER_CANT_SAVE, user.getNameTag(), e.getMessage());
            }

            if (CommandUtils.areTheSame(source, user))
                source.sendMessage(messages.commands().playerHomes().homeSet
                        .replace("{HOME_NAME}", name));
            else source.sendMessage(messages.commands().playerHomes().admin().homeSet
                    .replace("{HOME_NAME}", name)
                    .replace("{TARGET_TAG}", user.getNameTag()));
        });

        return AWAIT;
    }
    
    private static boolean canSet(User user) {
        for (int i = 0; i < KiloConfig.main().homesLimit; i++) {
            String thisPerm = "kiloessentials.command.home.limit." + i;
            int allowed = Integer.parseInt(thisPerm.split("\\.")[4]);

            if (user.getHomesHandler().homes() + 1 <= allowed &&
                    KiloCommands.hasPermission(((OnlineUser) user).getCommandSource(), thisPerm, 3)) {
                return true;
            }
        }

        return KiloCommands.hasPermission(((OnlineUser) user).getCommandSource(), CommandPermission.HOME_SET_LIMIT_BYPASS);
    }

    private Text getConfirmationText(String homeName, String user) {
        return new LiteralText("")
                .append(LangText.get(true, "command.sethome.confirmation_message")
                        .formatted(Formatting.YELLOW))
                .append(new LiteralText(" [").formatted(Formatting.GRAY)
                        .append(new LiteralText("Click here to Confirm").formatted(Formatting.GREEN))
                        .append(new LiteralText("]").formatted(Formatting.GRAY))
                        .styled((style) -> {
                            return style.withFormatting(Formatting.GRAY).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Confirm").formatted(Formatting.YELLOW))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sethome -confirmed-" + homeName + " " + user));
                        }));
    }

}
