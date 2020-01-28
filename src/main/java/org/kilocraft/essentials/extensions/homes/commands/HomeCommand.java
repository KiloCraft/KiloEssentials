package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.homes.api.UnsafeHomeException;
import org.kilocraft.essentials.user.UserHomeHandler;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class HomeCommand extends EssentialCommand {
    private static final SimpleCommandExceptionType MISSING_DIMENSION = new SimpleCommandExceptionType(new LiteralText("The Dimension this home exists in no longer exists"));

    public HomeCommand() {
        super("home", CommandPermission.HOME_SELF_TP);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> homeArgument = argument("name", word())
                .suggests(UserHomeHandler::suggestHomes)
                .executes(this::executeSelf);

        RequiredArgumentBuilder<ServerCommandSource, String> targetArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, CommandPermission.HOME_OTHERS_TP))
                .executes(this::executeOthers);

        homeArgument.then(targetArgument);
        commandNode.addChild(homeArgument.build());
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        OnlineUser user = getOnlineUser(player);
        UserHomeHandler homeHandler = user.getHomesHandler();
        String name = getString(ctx, "name");

        if (!homeHandler.hasHome(name)) {
            user.sendConfigMessage("commands.playerHomes.invalid_home");
            return -1;
        }

        try {
            homeHandler.teleportToHome(user, name);
        } catch (UnsafeHomeException e) {
            if (e.getReason() == UserHomeHandler.Reason.MISSING_DIMENSION)
                throw MISSING_DIMENSION.create();
        }

        user.sendMessage(new ChatMessage(KiloConfig.getMessage("commands.playerHomes.teleportTo")
                .replace("%HOME_NAME%", name), true));
        return SINGLE_SUCCESS;
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) {

        return SINGLE_SUCCESS;
    }

}
