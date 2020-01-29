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
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.NeverJoinedUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class SethomeCommand extends EssentialCommand {
    public SethomeCommand() {
        super("sethome", CommandPermission.HOME_SELF_SET);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> homeArgument = argument("name", word())
                .suggests(TabCompletions::noSuggestions)
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

        if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
            KiloChat.sendMessageTo(player, getConfirmationText(name, ""));
            return AWAIT_RESPONSE;
        } else {
            homeHandler.removeHome(name);
        }

        homeHandler.addHome(new Home(player.getUuid(), name, Vec3dLocation.of(player).shortDecimals()));
        user.sendMessage(new ChatMessage(KiloConfig.getMessage("commands.playerHomes.set")
                .replace("{HOME_NAME}", name), true));

        return SINGLE_SUCCESS;
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        OnlineUser source = getOnlineUser(player);
        String inputName = getString(ctx, "user");
        String input = getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

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
                UserHomeHandler homeHandler = user.getHomesHandler();

                if (homeHandler.hasHome(name) && !input.startsWith("-confirmed-")) {
                    KiloChat.sendMessageTo(player, getConfirmationText(name, user.getUsername()));
                    return;
                } else {
                    homeHandler.removeHome(name);
                }

                if (CommandHelper.areTheSame(source, user))
                    source.sendMessage(KiloConfig.getMessage("commands.playerHomes.set")
                            .replace("{HOME_NAME}", name));
                else source.sendMessage(KiloConfig.getMessage("commands.playerHomes.admin.set")
                        .replace("{HOME_NAME}", name)
                        .replace("{TARGET_TAG}", user.getNameTag()));

                homeHandler.addHome(new Home(user.getUuid(), name, Vec3dLocation.of(player).shortDecimals()));

                try {
                    user.saveData();
                } catch (IOException e) {
                    source.sendError(ExceptionMessageNode.USER_CANT_SAVE, user.getNameTag(), e.getMessage());
                }
            });

            loadingText.stop();
        }, ctx.getSource().getMinecraftServer());

        if (!optionalCompletableFuture.isCompletedExceptionally()) {
            loadingText.start();
        }

        return AWAIT_RESPONSE;
    }

    private Text getConfirmationText(String homeName, String user) {
        return new LiteralText("")
                .append(LangText.get(true, "command.sethome.confirmation_message")
                        .formatted(Formatting.YELLOW))
                .append(new LiteralText(" [").formatted(Formatting.GRAY)
                        .append(new LiteralText("Click here to Confirm").formatted(Formatting.GREEN))
                        .append(new LiteralText("]").formatted(Formatting.GRAY))
                        .styled((style) -> {
                            style.setColor(Formatting.GRAY);
                            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Confirm").formatted(Formatting.YELLOW)));
                            style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sethome -confirmed-" + homeName + user));
                        }));
    }

}
