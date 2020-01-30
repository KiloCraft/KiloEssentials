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
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.NeverJoinedUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.config_old.KiloConfigOLD;
import org.kilocraft.essentials.extensions.homes.api.UnsafeHomeException;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

        user.sendMessage(new ChatMessage(KiloConfigOLD.getMessage("commands.playerHomes.teleporting")
                .replace("{HOME_NAME}", name), true));
        return SINGLE_SUCCESS;
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        String name = getString(ctx, "name");
        OnlineUser source = getOnlineUser(player);
        String inputName = getString(ctx, "user");

        CompletableFuture<Optional<User>> optionalCompletableFuture = getUser(inputName);
        ServerUserManager.UserLoadingText loadingText = new ServerUserManager.UserLoadingText(player);

        optionalCompletableFuture.thenAcceptAsync((optionalUser) -> {
            if (!optionalUser.isPresent() || optionalUser.get() instanceof NeverJoinedUser) {
                source.sendError(ExceptionMessageNode.USER_NOT_FOUND);
                loadingText.stop();
                return;
            }

            User user = optionalUser.get();
            KiloServer.getServer().getVanillaServer().execute(() -> {
                if (!user.getHomesHandler().hasHome(name)) {
                    source.sendConfigMessage("commands.playerHomes.invalid_home");
                    return;
                }

                try {
                    user.getHomesHandler().teleportToHome(source, name);
                } catch (UnsafeHomeException e) {
                    source.sendError(e.getMessage());
                }

                if (CommandHelper.areTheSame(source, user))
                    source.sendMessage(KiloConfigOLD.getMessage("commands.playerHomes.teleporting")
                            .replace("{HOME_NAME}", name));
                else source.sendMessage(KiloConfigOLD.getMessage("commands.playerHomes.admin.teleporting")
                        .replace("{HOME_NAME}", name)
                        .replace("{TARGET_TAG}", user.getNameTag()));
            });

            loadingText.stop();
        }, ctx.getSource().getMinecraftServer());

        if (!optionalCompletableFuture.isCompletedExceptionally()) {
            loadingText.start();
        }

        return AWAIT_RESPONSE;
    }


}
