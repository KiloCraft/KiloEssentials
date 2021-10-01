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
import net.minecraft.world.World;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.commands.KiloCommands;

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
        RequiredArgumentBuilder<ServerCommandSource, String> homeArgument = this.argument("name", word())
                .executes(this::executeSelf);

        RequiredArgumentBuilder<ServerCommandSource, String> targetArgument = this.getUserArgument("user")
                .requires(src -> this.hasPermission(src, CommandPermission.HOME_OTHERS_SET))
                .executes(this::executeOthers);

        homeArgument.then(targetArgument);
        this.commandNode.addChild(homeArgument.build());
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        OnlineUser user = this.getOnlineUser(player);
        UserHomeHandler homeHandler = user.getHomesHandler();
        String input = getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

        if (!canSet(user) && !homeHandler.hasHome(name)) {
            user.sendLangMessage("command.sethome.limit");
            return FAILED;
        }

        if (!World.isValid(player.getBlockPos())) {
            user.sendLangError("general.position_out_of_world");
            return FAILED;
        }

        if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
            user.sendMessage(this.getConfirmationText(name, ""));
            return AWAIT;
        } else {
            homeHandler.removeHome(name);
        }

        homeHandler.addHome(new Home(player.getUuid(), name, Vec3dLocation.of(player).shortDecimals()));
        user.sendLangMessage("command.sethome.self", name);

        return SUCCESS;
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        OnlineUser source = this.getOnlineUser(player);
        String inputName = getString(ctx, "user");
        String input = getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

        if (!World.isValid(player.getBlockPos())) {
            source.sendLangError("general.position_out_of_world");
            return FAILED;
        }

        this.getUserManager().getUserThenAcceptAsync(player, inputName, (user) -> {
            UserHomeHandler homeHandler = user.getHomesHandler();

            if (CommandUtils.areTheSame(source, user) && canSet(user) && !homeHandler.hasHome(name)) {
                source.sendLangMessage("command.sethome.limit");
                return;
            }

            if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
                source.sendMessage(this.getConfirmationText(name, user.getUsername()));
                return;
            } else {
                homeHandler.removeHome(name);
            }

            homeHandler.addHome(new Home(user.getUuid(), name, Vec3dLocation.of(player).shortDecimals()));

            try {
                user.saveData();
            } catch (IOException e) {
                source.sendLangError("exception.user_cant_save", user.getNameTag(), e.getMessage());
            }

            if (CommandUtils.areTheSame(source, user))
                source.sendLangMessage("command.sethome.self", name);
            else
                source.sendLangMessage("command.sethome.other", name, user.getDisplayName());
        });

        return AWAIT;
    }

    private static boolean canSet(User user) {
        if (KiloCommands.hasPermission(((OnlineUser) user).getCommandSource(), CommandPermission.HOME_SET_LIMIT_BYPASS))
            return true;

        return user.getHomesHandler().homes() < getHomeLimit(user);
    }

    /**
     * TODO: If this feature is more needed, a different solution to this would be a "path" system
     * kiloessentials.command.home.limit.<path>.x
     * Example:
     * Playtime earned homes = kiloessentials.command.home.limit.playtime.x
     * Donation earned homes = kiloessentials.command.home.limit.donate.x
    * */
    private static int getHomeLimit(User user) {
        return getHomePermissionLimit(user, "kiloessentials.command.home.limit.") +
                getHomePermissionLimit(user, "kiloessentials.command.home.limit.add.");
    }

    private static int getHomePermissionLimit(User user, String permission) {
        int homeLimit = 0;
        for (int i = 1; i <= KiloConfig.main().homesLimit; i++) {
            if (KiloCommands.hasPermission(((OnlineUser) user).getCommandSource(), permission + i)) {
                homeLimit = i;
            }
        }
        return homeLimit;
    }

    private Text getConfirmationText(String homeName, String user) {
        return new LiteralText("")
                .append(StringText.of("command.sethome.confirmation_message")
                        .formatted(Formatting.YELLOW))
                .append(new LiteralText(" [").formatted(Formatting.GRAY)
                        .append(new LiteralText("Click here to Confirm").formatted(Formatting.GREEN))
                        .append(new LiteralText("]").formatted(Formatting.GRAY))
                        .styled((style) -> {
                            return style.withFormatting(Formatting.GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Confirm").formatted(Formatting.YELLOW))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sethome -confirmed-" + homeName + " " + user));
                        }));
    }

}
