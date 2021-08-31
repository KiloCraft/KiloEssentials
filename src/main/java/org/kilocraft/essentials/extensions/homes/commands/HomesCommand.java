package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.containergui.ScreenGUIBuilder;
import org.kilocraft.essentials.api.containergui.buttons.GUIButton;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.text.Texter;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class HomesCommand extends EssentialCommand {
    public HomesCommand() {
        super("homes", CommandPermission.HOMES_SELF);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> targetArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, CommandPermission.HOMES_OTHERS))
                .executes(this::executeOthers);

        argumentBuilder.executes(this::executeSelf);
        commandNode.addChild(targetArgument.build());
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx);
        return sendInfo(user, user);
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        OnlineUser source = getOnlineUser(player);
        String inputName = getString(ctx, "user");

        getUserManager().getUserThenAcceptAsync(player, inputName, (user) -> sendInfo(source, user));
        return AWAIT;
    }

    private int sendInfo(OnlineUser source, User user) {
        boolean areTheSame = CommandUtils.areTheSame(source, user);
        assert user.getHomesHandler() != null;
        if (user.getHomesHandler().homes() == 0) {
            if (areTheSame)
                source.sendLangMessage("command.home.no_home.self");
            else
                source.sendLangMessage("command.home.no_home.other", user.getDisplayName());
            return FAILED;
        }

        Texter.ListStyle text = Texter.ListStyle.of(
                areTheSame ? "Homes" : user.getFormattedDisplayName() + "'s Homes"
                , Formatting.GOLD, Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY
        );

        for (Home home : user.getHomesHandler().getHomes()) {
            Vec3dLocation loc = (Vec3dLocation) home.getLocation();
            text.append(home.getName(),
                    Texter.Events.onHover(new LiteralText("")
                            .append(new LiteralText(tl("general.click_teleport")).formatted(Formatting.YELLOW))
                            .append("\n")
                            .append(Texter.newText(loc.asFormattedString()))
                    ),
                    Texter.Events.onClickRun("/home " + home.getName() + (areTheSame ? "" : " " + user.getUsername())));
        }

        source.sendMessage(text.build());
        return SUCCESS;
    }

    private int openScreen(final OnlineUser src, User user) {
        UserHomeHandler homeHandler = user.getHomesHandler();
        assert homeHandler != null;

        if (homeHandler.getHomes().size() == 0) {
            src.sendLangError("command.home.no_home.self");
            return FAILED;
        }

        ScreenGUIBuilder builder = new ScreenGUIBuilder()
                .titled(src.equals(user) ? "Homes" : user.getFormattedDisplayName() + "'s Homes");

        List<Item> wools = ItemTags.WOOL.values();
        int iconIndex = 0;
        for (int i = 0; i < homeHandler.getHomes().size(); i++) {
            Home home = homeHandler.getHomes().get(i);

            iconIndex = iconIndex > wools.size() ? 0 : iconIndex + 1;
            builder.addButton(
                    new ScreenGUIBuilder.Button(
                            new ScreenGUIBuilder.Icon(wools.get(iconIndex))
                                    .titled(home.getName())
                                    .addLore(Texter.newRawText("Click to Teleport!").formatted(Formatting.GREEN)).build()
                    )
                            .withClickAction(GUIButton.ClickAction.CLICK, () -> Home.teleportTo(src, home))
                            .build()
            );
        }

        builder.handleFor(src.asPlayer());
        return SUCCESS;
    }

}
