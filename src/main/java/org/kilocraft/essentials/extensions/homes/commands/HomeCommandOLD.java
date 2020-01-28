package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
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
import org.kilocraft.essentials.extensions.homes.api.UnsafeHomeException;
import org.kilocraft.essentials.user.UserHomeHandler;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.command.arguments.GameProfileArgumentType.gameProfile;
import static net.minecraft.command.arguments.GameProfileArgumentType.getProfileArgument;
import static net.minecraft.server.command.CommandManager.argument;
import static org.kilocraft.essentials.KiloCommands.executeUsageFor;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class HomeCommandOLD {

    private static final SimpleCommandExceptionType HOME_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Can not find the home specified!"));
    private static final SimpleCommandExceptionType TOO_MANY_PROFILES = new SimpleCommandExceptionType(new LiteralText("Only one player is allowed but the provided selector includes more!"));
    private static final SimpleCommandExceptionType NO_HOMES_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Can not find any homes!"));
    private static final SimpleCommandExceptionType REACHED_THE_LIMIT = new SimpleCommandExceptionType(new LiteralText("You can't set any more Homes! you have reached the limit"));
    private static final SimpleCommandExceptionType MISSING_DIMENSION = new SimpleCommandExceptionType(new LiteralText("The Dimension this home exists in no longer exists"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> homeLiteral = CommandManager.literal("home_old")
                .requires(s -> hasPermission(s, CommandPermission.HOME_SELF_TP))
                .executes(context -> executeUsageFor("command.home.usage", context.getSource()));

        LiteralArgumentBuilder<ServerCommandSource> sethomeLiteral = CommandManager.literal("sethome_old")
                .requires(s -> hasPermission(s, CommandPermission.HOME_SELF_SET))
                .executes(context -> executeUsageFor("command.home.usage", context.getSource()));

        LiteralArgumentBuilder<ServerCommandSource> delhomeLiteral = CommandManager.literal("delhome_old")
                .requires(s -> hasPermission(s, CommandPermission.HOME_SELF_REMOVE))
                .executes(context -> executeUsageFor("command.home.usage", context.getSource()));

        LiteralArgumentBuilder<ServerCommandSource> homesLiteral = CommandManager.literal("homes_old")
                .requires(s -> hasPermission(s, CommandPermission.HOMES_SELF));

        RequiredArgumentBuilder<ServerCommandSource, String> argRemove, argSet, argTeleport;

        argRemove = argument("home", StringArgumentType.word());
        argSet = argument("name", StringArgumentType.word());
        argTeleport = argument("home", StringArgumentType.word());

        argSet.executes(c -> executeSet(
                c, Collections.singleton(c.getSource().getPlayer().getGameProfile())));

        argRemove.executes(c -> executeRemove(
                c, Collections.singleton(c.getSource().getPlayer().getGameProfile())));

        argTeleport.executes(c -> executeTeleport(
                c, Collections.singleton(c.getSource().getPlayer().getGameProfile())));

        homesLiteral.executes(c -> executeList(c.getSource(), Collections.singleton(c.getSource().getPlayer().getGameProfile())));

        homesLiteral.then(argument("player", gameProfile())
                .requires(s -> hasPermission(s, CommandPermission.HOMES_OTHERS, 2))
                .suggests(TabCompletions::allPlayers)
                .executes(c -> executeList(c.getSource(), getProfileArgument(c, "player"))));


        argTeleport.suggests(UserHomeHandler::suggestHomes);
        argRemove.suggests(UserHomeHandler::suggestHomes);

        argTeleport.then(
                argument("player", gameProfile())
                    .requires(s -> hasPermission(s, CommandPermission.HOME_OTHERS_TP, 2))
                    .suggests(TabCompletions::allPlayers)
                    .executes(c -> executeTeleport(c, getProfileArgument(c, "player"))));

        argSet.then(
                argument("player", gameProfile())
                        .requires(s -> hasPermission(s, CommandPermission.HOME_OTHERS_SET, 2))
                        .suggests(TabCompletions::allPlayers)
                        .executes(c -> executeSet(c, getProfileArgument(c, "player"))));

        argRemove.then(
                argument("player", gameProfile())
                        .requires(s -> hasPermission(s, CommandPermission.HOME_OTHERS_REMOVE, 2))
                        .suggests(TabCompletions::allPlayers)
                        .executes(c -> executeRemove(c, getProfileArgument(c, "player"))));

        delhomeLiteral.then(argRemove);
        homeLiteral.then(argTeleport);
        sethomeLiteral.then(argSet);

        dispatcher.register(homeLiteral);
        dispatcher.register(homesLiteral);
        dispatcher.register(sethomeLiteral);
        dispatcher.register(delhomeLiteral);
    }

    private static int executeList(ServerCommandSource source, Collection<GameProfile> gameProfiles) throws CommandSyntaxException {
        if (gameProfiles.size() > 1)
            throw TOO_MANY_PROFILES.create();

        GameProfile gameProfile = gameProfiles.iterator().next();
        User serverUser = KiloServer.getServer().getUserManager().getOffline(gameProfile).join(); // TODO threading in future

        if (serverUser instanceof NeverJoinedUser)
            throw NO_HOMES_EXCEPTION.create();

        int homesSize = serverUser.getHomesHandler().getHomes().size();

        if (homesSize == 0)
            throw NO_HOMES_EXCEPTION.create();

        String prefix = CommandHelper.areTheSame(source, serverUser) ? "Homes" : serverUser.getFormattedDisplayname() + "'s Homes";
        Text text = new LiteralText(prefix).formatted(Formatting.GOLD)
                .append(new LiteralText(" [ ").formatted(Formatting.DARK_GRAY))
                .append(new LiteralText(String.valueOf(homesSize)).formatted(Formatting.LIGHT_PURPLE))
                .append(new LiteralText(" ]: ").formatted(Formatting.DARK_GRAY));

        int i = 0;
        boolean nextColor = false;
        for (Home home : serverUser.getHomesHandler().getHomes()) {
            LiteralText thisHome = new LiteralText("");
            i++;

            Formatting thisFormat = nextColor ? Formatting.WHITE : Formatting.GRAY;

            thisHome.append(new LiteralText(home.getName()).styled((style) -> {
                style.setColor(thisFormat);
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new LiteralText("[i] ").formatted(Formatting.YELLOW)
                                .append(new LiteralText("Click to teleport!").formatted(Formatting.GREEN))));
                style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/home " + home.getName() + " " + serverUser.getUsername()));
            }));

            if (homesSize != i)
                thisHome.append(new LiteralText(", ").formatted(Formatting.DARK_GRAY));

            nextColor = !nextColor;

            text.append(thisHome);
        }

        KiloChat.sendMessageToSource(source, text);
        return 1;
    }

    private static boolean canSetHome(User user) {
        for (int i = 1; i < KiloConfig.getProvider().getMain().getIntegerSafely("homes.limit", 20); i++) {
            String thisPerm = "kiloessentials.command.home.limit." + i;
            int amount = Integer.parseInt(thisPerm.split("\\.")[4]);
            if (user.getHomesHandler().getHomes().size() < amount &&
                    Thimble.hasPermissionOrOp(((OnlineUser) user).getCommandSource(), thisPerm, 3)) {
                return true;
            }
        }

        return KiloCommands.hasPermission(((OnlineUser) user).getCommandSource(), CommandPermission.HOME_SET_LIMIT_BYPASS, 3);
    }

    private static int executeSet(CommandContext<ServerCommandSource> context, Collection<GameProfile> gameProfiles) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String arg = StringArgumentType.getString(context, "name");

        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();
            User serverUser = KiloServer.getServer().getUserManager().getOffline(gameProfile).join(); // TODO threading in future

            if(serverUser instanceof NeverJoinedUser)
                throw NO_HOMES_EXCEPTION.create();

            if (!canSetHome(serverUser))
                throw REACHED_THE_LIMIT.create();

            if (serverUser.getHomesHandler().hasHome(arg)) {
                serverUser.getHomesHandler().removeHome(arg);
            }

            serverUser.getHomesHandler().addHome(new Home(gameProfile.getId(), arg, Vec3dLocation.of(source.getPlayer()).shortDecimals()));

            if (source.getPlayer().getUuid().equals(gameProfile.getId())) {
                KiloChat.sendMessageTo(source, new ChatMessage(
                        KiloConfig.getProvider().getMessages().get(true, "commands.playerHomes.set").replace("%HOMENAME%", arg),
                        true
                ));
            } else {
                KiloChat.sendMessageTo(source, new ChatMessage(
                        KiloConfig.getProvider().getMessages().get(true, "commands.playerHomes.admin.set")
                                .replace("%HOMENAME%", arg).replace("%OWNER%", gameProfile.getName()),
                        true
                ));
            }

        } else
            throw TOO_MANY_PROFILES.create();

        return 1;
    }

    private static int executeRemove(CommandContext<ServerCommandSource> context, Collection<GameProfile> gameProfiles) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String arg = StringArgumentType.getString(context, "home");

        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();
            User user = KiloServer.getServer().getUserManager().getOffline(gameProfile).join(); // TODO threading in future

            if(user instanceof NeverJoinedUser) {
                throw NO_HOMES_EXCEPTION.create();
            }

            if (user.getHomesHandler().hasHome(arg)) {
                user.getHomesHandler().removeHome(arg);

                if (source.getPlayer().getUuid().equals(gameProfile.getId())) {
                    KiloChat.sendMessageTo(source, new ChatMessage(
                            KiloConfig.getProvider().getMessages().get(true, "commands.playerHomes.remove").replace("%HOMENAME%", arg),
                            true
                    ));
                } else {
                    KiloChat.sendMessageTo(source, new ChatMessage(
                            KiloConfig.getProvider().getMessages().get(true, "commands.playerHomes.admin.remove")
                                    .replace("%HOMENAME%", arg).replace("%OWNER%", gameProfile.getName()),
                            true
                    ));
                }

            } else
                throw HOME_NOT_FOUND_EXCEPTION.create();

        } else
            throw TOO_MANY_PROFILES.create();

        return 1;
    }

    private static int executeTeleport(CommandContext<ServerCommandSource> context, Collection<GameProfile> gameProfiles) throws CommandSyntaxException {
        // TODO this needs to be reworked cause you can't really get predictions on the name of a
        ServerCommandSource source = context.getSource();
        String arg = StringArgumentType.getString(context, "home");

        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();
            User user = KiloServer.getServer().getUserManager().getOffline(gameProfile).join(); // TODO threading in future

            if(user instanceof NeverJoinedUser) {
                throw NO_HOMES_EXCEPTION.create();
            }

            if (user.getHomesHandler().hasHome(arg)) {
                try {
                    user.getHomesHandler().teleportToHome(KiloServer.getServer().getUserManager().getOnline(source), arg);
                } catch (UnsafeHomeException e) {
                    if (e.getReason() == UserHomeHandler.Reason.MISSING_DIMENSION) {
                        throw MISSING_DIMENSION.create();
                    }
                }

                if (source.getPlayer().getUuid().equals(gameProfile.getId())) {
                    KiloChat.sendMessageTo(source, new ChatMessage(
                            KiloConfig.getProvider().getMessages().get(true, "commands.playerHomes.teleportTo").replace("%HOMENAME%", arg),
                            true
                    ));
                } else {
                    KiloChat.sendMessageTo(source, new ChatMessage(
                            KiloConfig.getProvider().getMessages().get(true, "commands.playerHomes.admin.teleportTo")
                                    .replace("%HOMENAME%", arg).replace("%OWNER%", gameProfile.getName()),
                            true
                    ));
                }

            } else
                throw HOME_NOT_FOUND_EXCEPTION.create();

        } else
            throw TOO_MANY_PROFILES.create();
        return 1;
    }

}
