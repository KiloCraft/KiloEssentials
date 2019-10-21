package org.kilocraft.essentials.craft.homesystem;

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
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.chat.ChatMessage;
import org.kilocraft.essentials.craft.chat.KiloChat;
import org.kilocraft.essentials.craft.commands.essentials.BackCommand;
import org.kilocraft.essentials.craft.config.KiloConifg;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;

public class HomeCommand {

    private static final SimpleCommandExceptionType HOME_NOT_FOUND_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Can not find the home specified!"));
    private static final SimpleCommandExceptionType TOO_MANY_PROFILES = new SimpleCommandExceptionType(new LiteralText("Only one player is allowed but the provided selector includes more!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> homeLiteral = CommandManager.literal("home");
        LiteralArgumentBuilder<ServerCommandSource> sethomeLiteral = CommandManager.literal("sethome");
        LiteralArgumentBuilder<ServerCommandSource> delhomeLiteral = CommandManager.literal("delhome");
        LiteralArgumentBuilder<ServerCommandSource> homesLiteral = CommandManager.literal("homes");
        RequiredArgumentBuilder<ServerCommandSource, String> argRemove, argSet, argTeleport;

        argRemove = CommandManager.argument("home", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home"), 2));
        argSet = CommandManager.argument("name", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home"), 2));
        argTeleport = CommandManager.argument("home", StringArgumentType.string())
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home"), 2));

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
                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.manage"), 2))
                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                        .executes(c -> executeList(c.getSource(), GameProfileArgumentType.getProfileArgument(c, "player")))
        );


        argTeleport.suggests((context, builder) -> HomeManager.suggestHomes.getSuggestions(context, builder));
        argRemove.suggests((context, builder) -> HomeManager.suggestHomes.getSuggestions(context, builder));

        argTeleport.then(
                CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                    .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.manage"), 2))
                    .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                    .executes(c -> executeTeleport(c, GameProfileArgumentType.getProfileArgument(c, "player")))
        );

        argSet.then(
                CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.manage"), 2))
                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                        .executes(c -> executeSet(c, GameProfileArgumentType.getProfileArgument(c, "player")))
        );

        argRemove.then(
                CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("home.manage"), 2))
                        .suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
                        .executes(c -> executeRemove(c, GameProfileArgumentType.getProfileArgument(c, "player")))
        );

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
            StringBuilder homes = new StringBuilder();
            if (source.getPlayer().getUuid().equals(gameProfile.getId())) homes.append("&6Homes&8:");
            else homes.append("&6" + gameProfile.getName() + "'s homes&8:");

            for (Home home  : HomeManager.getHomes(gameProfile.getId())) {
                homes.append("&7, &f").append(home.getName());
            }

            KiloChat.sendMessageTo(source, new ChatMessage(
                    homes.toString().replaceFirst("&7,", ""), true
            ));

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

            if (HomeManager.hasHome(gameProfile.getId(), arg)) {
                HomeManager.removeHome(HomeManager.getHome(gameProfile.getId(), arg));
            }

            HomeManager.addHome(
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
                        KiloConifg.getProvider().getMessages().get(true, "commands.playerHomes.set").replace("%HOMENAME%", arg),
                        true
                ));
            } else {
                KiloChat.sendMessageTo(source, new ChatMessage(
                        KiloConifg.getProvider().getMessages().get(true, "commands.playerHomes.admin.set")
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

            if (HomeManager.hasHome(gameProfile.getId(), arg)) {
                HomeManager.removeHome(HomeManager.getHome(gameProfile.getId(), arg));

                if (source.getPlayer().getUuid().equals(gameProfile.getId())) {
                    KiloChat.sendMessageTo(source, new ChatMessage(
                            KiloConifg.getProvider().getMessages().get(true, "commands.playerHomes.remove").replace("%HOMENAME%", arg),
                            true
                    ));
                } else {
                    KiloChat.sendMessageTo(source, new ChatMessage(
                            KiloConifg.getProvider().getMessages().get(true, "commands.playerHomes.admin.remove")
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

            if (HomeManager.hasHome(gameProfile.getId(), arg)) {
                HomeManager.teleport(source, HomeManager.getHome(gameProfile.getId(), arg));
                BackCommand.setLocation(source.getPlayer(), new Vector3f(source.getPosition()), source.getPlayer().dimension);

                if (source.getPlayer().getUuid().equals(gameProfile.getId())) {
                    KiloChat.sendMessageTo(source, new ChatMessage(
                            KiloConifg.getProvider().getMessages().get(true, "commands.playerHomes.teleportTo").replace("%HOMENAME%", arg),
                            true
                    ));
                } else {
                    KiloChat.sendMessageTo(source, new ChatMessage(
                            KiloConifg.getProvider().getMessages().get(true, "commands.playerHomes.admin.teleportTo")
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
