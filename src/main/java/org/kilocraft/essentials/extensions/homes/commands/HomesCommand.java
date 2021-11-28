package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.text.Texter;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class HomesCommand extends EssentialCommand {
    public HomesCommand() {
        super("homes", CommandPermission.HOMES_SELF);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> targetArgument = this.getUserArgument("user")
                .requires(src -> this.hasPermission(src, CommandPermission.HOMES_OTHERS))
                .executes(this::executeOthers);

        this.argumentBuilder.executes(this::executeSelf);
        this.commandNode.addChild(targetArgument.build());
    }

    private int executeSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);
        return this.sendInfo(user, user);
    }

    private int executeOthers(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        OnlineUser source = this.getOnlineUser(player);
        String inputName = getString(ctx, "user");

        this.getUserManager().getUserThenAcceptAsync(player, inputName, (user) -> this.sendInfo(source, user));
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
                , ChatFormatting.GOLD, ChatFormatting.DARK_GRAY, ChatFormatting.WHITE, ChatFormatting.GRAY
        );

        for (Home home : user.getHomesHandler().getHomes()) {
            Vec3dLocation loc = (Vec3dLocation) home.getLocation();
            text.append(home.getName(),
                    Texter.Events.onHover(new TextComponent("")
                            .append(new TextComponent(ModConstants.translation("general.click_teleport")).withStyle(ChatFormatting.YELLOW))
                            .append("\n")
                            .append(Texter.newText(loc.asFormattedString()))
                    ),
                    Texter.Events.onClickRun("/home " + home.getName() + (areTheSame ? "" : " " + user.getUsername())));
        }

        source.sendMessage(text.build());
        return SUCCESS;
    }

}
