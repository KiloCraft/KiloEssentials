package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.EssentialPermissions;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.NeverJoinedUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.homes.api.Home;
import org.kilocraft.essentials.extensions.homes.api.UnsafeHomeException;
import org.kilocraft.essentials.user.UserHomeHandler;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;

import static net.minecraft.command.arguments.GameProfileArgumentType.gameProfile;
import static net.minecraft.command.arguments.GameProfileArgumentType.getProfileArgument;
import static net.minecraft.server.command.CommandManager.argument;
import static org.kilocraft.essentials.KiloCommands.executeUsageFor;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class HomeCommand {

    private static final SimpleCommandExceptionType HOME_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Can not find the home specified!"));
    private static final SimpleCommandExceptionType TOO_MANY_PROFILES = new SimpleCommandExceptionType(new LiteralText("Only one player is allowed but the provided selector includes more!"));
    private static final SimpleCommandExceptionType NO_HOMES_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Can not find any homes!"));
    private static final SimpleCommandExceptionType REACHED_THE_LIMIT = new SimpleCommandExceptionType(new LiteralText("You can't set any more Homes! you have reached the limit"));
    private static final SimpleCommandExceptionType MISSING_DIMENSION = new SimpleCommandExceptionType(new LiteralText("The Dimension this home exists in no longer exists"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> homeLiteral = CommandManager.literal("home")
                .requires(s -> hasPermission(s, EssentialPermissions.HOME_SELF_TP.getNode(), 2))
                .executes(context -> executeUsageFor("command.home.usage", context.getSource()));

        LiteralArgumentBuilder<ServerCommandSource> sethomeLiteral = CommandManager.literal("sethome")
                .requires(s -> hasPermission(s, EssentialPermissions.HOME_SELF_SET.getNode(), 2))
                .executes(context -> executeUsageFor("command.home.usage", context.getSource()));

        LiteralArgumentBuilder<ServerCommandSource> delhomeLiteral = CommandManager.literal("delhome")
                .requires(s -> hasPermission(s, EssentialPermissions.HOME_SELF_REMOVE.getNode(), 2))
                .executes(context -> executeUsageFor("command.home.usage", context.getSource()));

        LiteralArgumentBuilder<ServerCommandSource> homesLiteral = CommandManager.literal("homes")
                .requires(s -> hasPermission(s, EssentialPermissions.HOMES_SELF.getNode(), 2));

        RequiredArgumentBuilder<ServerCommandSource, String> argRemove, argSet, argTeleport;

        argRemove = argument("home", StringArgumentType.string())
                .requires(s -> hasPermission(s, "home.self.remove", 2));
        argSet = argument("name", StringArgumentType.string())
                .requires(s -> hasPermission(s, "home.self.set", 2));
        argTeleport = argument("home", StringArgumentType.string())
                .requires(s -> hasPermission(s, "home.self.tp", 2));

        argSet.executes(c -> executeSet(
                c, Collections.singleton(c.getSource().getPlayer().getGameProfile())));

        argRemove.executes(c -> executeRemove(
                c, Collections.singleton(c.getSource().getPlayer().getGameProfile())));

        argTeleport.executes(c -> executeTeleport(
                c, Collections.singleton(c.getSource().getPlayer().getGameProfile())));

        homesLiteral.executes(c -> executeList(c.getSource(), Collections.singleton(c.getSource().getPlayer().getGameProfile())));

        homesLiteral.then(argument("player", gameProfile())
                .requires(s -> hasPermission(s, EssentialPermissions.HOMES_OTHERS.getNode(), 2))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(c -> executeList(c.getSource(), getProfileArgument(c, "player"))));


        argTeleport.suggests(UserHomeHandler::suggestHomes);
        argRemove.suggests(UserHomeHandler::suggestHomes);

        argTeleport.then(
                argument("player", gameProfile())
                    .requires(s -> hasPermission(s, EssentialPermissions.HOME_OTHERS_TP.getNode(), 2))
                    .suggests(ArgumentSuggestions::allPlayers)
                    .executes(c -> executeTeleport(c, getProfileArgument(c, "player"))));

        argSet.then(
                argument("player", gameProfile())
                        .requires(s -> hasPermission(s, EssentialPermissions.HOME_OTHERS_SET.getNode(), 2))
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes(c -> executeSet(c, getProfileArgument(c, "player"))));

        argRemove.then(
                argument("player", gameProfile())
                        .requires(s -> hasPermission(s, EssentialPermissions.HOME_OTHERS_REMOVE.getNode(), 2))
                        .suggests(ArgumentSuggestions::allPlayers)
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
        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();
            User serverUser = KiloServer.getServer().getUserManager().getOffline(gameProfile).join(); // TODO threading in future

            if(serverUser instanceof NeverJoinedUser) {
                throw NO_HOMES_EXCEPTION.create();
            }

            StringBuilder homes = new StringBuilder();
            int homesSize = serverUser.getHomesHandler().getHomes().size();

            if (homesSize > 0) {
                if (source.getPlayer().getUuid().equals(gameProfile.getId())) homes.append("&6Homes&8 (&b").append(homesSize).append("&8)&7:");
                else homes.append("&6" + gameProfile.getName() + "'s homes&8 (&b").append(homesSize).append("&8)&7:");

                for (Home home  : serverUser.getHomesHandler().getHomes()) {
                    homes.append("&7, &f").append(home.getName());
                }

                KiloChat.sendMessageTo(source, new ChatMessage(
                        homes.toString().replaceFirst("&7,", ""), true
                ));
            } else
                throw NO_HOMES_EXCEPTION.create();

        } else
            throw TOO_MANY_PROFILES.create();
        return 1;
    }

    private static int executeSet(CommandContext<ServerCommandSource> context, Collection<GameProfile> gameProfiles) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String arg = StringArgumentType.getString(context, "name");
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();
            User serverUser = KiloServer.getServer().getUserManager().getOffline(gameProfile).join(); // TODO threading in future

            if(serverUser instanceof NeverJoinedUser) {
                throw NO_HOMES_EXCEPTION.create();
            }

            int homes = serverUser.getHomesHandler().getHomes().size();
            boolean canSet = true;
                    //KiloCommands.hasPermission(context.getSource(), EssentialPermissions.HOME_SET_LIMIT.getNode() + (homes + 1), 3) ||
                    //KiloCommands.hasPermission(context.getSource(), EssentialPermissions.HOME_SET_LIMIT_BYPASS.getNode(), 3);

            if (!canSet)
                throw REACHED_THE_LIMIT.create();
            else {
                if (serverUser.getHomesHandler().hasHome(arg)) {
                    serverUser.getHomesHandler().removeHome(arg);
                }

                serverUser.getHomesHandler().addHome(
                        new Home(
                                gameProfile.getId(),
                                arg,
                                Double.parseDouble(decimalFormat.format(source.getPlayer().getPos().getX())),
                                Double.parseDouble(decimalFormat.format(source.getPlayer().getPos().getY())),
                                Double.parseDouble(decimalFormat.format(source.getPlayer().getPos().getZ())),
                                DimensionType.getId(source.getWorld().getDimension().getType()),
                                Float.parseFloat(decimalFormat.format(source.getPlayer().yaw)),
                                Float.parseFloat(decimalFormat.format(source.getPlayer().pitch))
                        )
                );

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
