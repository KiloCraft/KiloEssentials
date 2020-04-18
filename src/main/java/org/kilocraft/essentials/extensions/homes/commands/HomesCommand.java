package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.text.Texter;

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
        OnlineUser user = getOnlineUser(ctx.getSource());
        return sendInfo(user, user);
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        OnlineUser source = getOnlineUser(player);
        String inputName = getString(ctx, "user");

        essentials.getUserThenAcceptAsync(player, inputName, (user) -> {
            sendInfo(source, user);
        });

        return AWAIT_RESPONSE;
    }

    private int sendInfo(OnlineUser source, User user) {
        boolean areTheSame = CommandUtils.areTheSame(source, user);
        if (user.getHomesHandler().homes() <= 0) {
             source.sendMessage(areTheSame ? KiloConfig.messages().commands().playerHomes().noHome :
                    KiloConfig.messages().commands().playerHomes().admin().noHome.replace("{TARGET_TAG}", user.getNameTag()));

            return SINGLE_FAILED;
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
                            .append(Texter.toText(loc.asFormattedString()))
                    ),
                    Texter.Events.onClickRun("/home " + home.getName() + (areTheSame ? "" : " " + user.getUsername())));
        }

        source.sendMessage(text.build());
        return SINGLE_SUCCESS;
    }
}
