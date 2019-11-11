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
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.teleport.BackCommand;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.homes.Home;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.user.UserHomeHandler;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;

public class HomeCommand {

    private static final SimpleCommandExceptionType HOME_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Can not find the home specified!"));
    private static final SimpleCommandExceptionType TOO_MANY_PROFILES = new SimpleCommandExceptionType(new LiteralText("Only one player is allowed but the provided selector includes more!"));
    private static final SimpleCommandExceptionType NO_HOMES_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Can not find any homes!"));
    private static final SimpleCommandExceptionType REACHED_THE_LIMIT = new SimpleCommandExceptionType(new LiteralText("You can't set any more Homes! you have reached the limit"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> homeLiteral = CommandManager.literal("home")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.self.tp"), 2))
                .executes(context -> KiloCommands.executeUsageFor("command.home.usage", context.getSource()));
        LiteralArgumentBuilder<ServerCommandSource> sethomeLiteral = CommandManager.literal("sethome")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.self.set"), 2))
                .executes(context -> KiloCommands.executeUsageFor("command.home.usage", context.getSource()));
        LiteralArgumentBuilder<ServerCommandSource> delhomeLiteral = CommandManager.literal("delhome")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.self.remove"), 2))
                .executes(context -> KiloCommands.executeUsageFor("command.home.usage", context.getSource()));
        LiteralArgumentBuilder<ServerCommandSource> homesLiteral = CommandManager.literal("homes")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("homes.self"), 2));
        RequiredArgumentBuilder<ServerCommandSource, String> argRemove, argSet, argTeleport;

        argRemove = CommandManager.argument("home", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.self.remove"), 2));
        argSet = CommandManager.argument("name", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.self.set"), 2));
        argTeleport = CommandManager.argument("home", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.self.tp"), 2));

        argSet.executes(
                c -> executeSet(
                        c, Collections.singleton(c.getSource().getPlayer().getGameProfile())
                )
        );

        argRemove.executes(
                c -> executeRemove(
                    c, Collections.singleton(c.getSource().getPlayer().getGameProfile())
                )
        );

        argTeleport.executes(
                c -> executeTeleport(
                        c, Collections.singleton(c.getSource().getPlayer().getGameProfile())
                )
        );

        homesLiteral.executes(
                c -> executeList(c.getSource(), Collections.singleton(c.getSource().getPlayer().getGameProfile()))
        );

        homesLiteral.then(
                CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("homes.others"), 2))
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes(c -> executeList(c.getSource(), GameProfileArgumentType.getProfileArgument(c, "player")))
        );


        argTeleport.suggests((context, builder) -> UserHomeHandler.suggestUserHomes.getSuggestions(context, builder)); // TODO make a home argument provider stub
        argRemove.suggests((context, builder) -> UserHomeHandler.suggestUserHomes.getSuggestions(context, builder));

        argTeleport.then(
                CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                    .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.others.tp"), 2))
                    .suggests(ArgumentSuggestions::allPlayers)
                    .executes(c -> executeTeleport(c, GameProfileArgumentType.getProfileArgument(c, "player")))
        );

        argSet.then(
                CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.others.set"), 2))
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes(c -> executeSet(c, GameProfileArgumentType.getProfileArgument(c, "player")))
        );

        argRemove.then(
                CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.others.remove"), 2))
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes(c -> executeRemove(c, GameProfileArgumentType.getProfileArgument(c, "player")))
        );

        for (int i = 0; i == KiloConfig.getProvider().getMain().getIntegerSafely("homes.limit", 20); i++) {
            KiloCommands.getCommandPermission("home.self.limit." + i);
        }

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
            ServerUser serverUser = ServerUser.of(gameProfile);
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
            ServerUser serverUser = ServerUser.of(gameProfile);
            int homes = serverUser.getHomesHandler().getHomes().size();
            boolean canSet =
                    Thimble.hasPermissionOrOp(context.getSource(), KiloCommands.getCommandPermission("home.set.limit." + homes + 1), 3) ||
                    Thimble.hasPermissionOrOp(context.getSource(), KiloCommands.getCommandPermission("home.set.limit.bypass"), 3);

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
                                source.getWorld().getDimension().getType().getRawId(),
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
            ServerUser serverUser = ServerUser.of(gameProfile);

            if (serverUser.getHomesHandler().hasHome(arg)) {
                serverUser.getHomesHandler().removeHome(arg);

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
        ServerCommandSource source = context.getSource();
        String arg = StringArgumentType.getString(context, "home");

        if (gameProfiles.size() == 1) {
            GameProfile gameProfile = gameProfiles.iterator().next();
            ServerUser serverUser = ServerUser.of(gameProfile);

            if (serverUser.getHomesHandler().hasHome(arg)) {
            	BackCommand.setLocation(source.getPlayer(), new Vector3f(source.getPosition()), source.getPlayer().dimension);
                serverUser.getHomesHandler().teleportToHome(arg);

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
